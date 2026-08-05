package com.eventflow.eventflow.dto.response;

import java.util.UUID;

public record HallResponse(

        UUID id,

        int hallNumber,

        int capacity,

        UUID venueId

) {}