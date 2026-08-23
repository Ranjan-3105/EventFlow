package com.eventflow.eventflow.service;

import com.eventflow.eventflow.dto.request.CreateEventRequest;
import com.eventflow.eventflow.dto.response.EventResponse;
import com.eventflow.eventflow.dto.response.SeatAvailabilityResponse;
import com.eventflow.eventflow.dto.response.SeatStatus;
import com.eventflow.eventflow.entity.Event;
import com.eventflow.eventflow.entity.EventStatus;
import com.eventflow.eventflow.entity.Hall;
import com.eventflow.eventflow.entity.Seat;
import com.eventflow.eventflow.entity.User;
import com.eventflow.eventflow.exception.EventNotFoundException;
import com.eventflow.eventflow.exception.HallAlreadyBookedException;
import com.eventflow.eventflow.exception.HallNotFoundException;
import com.eventflow.eventflow.exception.InvalidEventTimeException;
import com.eventflow.eventflow.exception.UserNotFoundException;
import com.eventflow.eventflow.repository.BookingSeatRepository;
import com.eventflow.eventflow.repository.EventRepository;
import com.eventflow.eventflow.repository.HallRepository;
import com.eventflow.eventflow.repository.SeatRepository;
import com.eventflow.eventflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final HallRepository hallRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final SeatLockService seatLockService;
    private final EventCacheService eventCacheService;


    public EventService(EventRepository eventRepository,
                        HallRepository hallRepository,
                        UserRepository userRepository,
                        SeatRepository seatRepository,
                        BookingSeatRepository bookingSeatRepository,
                        SeatLockService seatLockService,
                        EventCacheService eventCacheService) {

        this.eventRepository = eventRepository;
        this.hallRepository = hallRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.seatLockService = seatLockService;
        this.eventCacheService = eventCacheService;
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
        event.setBasePrice(request.basePrice());
        event.setHall(hall);
        event.setOrganizer(organizer);
        event.setStatus(EventStatus.DRAFT);
        event.setBannerUrl(request.bannerUrl());
        Instant now = Instant.now();

        event.setUpdatedAt(now);
        event.setCreatedAt(now);

        Event savedEvent = eventRepository.save(event);

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getTitle(),
                savedEvent.getStartTime(),
                savedEvent.getEndTime(),
                savedEvent.getStatus(),
                savedEvent.getBasePrice()
        );

    }

    public List<EventResponse> getPublishedEvents() {

        // 1. Check Redis first
        List<EventResponse> cached =
                eventCacheService.getPublishedEvents();

        // 2. Cache HIT
        if (cached != null) {
            return cached;
        }

        // 3. Cache MISS → PostgreSQL
        List<EventResponse> events =
                eventRepository.findByStatus(EventStatus.PUBLISHED)
                        .stream()
                        .map(event -> new EventResponse(
                                event.getId(),
                                event.getTitle(),
                                event.getStartTime(),
                                event.getEndTime(),
                                event.getStatus(),
                                event.getBasePrice()
                        ))
                        .toList();

        // 4. Store result in Redis for 5 minutes
        eventCacheService.cachePublishedEvents(events);

        // 5. Return result
        return events;
    }

    public EventResponse getPublishedEvent(UUID eventId) {
        Event event = eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getStartTime(),
                event.getEndTime(),
                event.getStatus(),
                event.getBasePrice()
        );
    }

    public List<SeatAvailabilityResponse> getEventSeats(UUID eventId) {
        Event event = eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        List<Seat> allSeats = seatRepository.findByHallId(event.getHall().getId());

        List<UUID> seatIds = allSeats.stream().map(Seat::getId).toList();

        Instant now = Instant.now();
        List<UUID> bookedSeatIds = bookingSeatRepository.findActiveBookedSeatIds(eventId, now);

        Set<UUID> lockedSeatIds = seatLockService.getLockedSeatIds(eventId, seatIds);

        Set<UUID> bookedSeatIdsSet = new HashSet<>(bookedSeatIds);

        return allSeats.stream().map(seat -> {
            SeatStatus status = SeatStatus.AVAILABLE;
            if (bookedSeatIdsSet.contains(seat.getId())) {
                status = SeatStatus.BOOKED;
            } else if (lockedSeatIds.contains(seat.getId())) {
                status = SeatStatus.LOCKED;
            }

            return new SeatAvailabilityResponse(
                    seat.getId(),
                    seat.getRowLabel(),
                    seat.getSeatNumber(),
                    seat.getSeatType().name(),
                    status
            );
        }).toList();
    }

    public EventResponse publishEvent(UUID eventId) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Event event =
                eventRepository
                        .findByIdAndOrganizer_Email(eventId, email)
                        .orElseThrow(() ->
                                new EventNotFoundException(
                                        "Event not found"
                                )
                        );

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft events can be published"
            );
        }

        event.setStatus(EventStatus.PUBLISHED);
        event.setUpdatedAt(Instant.now());

        Event savedEvent =
                eventRepository.save(event);

        eventCacheService.invalidatePublishedEvents();

        return new EventResponse(
                savedEvent.getId(),
                savedEvent.getTitle(),
                savedEvent.getStartTime(),
                savedEvent.getEndTime(),
                savedEvent.getStatus(),
                savedEvent.getBasePrice()
        );
    }
}

