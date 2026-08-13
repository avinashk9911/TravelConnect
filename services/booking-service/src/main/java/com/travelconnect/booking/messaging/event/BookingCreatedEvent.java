package com.travelconnect.booking.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
    private UUID bookingId;
    private UUID tripId;
    private UUID travelerId;
    private String traceId;
    private List<BookingItemEventData> items;
}
