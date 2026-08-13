package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateBookingRequest;
import com.eventflow.eventflow.dto.response.BookingResponse;
import com.eventflow.eventflow.entity.*;
import com.eventflow.eventflow.exception.EventNotFoundException;
import com.eventflow.eventflow.exception.InvalidBookingException;
import com.eventflow.eventflow.exception.SeatAlreadyBookedException;
import com.eventflow.eventflow.exception.UserNotFoundException;
import com.eventflow.eventflow.repository.BookingRepository;
import com.eventflow.eventflow.repository.BookingSeatRepository;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.SeatRepository;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.eventflow.eventflow.exception.BookingNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final SeatLockService seatLockService;
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository,
            UserRepository userRepository,
            SeatLockService seatLockService
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
        this.seatLockService = seatLockService;
    }

    private User getAuthenticatedUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Authenticated user not found"
                        ));
    }

    private Event getEvent(UUID eventId) {

        return eventRepository
                .findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(
                                "Event not found"
                        ));
    }

    /*
     * Validates the structural correctness of the requested seats.
     *
     * This happens BEFORE Redis because these checks do not involve
     * booking contention.
     */
    private List<Seat> validateSeatStructure(
            Event event,
            List<UUID> seatIds
    ) {

        // Prevent the same seat from being requested twice
        Set<UUID> uniqueSeatIds = new HashSet<>(seatIds);

        if (uniqueSeatIds.size() != seatIds.size()) {
            throw new InvalidBookingException(
                    "Duplicate seats cannot be selected."
            );
        }

        // Load all requested seats in one query
        List<Seat> seats = seatRepository.findAllById(seatIds);

        // Some requested IDs did not exist
        if (seats.size() != seatIds.size()) {
            throw new InvalidBookingException(
                    "One or more selected seats do not exist."
            );
        }

        // Every seat must belong to the event's hall
        UUID eventHallId = event.getHall().getId();

        for (Seat seat : seats) {

            if (!seat.getHall().getId().equals(eventHallId)) {
                throw new InvalidBookingException(
                        "One or more selected seats do not belong to this event's hall."
                );
            }
        }

        return seats;
    }

    /*
     * PostgreSQL remains the source of truth for booking state.
     *
     * This is intentionally executed AFTER Redis locking.
     */
    private void validateSeatAvailability(
            Event event,
            List<Seat> seats
    ) {

        Instant now = Instant.now();

        for (Seat seat : seats) {

            boolean alreadyBooked =
                    bookingSeatRepository
                            .existsActiveBookingForSeat(
                                    seat.getId(),
                                    event.getId(),
                                    now
                            );

            if (alreadyBooked) {
                throw new SeatAlreadyBookedException(
                        "Seat " +
                                seat.getRowLabel() +
                                seat.getSeatNumber() +
                                " is already booked."
                );
            }
        }
    }

    public BookingResponse createBooking(
            CreateBookingRequest request
    ) {

        User user = getAuthenticatedUser();

        Event event = getEvent(request.eventId());

        /*
         * Step 1:
         * Validate that the requested seats actually exist
         * and belong to this event's hall.
         */
        List<Seat> seats = validateSeatStructure(
                event,
                request.seatIds()
        );

        /*
         * Every booking attempt gets its own unique lock owner.
         */
        String lockOwner = UUID.randomUUID().toString();

        /*
         * Step 2:
         * Acquire Redis locks BEFORE checking booking availability
         * in PostgreSQL.
         *
         * This makes Redis our fast concurrency gate.
         */
        lockSeats(
                event,
                seats,
                lockOwner
        );

        try {

            /*
             * Step 3:
             * PostgreSQL remains the source of truth.
             *
             * Because we already own the Redis locks, only one
             * booking attempt for these seats should reach this point.
             */
            validateSeatAvailability(
                    event,
                    seats
            );

            /*
             * Step 4:
             * Calculate the booking amount.
             */
            BigDecimal totalAmount =
                    calculateTotalAmount(event, seats);

            /*
             * Step 5:
             * Create PENDING booking.
             */
            Booking booking = buildBooking(
                    user,
                    event,
                    totalAmount,
                    seats.size(),
                    lockOwner
            );

            Booking savedBooking =
                    bookingRepository.save(booking);

            /*
             * Step 6:
             * Create BookingSeat records.
             */
            List<BookingSeat> bookingSeats =
                    buildBookingSeats(
                            savedBooking,
                            seats,
                            event.getBasePrice()
                    );

            bookingSeatRepository.saveAll(bookingSeats);

            /*
             * IMPORTANT:
             *
             * We intentionally DO NOT release the Redis locks here.
             *
             * The booking is PENDING, so the seats remain protected
             * until payment succeeds, fails, or the lock expires.
             */
            return mapToResponse(savedBooking);

        } catch (RuntimeException ex) {

            /*
             * Something failed after Redis locks were acquired.
             *
             * Release them immediately instead of waiting for TTL.
             */
            releaseLocks(
                    event,
                    seats,
                    lockOwner
            );

            throw ex;
        }
    }

    private BigDecimal calculateTotalAmount(
            Event event,
            List<Seat> seats
    ) {

        return event.getBasePrice()
                .multiply(
                        BigDecimal.valueOf(seats.size())
                );
    }

    private Booking buildBooking(
            User user,
            Event event,
            BigDecimal totalAmount,
            int seatCount,
            String lockOwner
    ) {

        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(600);

        Booking booking = new Booking();

        booking.setId(UUID.randomUUID());
        booking.setUser(user);
        booking.setEvent(event);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalAmount(totalAmount);
        booking.setSeatCount(seatCount);
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setExpiresAt(expiresAt);
        booking.setLockOwner(lockOwner);

        return booking;
    }

    private List<BookingSeat> buildBookingSeats(
            Booking booking,
            List<Seat> seats,
            BigDecimal pricePerSeat
    ) {

        Instant now = Instant.now();

        return seats.stream()
                .map(seat -> {

                    BookingSeat bookingSeat = new BookingSeat();

                    bookingSeat.setId(UUID.randomUUID());
                    bookingSeat.setBooking(booking);
                    bookingSeat.setSeat(seat);
                    bookingSeat.setPricePaid(pricePerSeat);
                    bookingSeat.setCreatedAt(now);
                    bookingSeat.setUpdatedAt(now);

                    return bookingSeat;
                })
                .toList();
    }

    private BookingResponse mapToResponse(
            Booking booking
    ) {

        return new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getStatus(),
                booking.getSeatCount(),
                booking.getTotalAmount()
        );
    }

    private void lockSeats(
            Event event,
            List<Seat> seats,
            String owner
    ) {

        List<Seat> lockedSeats = new ArrayList<>();

        try {

            for (Seat seat : seats) {

                boolean acquired =
                        seatLockService.tryLock(
                                event.getId(),
                                seat.getId(),
                                owner
                        );

                if (!acquired) {

                    throw new SeatAlreadyBookedException(
                            "Seat " +
                                    seat.getRowLabel() +
                                    seat.getSeatNumber() +
                                    " is temporarily unavailable."
                    );
                }

                lockedSeats.add(seat);
            }

        } catch (RuntimeException ex) {

            /*
             * If one seat cannot be locked,
             * release every lock acquired so far.
             */
            releaseLocks(
                    event,
                    lockedSeats,
                    owner
            );

            throw ex;
        }
    }

    private void releaseLocks(
            Event event,
            List<Seat> seats,
            String owner
    ) {

        for (Seat seat : seats) {

            seatLockService.releaseLock(
                    event.getId(),
                    seat.getId(),
                    owner
            );
        }
    }

    public List<BookingResponse> getUserBookings() {
        User user = getAuthenticatedUser();
        return bookingRepository.findByUser_EmailOrderByCreatedAtDesc(user.getEmail())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookingResponse getUserBooking(UUID bookingId) {
        User user = getAuthenticatedUser();
        Booking booking = bookingRepository.findByIdAndUser_Email(bookingId, user.getEmail())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));
        return mapToResponse(booking);
    }

    public void cancelBooking(UUID bookingId) {
        User user = getAuthenticatedUser();
        Booking booking = bookingRepository.findByIdAndUser_Email(bookingId, user.getEmail())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.FAILED ||
            booking.getStatus() == BookingStatus.CANCELLED ||
            booking.getStatus() == BookingStatus.EXPIRED) {
            throw new InvalidBookingException("Cannot cancel a booking that is " + booking.getStatus());
        }

        boolean wasPending = (booking.getStatus() == BookingStatus.PENDING);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        if (wasPending) {
            String lockOwner = booking.getLockOwner();
            if (lockOwner == null || lockOwner.isBlank()) {
                log.warn("Cannot release Redis locks for PENDING booking {}: lockOwner is null or blank", bookingId);
            } else {
                List<Seat> bookedSeats = bookingSeatRepository.findByBookingId(bookingId)
                        .stream()
                        .map(BookingSeat::getSeat)
                        .toList();
                releaseLocks(booking.getEvent(), bookedSeats, lockOwner);
            }
        }
    }
}