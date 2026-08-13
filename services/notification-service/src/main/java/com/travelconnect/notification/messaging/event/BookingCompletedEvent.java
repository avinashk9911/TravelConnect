package com.travelconnect.notification.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published by the booking-service when a booking is fully completed.
 * Consumed by the notification-service to trigger emails and audit logging.
 */
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
