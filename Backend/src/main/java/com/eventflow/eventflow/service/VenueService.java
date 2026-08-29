package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateVenueRequest;
import com.eventflow.eventflow.dto.response.VenueResponse;
import com.eventflow.eventflow.entity.User;
import com.eventflow.eventflow.entity.Venue;
import com.eventflow.eventflow.exception.UserNotFoundException;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.HallRepository;
import com.eventflow.eventflow.repository.UserRepository;
import com.eventflow.eventflow.repository.VenueRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    public VenueService(VenueRepository venueRepository,
                        UserRepository userRepository
    ) {
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
    }

    public VenueResponse createVenue(CreateVenueRequest request) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User admin = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("Admin not found"));

        Venue venue = new Venue();

        venue.setId(UUID.randomUUID());
        venue.setName(request.name());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        venue.setState(request.state());
        venue.setCountry(request.country());
        venue.setLatitude(request.latitude());
        venue.setLongitude(request.longitude());
        Instant now = Instant.now();

        venue.setCreatedAt(now);
        venue.setUpdatedAt(now);

        Venue savedVenue = venueRepository.save(venue);

        return new VenueResponse(
                savedVenue.getId(),
                savedVenue.getName(),
                savedVenue.getAddress(),
                savedVenue.getCity(),
                savedVenue.getState(),
                savedVenue.getCountry(),
                savedVenue.getLatitude(),
                savedVenue.getLongitude()
        );

    }
}
