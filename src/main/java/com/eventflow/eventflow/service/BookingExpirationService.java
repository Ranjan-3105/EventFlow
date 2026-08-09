package com.eventflow.eventflow.service;

import com.eventflow.eventflow.entity.Booking;
import com.eventflow.eventflow.entity.BookingStatus;
import com.eventflow.eventflow.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class BookingExpirationService {

    private final BookingRepository bookingRepository;

    public BookingExpirationService(
            BookingRepository bookingRepository
    ) {
        this.bookingRepository = bookingRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void expireBookings() {

        Instant now = Instant.now();

        List<Booking> expiredBookings =
                bookingRepository
                        .findByStatusAndExpiresAtLessThanEqual(
                                BookingStatus.PENDING,
                                now
                        );

        if (expiredBookings.isEmpty()) {
            return;
        }

        for (Booking booking : expiredBookings) {

            booking.setStatus(BookingStatus.EXPIRED);
            booking.setUpdatedAt(now);
        }

        bookingRepository.saveAll(expiredBookings);
    }
}