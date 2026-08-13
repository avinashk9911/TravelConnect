package com.travelconnect.integration.dto.response;

/**
 * Live availability status of a registered supplier adapter.
 * Surfaced via GET /api/v1/integrations/suppliers.
 */
public record SupplierStatusResponse(
        String supplierId,
        String supplierType,
        boolean available
) {}
