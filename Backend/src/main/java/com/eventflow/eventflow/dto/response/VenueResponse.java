package com.eventflow.eventflow.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VenueResponse(

        UUID id,

        String name,

        String address,

        String city,

        String state,

        String country,

        Double latitude,

        Double longitude

) {}
