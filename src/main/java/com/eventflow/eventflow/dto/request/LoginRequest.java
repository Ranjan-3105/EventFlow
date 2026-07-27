package com.eventflow.eventflow.dto.request;

public record LoginRequest(
        String email,
        String password
){}
