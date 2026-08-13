package com.travelconnect.integration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores the raw response received from a supplier, linked back to the
 * originating IntegrationRequest.
 *
 * Keeping requests and responses in separate tables allows querying
 * "did we get a response?" independently of "what did we send?".
 */
@Entity
@Table(
    name = "integration_responses",
    indexes = {
        @Index(name = "idx_integration_resp_request_id", columnList = "integration_request_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationResponse {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integration_request_id", nullable = false)
    private IntegrationRequest integrationRequest;

    /** The raw response body (JSON or XML) returned by the supplier. */
    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** Round-trip time from sending the request to receiving the response. */
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @CreationTimestamp
    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;
}
