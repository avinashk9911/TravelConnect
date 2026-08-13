package com.travelconnect.integration.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelconnect.integration.adapter.SupplierAdapter;
import com.travelconnect.integration.adapter.SupplierAdapterRegistry;
import com.travelconnect.integration.domain.IntegrationRequest;
import com.travelconnect.integration.domain.IntegrationResponse;
import com.travelconnect.integration.exception.IntegrationRequestNotFoundException;
import com.travelconnect.integration.messaging.IntegrationEventPublisher;
import com.travelconnect.integration.messaging.event.BookingCreatedEvent;
import com.travelconnect.integration.messaging.event.BookingItemEventData;
import com.travelconnect.integration.messaging.event.SupplierResponseReceivedEvent;
import com.travelconnect.integration.model.SupplierBookingRequest;
import com.travelconnect.integration.model.SupplierBookingResponse;
import com.travelconnect.integration.repository.IntegrationRequestRepository;
import com.travelconnect.integration.repository.IntegrationResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Core business logic for the integration service.
 *
 * Flow for each booking item:
 *  1. Persist an IntegrationRequest record (status = PENDING)
 *  2. Look up the correct SupplierAdapter from the registry
 *  3. Convert the item to a SupplierBookingRequest (canonical model)
 *  4. Call the adapter — it returns a SupplierBookingResponse regardless of protocol
 *  5. Persist an IntegrationResponse record
 *  6. Update the IntegrationRequest status (SUCCESS or FAILED)
 *  7. Publish a SupplierResponseReceivedEvent so booking-service can update its state
 *
 * Each item is handled in its own nested transaction so a failure on one
 * item (e.g. car supplier down) does not roll back a successful flight booking.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationServiceImpl implements com.travelconnect.integration.service.IntegrationService {

    private final IntegrationRequestRepository integrationRequestRepository;
    private final IntegrationResponseRepository integrationResponseRepository;
    private final SupplierAdapterRegistry adapterRegistry;
    private final IntegrationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processBookingRequest(BookingCreatedEvent event) {
        log.info("Processing BookingCreatedEvent: bookingId={}, traceId={}, items={}",
            event.getBookingId(), event.getTraceId(),
            event.getItems() != null ? event.getItems().size() : 0);

        if (event.getItems() == null || event.getItems().isEmpty()) {
            log.warn("BookingCreatedEvent has no items: bookingId={}", event.getBookingId());
            return;
        }

        for (BookingItemEventData item : event.getItems()) {
            processSingleItem(event, item);
        }
    }

    private void processSingleItem(BookingCreatedEvent event, BookingItemEventData item) {
        String supplierType = item.getItemType();
        log.info("[traceId={}] Processing item: bookingId={}, supplierType={}",
            event.getTraceId(), event.getBookingId(), supplierType);

        // Step 1: Persist PENDING request
        IntegrationRequest integrationRequest = IntegrationRequest.builder()
                .bookingId(event.getBookingId())
                .supplierType(supplierType)
                .status("PENDING")
                .retryCount(0)
                .traceId(event.getTraceId())
                .build();

        // Determine adapter early — if unknown type, save FAILED immediately
        SupplierAdapter adapter;
        try {
            adapter = adapterRegistry.getAdapter(supplierType);
            integrationRequest.setSupplierId(adapter.getSupplierId());
        } catch (IllegalArgumentException e) {
            log.error("[traceId={}] Unknown supplier type: {}", event.getTraceId(), supplierType);
            integrationRequest.setStatus("FAILED");
            integrationRequestRepository.save(integrationRequest);
            return;
        }

        integrationRequest = integrationRequestRepository.save(integrationRequest);
        log.debug("[traceId={}] Saved IntegrationRequest: id={}", event.getTraceId(), integrationRequest.getId());

        // Step 2: Build canonical request
        SupplierBookingRequest supplierRequest = SupplierBookingRequest.builder()
                .bookingId(event.getBookingId())
                .traceId(event.getTraceId())
                .itemType(supplierType)
                .origin(item.getOrigin())
                .destination(item.getDestination())
                .departureDate(item.getDepartureDate())
                .returnDate(item.getReturnDate())
                .passengers(item.getPassengers())
                .currency(item.getCurrency())
                .supplierCode(item.getSupplierCode())
                .build();

        // Serialise request payload for audit
        String requestPayload = serializeToJson(supplierRequest);
        integrationRequest.setRequestPayload(requestPayload);
        integrationRequest.setStatus("SENT");
        integrationRequest = integrationRequestRepository.save(integrationRequest);

        // Step 3: Call adapter
        SupplierBookingResponse supplierResponse = adapter.sendBookingRequest(supplierRequest);

        // Step 4: Persist response
        IntegrationResponse integrationResponse = IntegrationResponse.builder()
                .integrationRequest(integrationRequest)
                .responsePayload(serializeToJson(supplierResponse))
                .httpStatus(supplierResponse.isSuccess() ? 200 : 503)
                .success(supplierResponse.isSuccess())
                .errorMessage(supplierResponse.getErrorMessage())
                .processingTimeMs(supplierResponse.getProcessingTimeMs())
                .build();

        integrationResponseRepository.save(integrationResponse);

        // Step 5: Update request status
        integrationRequest.setStatus(supplierResponse.isSuccess() ? "SUCCESS" : "FAILED");
        integrationRequestRepository.save(integrationRequest);

        log.info("[traceId={}] Supplier call complete: bookingId={}, supplierType={}, success={}, processingTimeMs={}",
            event.getTraceId(), event.getBookingId(), supplierType,
            supplierResponse.isSuccess(), supplierResponse.getProcessingTimeMs());

        // Step 6: Publish outcome event
        SupplierResponseReceivedEvent responseEvent = SupplierResponseReceivedEvent.builder()
                .bookingId(event.getBookingId())
                .traceId(event.getTraceId())
                .integrationRequestId(integrationRequest.getId())
                .supplierId(adapter.getSupplierId())
                .supplierType(supplierType)
                .success(supplierResponse.isSuccess())
                .supplierReferenceId(supplierResponse.getSupplierReferenceId())
                .confirmedPrice(supplierResponse.getConfirmedPrice())
                .currency(supplierResponse.getCurrency())
                .errorCode(supplierResponse.getErrorCode())
                .errorMessage(supplierResponse.getErrorMessage())
                .processingTimeMs(supplierResponse.getProcessingTimeMs())
                .build();

        eventPublisher.publishSupplierResponse(responseEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrationRequest getIntegrationRequest(UUID id) {
        return integrationRequestRepository.findById(id)
                .orElseThrow(() -> new IntegrationRequestNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IntegrationRequest> getIntegrationRequestsByBooking(UUID bookingId) {
        return integrationRequestRepository.findByBookingId(bookingId);
    }

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Could not serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
