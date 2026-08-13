package com.travelconnect.traveler.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Java record used as the request body for POST /api/v1/travelers.
 *
 * Records (Java 16+) are ideal for immutable DTOs:
 * - Compact syntax
 * - All fields are final
 * - Constructor, equals(), hashCode(), toString() generated automatically
 *
 * Validation annotations here are processed by Spring Validation
 * when @Valid is placed on the controller method parameter.
 */
public record CreateTravelerRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Phone must be a valid international format, e.g. +441234567890"
        )
        String phone,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 100, message = "Nationality must not exceed 100 characters")
        String nationality,

        @Size(max = 50, message = "Passport number must not exceed 50 characters")
        String passportNumber,

        @Future(message = "Passport expiry must be a future date")
        LocalDate passportExpiry
) {}
