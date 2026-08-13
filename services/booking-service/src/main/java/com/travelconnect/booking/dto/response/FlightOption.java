package com.travelconnect.booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FlightOption(
        String supplierCode,
        String airline,
        String flightNumber,
        String origin,
        String destination,
        LocalDate departureDate,
        LocalDate arrivalDate,
        BigDecimal price,
        String currency,
        Integer availableSeats
) {
}
