package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.Positive;

public record GenerateSeatsRequest(

        @Positive(message = "Rows must be greater than 0")
        int rows,

        @Positive(message = "Seats per row must be greater than 0")
        int seatsPerRow

) {}