package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.BookingSeat;
import com.eventflow.eventflow.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingSeatRepository
        extends JpaRepository<BookingSeat, UUID> {

    boolean existsBySeatIdAndBookingEventIdAndBookingStatusIn(
            UUID seatId,
            UUID eventId,
            List<BookingStatus> statuses
    );
}
