package com.travelconnect.integration.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Published by booking-service when a new booking is created.
 * Consumed by integration-service to dispatch each item to the
 * appropriate supplier adapter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {

    private UUID bookingId;
    private UUID travelerId;

    /** Propagated trace ID for correlating logs across services. */
    private String traceId;

    /** One entry per booking item (flight, hotel, car, etc.). */
    private List<BookingItemEventData> items;
}
