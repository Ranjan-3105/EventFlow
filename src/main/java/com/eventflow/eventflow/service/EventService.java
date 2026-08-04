package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateEventRequest;
import com.eventflow.eventflow.dto.response.EventResponse;
import com.eventflow.eventflow.entity.Event;
import com.eventflow.eventflow.entity.EventStatus;
import com.eventflow.eventflow.entity.Hall;
import com.eventflow.eventflow.entity.User;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.HallRepository;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final HallRepository hallRepository;
    private final UserRepository userRepository;


    public EventService(EventRepository eventRepository,
                        HallRepository hallRepository,
                        UserRepository userRepository) {

        this.eventRepository = eventRepository;
        this.hallRepository = hallRepository;
        this.userRepository = userRepository;
    }


    public EventResponse createEvent(CreateEventRequest request) {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User organizer = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("Organizer not found"));

        Hall hall = hallRepository
                .findById(request.hallId())
                .orElseThrow(() ->
                        new HallNotFoundException("Hall not found"));

        if (!request.startTime().isBefore(request.endTime())) {
            throw new InvalidEventTimeException(
                    "Start time must be before end time"
            );
        }

        boolean conflicting =
                eventRepository.existsConflictingEvent(
                        hall.getId(),
                        request.startTime(),
                        request.endTime()
                );

        if (conflicting) {
            throw new HallAlreadyBookedException(
                    "Hall is already booked for this time slot."
            );
        }

        Event event = new Event();

        event.setId(UUID.randomUUID());
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEndTime(request.endTime());
        event.setStartTime(request.startTime());
        event.setHall(hall);
        event.setOrganizer(organizer);
        event.setUpdatedAt(Instant.now());
        event.setCreatedAt(Instant.now());
        event.setStatus(EventStatus.DRAFT);
        event.setBannerUrl(request.bannerUrl());

        Event savedEvent = eventRepository.save(event);

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getTitle(),
                savedEvent.getStartTime(),
                savedEvent.getEndTime(),
                savedEvent.getStatus()
        );

    }
}

