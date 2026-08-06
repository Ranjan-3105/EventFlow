package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateHallRequest(

        @NotNull
        UUID venueId,

        @Positive
        int hallNumber
) {}
