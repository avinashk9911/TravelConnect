package com.travelconnect.booking.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemEventData {
    private String itemType;
    private String supplierCode;
    private String origin;
    private String destination;
    private String departureDate;
    private String returnDate;
    private Integer passengers;
    private BigDecimal pricePerUnit;
    private Integer quantity;
    private String currency;
}
