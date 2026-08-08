package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(

        @NotNull(message = "Event ID is required")
        UUID eventId,

        @NotEmpty(message = "At least one seat must be selected")
        List<UUID> seatIds

) {}
