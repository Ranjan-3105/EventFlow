package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateHallRequest;
import com.eventflow.eventflow.dto.response.HallResponse;
import com.eventflow.eventflow.entity.Hall;
import com.eventflow.eventflow.entity.Venue;
import com.eventflow.eventflow.exception.DuplicateHallException;
import com.eventflow.eventflow.exception.VenueNotFoundException;
import com.eventflow.eventflow.repository.HallRepository;
import com.eventflow.eventflow.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class HallService {

    private final HallRepository hallRepository;
    private final VenueRepository venueRepository;

    public HallService(
            HallRepository hallRepository,
            VenueRepository venueRepository
    ) {
        this.hallRepository = hallRepository;
        this.venueRepository = venueRepository;
    }

    public HallResponse createHall(CreateHallRequest request) {

        System.out.println(">>> HallService reached");

        Venue venue = venueRepository
                .findById(request.venueId())
                .orElseThrow(() ->
                        new VenueNotFoundException("Venue not found"));

        System.out.println("Venue = " + venue.getId());

        boolean exists = hallRepository.existsByVenueIdAndHallNumber(
                request.venueId(),
                request.hallNumber()
        );

        if (exists) {
            throw new DuplicateHallException(
                    "Hall number already exists in this venue."
            );
        }

        Hall hall = new Hall();

        hall.setHallNumber(request.hallNumber());
        hall.setId(UUID.randomUUID());
        hall.setCapacity(request.capacity());
        hall.setVenue(venue);

        Instant now = Instant.now();
        hall.setCreatedAt(now);
        hall.setUpdatedAt(now);

        Hall savedHall = hallRepository.save(hall);

        return new HallResponse(
                savedHall.getId(),
                savedHall.getHallNumber(),
                savedHall.getCapacity(),
                savedHall.getVenue().getId()
        );

    }
}
