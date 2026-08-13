package com.travelconnect.booking.dto.response;

import com.travelconnect.booking.domain.TripStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripResponse(
        UUID id,
        UUID travelerId,
        String name,
        String description,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        TripStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
