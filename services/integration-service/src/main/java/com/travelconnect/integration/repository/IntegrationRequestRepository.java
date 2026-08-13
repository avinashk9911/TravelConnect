package com.travelconnect.integration.repository;

import com.travelconnect.integration.domain.IntegrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntegrationRequestRepository extends JpaRepository<IntegrationRequest, UUID> {

    List<IntegrationRequest> findByBookingIdAndStatus(UUID bookingId, String status);

    List<IntegrationRequest> findByStatus(String status);

    List<IntegrationRequest> findByBookingId(UUID bookingId);

    long countByBookingId(UUID bookingId);
}
