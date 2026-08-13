package com.travelconnect.integration.repository;

import com.travelconnect.integration.domain.IntegrationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IntegrationResponseRepository extends JpaRepository<IntegrationResponse, UUID> {

    List<IntegrationResponse> findByIntegrationRequestId(UUID integrationRequestId);
}
