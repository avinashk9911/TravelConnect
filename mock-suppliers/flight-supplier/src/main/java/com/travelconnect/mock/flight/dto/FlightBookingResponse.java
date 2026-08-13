package com.travelconnect.mock.flight.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response returned by the mock flight supplier.
 * Parsed by FlightSupplierAdapter.mapResponse() in integration-service.
 */
public record FlightBookingResponse(
        String referenceId,
        UUID bookingId,
        String status,
        BigDecimal confirmedPrice,
        String currency,
        String errorCode,
        String errorMessage
) {}
