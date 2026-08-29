package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.Hall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HallRepository extends JpaRepository<Hall, UUID> {

    boolean existsByVenueIdAndHallNumber(
            UUID venueId,
            int hallNumber
    );

}