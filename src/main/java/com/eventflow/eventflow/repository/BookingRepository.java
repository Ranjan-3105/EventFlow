package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.Booking;
import com.eventflow.eventflow.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository
        extends JpaRepository<Booking, UUID> {

    List<Booking> findByStatusAndExpiresAtLessThanEqual(
            BookingStatus status,
            Instant expiresAt
    );
}
