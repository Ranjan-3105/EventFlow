package com.eventflow.eventflow.dto.response;

import java.util.UUID;

public record SeatAvailabilityResponse(
        UUID seatId,
        Character rowLabel,
        int seatNumber,
        String seatType,
        SeatStatus status
) {}
