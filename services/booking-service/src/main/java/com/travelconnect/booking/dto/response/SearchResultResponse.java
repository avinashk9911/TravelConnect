package com.travelconnect.booking.dto.response;

import java.time.LocalDate;
import java.util.List;

public record SearchResultResponse(
        String origin,
        String destination,
        LocalDate departureDate,
        List<FlightOption> flights,
        List<HotelOption> hotels,
        List<CarOption> cars
) {
}
