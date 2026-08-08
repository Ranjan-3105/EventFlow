package com.eventflow.eventflow.dto.response;

import com.eventflow.eventflow.entity.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String title,
        Instant startTime,
        Instant endTime,
        EventStatus status,
        BigDecimal basePrice
){}
