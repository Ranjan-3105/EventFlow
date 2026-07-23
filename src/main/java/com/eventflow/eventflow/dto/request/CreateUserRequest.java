package com.eventflow.eventflow.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateUserRequest(
        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @Pattern(
                regexp = "^[0-9]{10}$",
                message = "Phone number must contain exactly 10 digits"
        )
        @NotBlank(message = "Phone number is required")
        String phoneNumber,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth
) {}
