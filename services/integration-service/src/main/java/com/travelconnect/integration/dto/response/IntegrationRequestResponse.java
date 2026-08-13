package com.travelconnect.integration.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-only projection of an IntegrationRequest for API responses.
 * Omits internal fields such as requestPayload that are unsuitable for external exposure.
 */
public record IntegrationRequestResponse(
        UUID id,
        UUID bookingId,
        String supplierId,
        String supplierType,
        String status,
        Integer retryCount,
        String traceId,
        LocalDateTime createdAt
) {}
