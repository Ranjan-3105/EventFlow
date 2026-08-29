package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull(message = "Booking ID is required")
        UUID bookingId

) {}