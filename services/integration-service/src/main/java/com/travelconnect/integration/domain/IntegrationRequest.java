package com.travelconnect.integration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Records every outbound call made to a supplier.
 *
 * One booking may generate multiple IntegrationRequests — one per item
 * (e.g. a flight + a hotel = 2 requests routed to two different suppliers).
 *
 * Lifecycle: PENDING → SENT → SUCCESS | FAILED | RETRYING
 */
@Entity
@Table(
    name = "integration_requests",
    indexes = {
        @Index(name = "idx_integration_req_booking_id", columnList = "booking_id"),
        @Index(name = "idx_integration_req_status",     columnList = "status"),
        @Index(name = "idx_integration_req_supplier_type", columnList = "supplier_type")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationRequest {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "supplier_id", length = 50)
    private String supplierId;

    @Column(name = "supplier_type", length = 20)
    private String supplierType;

    /** The raw payload (JSON or XML) sent to the supplier. */
    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    /** PENDING | SENT | SUCCESS | FAILED | RETRYING */
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /** Distributed trace ID propagated from the originating service. */
    @Column(name = "trace_id", length = 64)
    private String traceId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
