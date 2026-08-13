package com.travelconnect.mock.hotel.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response returned by the mock hotel supplier.
 * Parsed by HotelSupplierAdapter.mapResponse() in integration-service.
 */
public record HotelBookingResponse(
        String referenceId,
        UUID bookingId,
        String status,
        BigDecimal confirmedPricePerNight,
        String currency,
        String errorCode,
        String errorMessage
) {}
