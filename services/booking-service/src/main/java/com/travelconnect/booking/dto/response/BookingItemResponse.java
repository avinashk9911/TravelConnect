package com.travelconnect.booking.dto.response;

import com.travelconnect.booking.domain.BookingItemType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record BookingItemResponse(
        UUID id,
        BookingItemType itemType,
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
