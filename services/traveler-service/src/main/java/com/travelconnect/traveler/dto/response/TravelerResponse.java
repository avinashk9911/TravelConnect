package com.travelconnect.traveler.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * What the API returns when a caller asks for traveler data.
 *
 * This is deliberately separate from the Traveler entity:
 * - We control exactly what fields are exposed (e.g. we might hide passportNumber
 *   from non-admin callers in a future security update)
 * - The entity can change without breaking the API contract
 */
public record TravelerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        LocalDate dateOfBirth,
        String nationality,
        String passportNumber,
        LocalDate passportExpiry,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
