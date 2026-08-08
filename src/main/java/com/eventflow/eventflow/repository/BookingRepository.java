package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BookingRepository
        extends JpaRepository<Booking, UUID> {

}
