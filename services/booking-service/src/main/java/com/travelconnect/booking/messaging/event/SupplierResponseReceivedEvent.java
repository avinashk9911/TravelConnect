package com.travelconnect.booking.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseReceivedEvent {
    private UUID bookingId;
    private String supplierId;
    private String supplierType;
    private String status;
    private String responseData;
    private String traceId;
}
