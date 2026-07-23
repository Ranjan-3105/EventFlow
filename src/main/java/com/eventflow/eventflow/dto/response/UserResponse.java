package com.eventflow.eventflow.dto.response;

import com.eventflow.eventflow.entity.Role;

import java.util.UUID;

public record UserResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        String phoneNumber,

        Role role

) {}