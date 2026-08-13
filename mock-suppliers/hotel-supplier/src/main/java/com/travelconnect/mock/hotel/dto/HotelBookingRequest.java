package com.travelconnect.mock.hotel.dto;

import java.util.UUID;

/**
 * Request body received by the mock hotel supplier.
 * Matches what HotelSupplierAdapter sends from integration-service.
 */
public record HotelBookingRequest(
        UUID bookingId,
        String traceId,
        String checkIn,
        String checkOut,
        String city,
        String hotelCode,
        int roomCount,
        String currency
) {}
