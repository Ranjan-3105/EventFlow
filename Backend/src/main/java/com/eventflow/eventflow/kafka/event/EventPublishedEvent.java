package com.eventflow.eventflow.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record EventPublishedEvent(
        UUID eventId,
        UUID organizerId,
        String organizerEmail,
        String eventName,
        Instant startTime,
        Instant timestamp
) {}