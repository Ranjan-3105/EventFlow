package com.eventflow.eventflow.dto.response;

import java.util.UUID;

public record GenerateSeatsResponse(

        UUID hallId,

        int totalSeats,

        String message

) {}