package com.travelconnect.integration.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Published by integration-service after receiving a response from a supplier.
 * booking-service consumes this to update the booking status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseReceivedEvent {

    private UUID bookingId;
    private String traceId;
    private UUID integrationRequestId;

    private String supplierId;

    /** FLIGHT | HOTEL | CAR */
    private String supplierType;

    private boolean success;
    private String supplierReferenceId;
    private BigDecimal confirmedPrice;
    private String currency;

    private String errorCode;
    private String errorMessage;

    private long processingTimeMs;
}
