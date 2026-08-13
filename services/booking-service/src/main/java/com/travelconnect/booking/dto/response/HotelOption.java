package com.travelconnect.booking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HotelOption(
        String supplierCode,
        String hotelName,
        String city,
        LocalDate checkIn,
        LocalDate checkOut,
        String roomType,
        BigDecimal pricePerNight,
        String currency,
        Integer availableRooms
) {
}
