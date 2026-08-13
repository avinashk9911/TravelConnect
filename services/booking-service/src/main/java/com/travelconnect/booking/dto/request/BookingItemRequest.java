package com.travelconnect.booking.dto.request;

import com.travelconnect.booking.domain.BookingItemType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingItemRequest(
        @NotNull BookingItemType itemType,
        String supplierCode,
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate returnDate,
        Integer passengers,
        BigDecimal pricePerUnit,
        Integer quantity,
        String currency
) {
}
