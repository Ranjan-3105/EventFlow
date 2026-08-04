package com.eventflow.eventflow.repository;

import com.eventflow.eventflow.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository
        extends JpaRepository<Venue, UUID> {

}
