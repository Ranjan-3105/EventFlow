package com.eventflow.eventflow.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String firstName,
        String email,
        Instant timestamp
) {}