package com.travelconnect.booking.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCompletedEvent {
    private UUID bookingId;
    private UUID travelerId;
    private String bookingReference;
    private BigDecimal totalAmount;
    private String currency;
    private String traceId;
    private String completedAt;
    private String supplierSummary;
}
