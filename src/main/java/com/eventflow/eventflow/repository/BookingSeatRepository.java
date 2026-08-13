package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.BookingSeat;
import com.eventflow.eventflow.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingSeatRepository
        extends JpaRepository<BookingSeat, UUID> {

    List<BookingSeat> findByBookingId(UUID bookingId);

    @Query("""
            SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END
            FROM BookingSeat bs
            JOIN bs.booking b
            WHERE bs.seat.id = :seatId
              AND b.event.id = :eventId
              AND (
                    b.status = com.eventflow.eventflow.entity.BookingStatus.CONFIRMED
                    OR (
                        b.status = com.eventflow.eventflow.entity.BookingStatus.PENDING
                        AND b.expiresAt > :now
                    )
                  )
            """)
    boolean existsActiveBookingForSeat(
            @Param("seatId") UUID seatId,
            @Param("eventId") UUID eventId,
            @Param("now") Instant now
    );
}