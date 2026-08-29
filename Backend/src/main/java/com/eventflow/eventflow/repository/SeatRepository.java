package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    boolean existsByHallId(UUID hallId);

    boolean existsByHallIdAndRowLabelAndSeatNumber(
            UUID hallId,
            Character rowLabel,
            int seatNumber
    );

    List<Seat> findByHallId(UUID hallId);
}
