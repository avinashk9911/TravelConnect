package com.travelconnect.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTripRequest(
        @NotNull UUID travelerId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 255) String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
