package com.travelconnect.booking.dto.response;

import com.travelconnect.booking.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID tripId,
        UUID travelerId,
        String bookingReference,
        BookingStatus status,
        BigDecimal totalAmount,
        String currency,
        String traceId,
        List<BookingItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
