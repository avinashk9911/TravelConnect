package com.travelconnect.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Supplier-agnostic internal representation of a booking item.
 *
 * Each adapter receives this canonical model and converts it into the
 * format required by the specific supplier (REST JSON, SOAP/XML, etc.).
 * This keeps adapter logic isolated and makes it trivial to add new suppliers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierBookingRequest {

    private UUID bookingId;
    private String traceId;

    /** FLIGHT | HOTEL | CAR */
    private String itemType;

    private String origin;
    private String destination;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private int passengers;
    private String currency;
    private String supplierCode;
}
