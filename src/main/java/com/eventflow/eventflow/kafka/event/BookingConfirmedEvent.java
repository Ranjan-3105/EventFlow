package com.eventflow.eventflow.kafka.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingConfirmedEvent(
        UUID bookingId,
        UUID userId,
        String email,
        String eventName,
        List<String> seats,
        BigDecimal amount,
        Instant timestamp
) {}