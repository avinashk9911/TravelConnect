package com.travelconnect.traveler.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request body for PUT /api/v1/travelers/{id}.
 *
 * Note: email is intentionally excluded from updates.
 * Changing a traveler's email is a sensitive operation that should
 * go through a dedicated verification flow (out of scope for now).
 */
public record UpdateTravelerRequest(

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Phone must be a valid international format"
        )
        String phone,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Size(max = 100)
        String nationality,

        @Size(max = 50)
        String passportNumber,

        @Future(message = "Passport expiry must be a future date")
        LocalDate passportExpiry
) {}
