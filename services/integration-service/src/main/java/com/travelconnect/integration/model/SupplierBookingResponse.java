package com.travelconnect.integration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Supplier-agnostic internal representation of a supplier's booking confirmation
 * (or failure).  Each adapter maps the supplier-specific response into this
 * canonical structure so the rest of the service never knows which supplier
 * protocol was used.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierBookingResponse {

    private boolean success;
    private String supplierReferenceId;
    private String supplierId;
    private String supplierType;

    /** CONFIRMED | FAILED | PENDING */
    private String status;

    private BigDecimal confirmedPrice;
    private String currency;

    private String errorCode;
    private String errorMessage;

    /** Time from sending the request to receiving the response, in milliseconds. */
    private long processingTimeMs;
}
