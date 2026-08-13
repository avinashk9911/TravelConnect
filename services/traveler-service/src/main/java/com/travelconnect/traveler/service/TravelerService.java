package com.travelconnect.traveler.service;

import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import com.travelconnect.traveler.dto.request.UpdateTravelerRequest;
import com.travelconnect.traveler.dto.response.TravelerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for traveler business logic.
 *
 * Why have an interface AND an implementation class?
 *
 * 1. The controller depends on the interface, not the concrete class.
 *    This allows you to swap implementations (e.g. mock for testing)
 *    without changing the controller.
 *
 * 2. Spring's @Transactional works via AOP proxies — Spring wraps your
 *    bean in a proxy at runtime. If the controller holds an interface
 *    reference, it transparently gets the proxy. This is the standard
 *    Spring pattern.
 *
 * 3. In interviews: "I define a service interface so the controller
 *    depends on an abstraction, not a concrete type. This makes the
 *    controller easier to test and the service easier to replace."
 */
public interface TravelerService {

    TravelerResponse createTraveler(CreateTravelerRequest request);

    TravelerResponse getTravelerById(UUID id);

    TravelerResponse getTravelerByEmail(String email);

    TravelerResponse updateTraveler(UUID id, UpdateTravelerRequest request);

    void deleteTraveler(UUID id);

    Page<TravelerResponse> getAllTravelers(Pageable pageable);

    Page<TravelerResponse> searchByLastName(String lastName, Pageable pageable);
}
