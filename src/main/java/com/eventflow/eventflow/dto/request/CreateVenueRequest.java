package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVenueRequest(

        @NotBlank(message = "name cannot be blank")
        String name,

        @NotBlank(message = "address cannot be blank")
        String address,

        @NotBlank(message = "city cannot be blank")
        String city,

        @NotBlank(message = "state cannot be blank")
        String state,

        @NotBlank(message = "country cannot be blank")
        String country,

        @NotNull
        Double latitude,

        @NotNull
        Double longitude

) {}
