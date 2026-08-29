package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateEventRequest(

        @NotBlank
        String title,

        @NotBlank
        String description,

        String bannerUrl,

        @NotNull
        Instant startTime,

        @NotNull
        Instant endTime,

        @NotNull
        @PositiveOrZero
        BigDecimal basePrice,

        @NotNull
        UUID hallId

) {}
