package com.travelconnect.notification.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Minimal event published by the booking-service when a booking is first created.
 * Can be used to send an acknowledgement notification to the traveler.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {

    private UUID bookingId;
    private UUID travelerId;
    private String traceId;
}
