package com.travelconnect.integration.controller;

import com.travelconnect.integration.adapter.SupplierAdapterRegistry;
import com.travelconnect.integration.domain.IntegrationRequest;
import com.travelconnect.integration.dto.response.ApiResponse;
import com.travelconnect.integration.dto.response.IntegrationRequestResponse;
import com.travelconnect.integration.dto.response.SupplierStatusResponse;
import com.travelconnect.integration.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for querying integration state and supplier health.
 *
 * All mutation (sending requests to suppliers) is driven by RabbitMQ events,
 * not by HTTP.  This controller is purely a read-only inspection surface.
 */
@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;
    private final SupplierAdapterRegistry registry;

    /**
     * GET /api/v1/integrations/{id}
     * Returns the details of a single integration request.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IntegrationRequestResponse>> getIntegrationRequest(
            @PathVariable UUID id) {
        log.debug("GET /api/v1/integrations/{}", id);
        IntegrationRequest request = integrationService.getIntegrationRequest(id);
        return ResponseEntity.ok(ApiResponse.success(toResponse(request)));
    }

    /**
     * GET /api/v1/integrations/booking/{bookingId}
     * Returns all integration requests for a given booking.
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<IntegrationRequestResponse>>> getByBookingId(
            @PathVariable UUID bookingId) {
        log.debug("GET /api/v1/integrations/booking/{}", bookingId);
        List<IntegrationRequestResponse> responses = integrationService
                .getIntegrationRequestsByBooking(bookingId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * GET /api/v1/integrations/suppliers
     * Returns the live availability status of all registered supplier adapters.
     */
    @GetMapping("/suppliers")
    public ResponseEntity<ApiResponse<List<SupplierStatusResponse>>> getSupplierStatus() {
        log.debug("GET /api/v1/integrations/suppliers");
        List<SupplierStatusResponse> statuses = registry.getAllAdapters().stream()
                .map(a -> new SupplierStatusResponse(a.getSupplierId(), a.getSupplierType(), a.isAvailable()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(statuses));
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private IntegrationRequestResponse toResponse(IntegrationRequest request) {
        return new IntegrationRequestResponse(
                request.getId(),
                request.getBookingId(),
                request.getSupplierId(),
                request.getSupplierType(),
                request.getStatus(),
                request.getRetryCount(),
                request.getTraceId(),
                request.getCreatedAt()
        );
    }
}
