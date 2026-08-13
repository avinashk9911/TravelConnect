package com.travelconnect.booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CarOption(
        String supplierCode,
        String carType,
        String pickupLocation,
        String dropoffLocation,
        LocalDate pickupDate,
        LocalDate returnDate,
        BigDecimal pricePerDay,
        String currency
) {
}
