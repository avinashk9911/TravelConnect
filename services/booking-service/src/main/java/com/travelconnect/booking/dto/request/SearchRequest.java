package com.travelconnect.booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SearchRequest(
        @NotBlank String origin,
        @NotBlank String destination,
        @NotNull LocalDate departureDate,
        LocalDate returnDate,
        @Min(1) int passengers,
        boolean includeFlights,
        boolean includeHotels,
        boolean includeCars
) {
}
