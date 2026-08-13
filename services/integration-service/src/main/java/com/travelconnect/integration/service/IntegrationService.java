package com.travelconnect.integration.service;

import com.travelconnect.integration.domain.IntegrationRequest;
import com.travelconnect.integration.messaging.event.BookingCreatedEvent;

import java.util.List;
import java.util.UUID;

public interface IntegrationService {

    /**
     * Processes a booking created event by dispatching each booking item to
     * its corresponding supplier adapter, persisting the request/response, and
     * publishing the outcome back onto the event bus.
     */
    void processBookingRequest(BookingCreatedEvent event);

    /**
     * Retrieves a single integration request by its ID.
     *
     * @throws com.travelconnect.integration.exception.IntegrationRequestNotFoundException if not found
     */
    IntegrationRequest getIntegrationRequest(UUID id);

    /**
     * Returns all integration requests associated with a booking.
     */
    List<IntegrationRequest> getIntegrationRequestsByBooking(UUID bookingId);
}
