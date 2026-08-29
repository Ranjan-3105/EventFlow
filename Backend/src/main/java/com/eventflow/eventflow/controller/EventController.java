package com.eventflow.eventflow.controller;

import com.eventflow.eventflow.dto.request.CreateEventRequest;
import com.eventflow.eventflow.dto.response.EventResponse;
import com.eventflow.eventflow.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;
import com.eventflow.eventflow.dto.response.SeatAvailabilityResponse;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request
    ) {

        EventResponse response = eventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents() {
        return ResponseEntity.ok(eventService.getPublishedEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventService.getPublishedEvent(eventId));
    }

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<SeatAvailabilityResponse>> getEventSeats(
            @PathVariable UUID eventId
    ) {
        return ResponseEntity.ok(eventService.getEventSeats(eventId));
    }

    @PostMapping("/{eventId}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable UUID eventId
    ) {

        return ResponseEntity.ok(
                eventService.publishEvent(eventId)
        );
    }
}
