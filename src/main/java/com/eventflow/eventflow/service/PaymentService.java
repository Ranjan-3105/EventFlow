package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreatePaymentRequest;
import com.eventflow.eventflow.dto.response.PaymentResponse;
import com.eventflow.eventflow.entity.Booking;
import com.eventflow.eventflow.entity.BookingStatus;
import com.eventflow.eventflow.entity.Payment;
import com.eventflow.eventflow.entity.PaymentStatus;
import com.eventflow.eventflow.exception.BookingNotFoundException;
import com.eventflow.eventflow.exception.PaymentException;
import com.eventflow.eventflow.repository.BookingRepository;
import com.eventflow.eventflow.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            RazorpayClient razorpayClient
    ) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.razorpayClient = razorpayClient;
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

            return new PaymentResponse(
                    savedPayment.getId(),
                    booking.getId(),
                    savedPayment.getAmount(),
                    savedPayment.getStatus(),
                    savedPayment.getRazorpayOrderId(),
                    razorpayKeyId
            );

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
}