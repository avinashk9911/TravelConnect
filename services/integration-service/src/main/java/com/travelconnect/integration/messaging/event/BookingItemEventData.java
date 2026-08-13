package com.travelconnect.integration.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One line-item within a BookingCreatedEvent.
 * Each item maps to one outbound supplier call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemEventData {

    /** FLIGHT | HOTEL | CAR */
    private String itemType;

    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate;

    /** Number of travellers / drivers. */
    private int passengers;

    private String currency;
    private String supplierCode;
}
