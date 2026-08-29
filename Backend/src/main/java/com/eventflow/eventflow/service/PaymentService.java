package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreatePaymentRequest;
import com.eventflow.eventflow.dto.request.VerifyPaymentRequest;
import com.eventflow.eventflow.dto.response.PaymentResponse;
import com.eventflow.eventflow.entity.Booking;
import com.eventflow.eventflow.entity.BookingSeat;
import com.eventflow.eventflow.kafka.KafkaEventProducer;
import com.eventflow.eventflow.kafka.event.BookingConfirmedEvent;
import com.eventflow.eventflow.entity.BookingStatus;
import com.eventflow.eventflow.entity.Payment;
import com.eventflow.eventflow.entity.PaymentStatus;
import com.eventflow.eventflow.exception.BookingNotFoundException;
import com.eventflow.eventflow.exception.PaymentException;
import com.eventflow.eventflow.repository.BookingRepository;
import com.eventflow.eventflow.repository.BookingSeatRepository;
import com.eventflow.eventflow.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final RazorpayClient razorpayClient;
    private final SeatLockService seatLockService;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            RazorpayClient razorpayClient,
            SeatLockService seatLockService, KafkaEventProducer kafkaEventProducer
    ) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.razorpayClient = razorpayClient;
        this.seatLockService = seatLockService;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    public PaymentResponse createPayment(
            CreatePaymentRequest request
    ) {

        Booking booking =
                bookingRepository
                        .findById(request.bookingId())
                        .orElseThrow(() ->
                                new BookingNotFoundException(
                                        "Booking not found"
                                )
                        );

        validateBooking(booking);

        if (paymentRepository.existsByBookingId(
                booking.getId()
        )) {

            throw new PaymentException(
                    "Payment already exists for this booking."
            );
        }

        BigDecimal amount =
                booking.getTotalAmount();

        long amountInPaise =
                amount
                        .multiply(BigDecimal.valueOf(100))
                        .longValueExact();

        try {

            JSONObject orderRequest =
                    new JSONObject();

            orderRequest.put(
                    "amount",
                    amountInPaise
            );

            orderRequest.put(
                    "currency",
                    "INR"
            );

            orderRequest.put(
                    "receipt",
                    booking.getId().toString()
            );

            Order razorpayOrder =
                    razorpayClient.orders.create(
                            orderRequest
                    );

            String razorpayOrderId =
                    razorpayOrder.get("id");

            Instant now = Instant.now();

            Payment payment =
                    new Payment();

            payment.setId(
                    UUID.randomUUID()
            );

            payment.setBooking(booking);

            payment.setAmount(amount);

            payment.setStatus(
                    PaymentStatus.PENDING
            );

            payment.setRazorpayOrderId(
                    razorpayOrderId
            );

            payment.setCreatedAt(now);

            payment.setUpdatedAt(now);

            Payment savedPayment =
                    paymentRepository.save(payment);

            return mapToResponse(savedPayment);

        } catch (Exception ex) {

            throw new PaymentException(
                    "Unable to create Razorpay payment order."
            );
        }
    }

    private void validateBooking(
            Booking booking
    ) {

        String authenticatedEmail =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        if (!booking.getUser()
                .getEmail()
                .equals(authenticatedEmail)) {

            throw new PaymentException(
                    "You are not authorized to pay for this booking."
            );
        }

        if (booking.getStatus()
                != BookingStatus.PENDING) {

            throw new PaymentException(
                    "Only pending bookings can be paid."
            );
        }

        if (booking.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new PaymentException(
                    "Booking has expired."
            );
        }
    }

    public PaymentResponse verifyPayment(
            VerifyPaymentRequest request
    ) {

        Payment payment =
                paymentRepository
                        .findByRazorpayOrderId(
                                request.razorpayOrderId()
                        )
                        .orElseThrow(() ->
                                new PaymentException(
                                        "Payment order not found."
                                )
                        );

        /*
         * Idempotency:
         *
         * If the same successful payment verification request
         * arrives again, simply return the existing successful
         * payment instead of processing it again.
         */
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return mapToResponse(payment);
        }

        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new PaymentException(
                    "Payment has already failed."
            );
        }

        Booking booking =
                payment.getBooking();

        /*
         * The authenticated user must own the booking.
         */
        String authenticatedEmail =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        if (!booking.getUser()
                .getEmail()
                .equals(authenticatedEmail)) {

            throw new PaymentException(
                    "You are not authorized to verify this payment."
            );
        }

        /*
         * Verify that the order ID belongs to this payment.
         */
        if (!payment.getRazorpayOrderId()
                .equals(request.razorpayOrderId())) {

            throw new PaymentException(
                    "Payment order mismatch."
            );
        }

        /*
         * Verify Razorpay signature.
         */
        JSONObject attributes =
                new JSONObject();

        attributes.put(
                "razorpay_order_id",
                request.razorpayOrderId()
        );

        attributes.put(
                "razorpay_payment_id",
                request.razorpayPaymentId()
        );

        attributes.put(
                "razorpay_signature",
                request.razorpaySignature()
        );

        try {

            boolean valid =
                    Utils.verifyPaymentSignature(
                            attributes,
                            razorpayKeySecret
                    );

            if (!valid) {

                throw new PaymentException(
                        "Invalid payment signature."
                );
            }

        } catch (PaymentException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new PaymentException(
                    "Unable to verify payment signature."
            );
        }

        Instant now =
                Instant.now();

        /*
         * The booking must still be payable.
         */
        if (booking.getStatus()
                != BookingStatus.PENDING) {

            throw new PaymentException(
                    "Booking is no longer pending."
            );
        }

        if (booking.getExpiresAt()
                .isBefore(now)) {

            throw new PaymentException(
                    "Booking has expired."
            );
        }

        /*
         * Capture the values needed by the Redis cleanup
         * BEFORE the transaction completes.
         *
         * This prevents the afterCommit callback from depending
         * on lazy JPA entities after the transaction has ended.
         */
        UUID eventId =
                booking.getEvent().getId();

        String lockOwner =
                booking.getLockOwner();

        List<UUID> seatIds =
                bookingSeatRepository
                        .findByBookingId(booking.getId())
                        .stream()
                        .map(bookingSeat ->
                                bookingSeat
                                        .getSeat()
                                        .getId()
                        )
                        .toList();

        /*
         * Payment becomes successful.
         */
        payment.setRazorpayPaymentId(
                request.razorpayPaymentId()
        );

        payment.setPaymentReference(
                request.razorpayPaymentId()
        );

        payment.setStatus(
                PaymentStatus.SUCCESS
        );

        payment.setUpdatedAt(now);

        /*
         * Booking becomes confirmed.
         */
        booking.setStatus(
                BookingStatus.CONFIRMED
        );

        booking.setUpdatedAt(now);

        Payment savedPayment =
                paymentRepository.save(payment);

        bookingRepository.save(booking);

        List<String> seatLabels = bookingSeatRepository.findByBookingId(booking.getId())
                .stream()
                .map(bs -> String.valueOf(bs.getSeat().getRowLabel()) + bs.getSeat().getSeatNumber())
                .toList();

        BookingConfirmedEvent event = new BookingConfirmedEvent(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getEmail(),
                booking.getEvent().getTitle(),
                seatLabels,
                booking.getTotalAmount(),
                Instant.now()
        );

        kafkaEventProducer.publishBookingConfirmed(event);

        /*
         * Release Redis locks only after the database
         * transaction successfully commits.
         */
        if (TransactionSynchronizationManager
                .isSynchronizationActive()) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    for (UUID seatId : seatIds) {

                                        seatLockService.releaseLock(
                                                eventId,
                                                seatId,
                                                lockOwner
                                        );
                                    }
                                }
                            }
                    );
        }

        return mapToResponse(savedPayment);
    }

    private PaymentResponse mapToResponse(
            Payment payment
    ) {

        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getRazorpayOrderId(),
                razorpayKeyId
        );
    }
}