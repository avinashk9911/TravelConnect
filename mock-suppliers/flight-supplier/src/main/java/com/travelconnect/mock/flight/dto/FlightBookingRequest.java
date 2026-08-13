package com.travelconnect.mock.flight.dto;

import java.util.UUID;

/**
 * Request body received by the mock flight supplier.
 * Matches what FlightSupplierAdapter sends from integration-service.
 */
public record FlightBookingRequest(
        UUID bookingId,
        String traceId,
        String origin,
        String destination,
        String departureDate,
        String returnDate,
        int passengers,
        String currency,
        String supplierCode
) {}
