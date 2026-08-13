package com.travelconnect.booking.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID tripId,
        @NotNull UUID travelerId,
        String currency,
        @NotEmpty List<BookingItemRequest> items
) {
}
