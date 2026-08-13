package com.travelconnect.integration.adapter;

import com.travelconnect.integration.exception.SupplierUnavailableException;
import com.travelconnect.integration.model.SupplierBookingRequest;
import com.travelconnect.integration.model.SupplierBookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

/**
 * Adapter for the Flight Supplier REST API.
 *
 * Converts the internal {@link SupplierBookingRequest} into the JSON shape
 * expected by the flight supplier and maps the response back to the
 * canonical {@link SupplierBookingResponse}.
 */
@Component
@Slf4j
public class FlightSupplierAdapter implements SupplierAdapter {

    @Value("${suppliers.flight.url:http://localhost:9001}")
    private String flightSupplierUrl;

    private final RestTemplate restTemplate;

    public FlightSupplierAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getSupplierType() {
        return "FLIGHT";
    }

    @Override
    public String getSupplierId() {
        return "FLIGHT-SUPPLIER-01";
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(flightSupplierUrl + "/api/bookings/health", String.class);
            return true;
        } catch (Exception e) {
            log.warn("Flight supplier health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public SupplierBookingResponse sendBookingRequest(SupplierBookingRequest request) {
        long start = System.currentTimeMillis();
        log.info("Sending flight booking request to supplier: bookingId={}, traceId={}, route={}->{}",
            request.getBookingId(), request.getTraceId(), request.getOrigin(), request.getDestination());

        try {
            // Simulate realistic network variability (200-500 ms)
            Thread.sleep(200 + (long) (Math.random() * 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            Map<String, Object> requestBody = Map.of(
                "bookingId",     request.getBookingId().toString(),
                "traceId",       request.getTraceId() != null ? request.getTraceId() : "",
                "origin",        request.getOrigin() != null ? request.getOrigin() : "",
                "destination",   request.getDestination() != null ? request.getDestination() : "",
                "departureDate", request.getDepartureDate() != null ? request.getDepartureDate().toString() : "",
                "returnDate",    request.getReturnDate() != null ? request.getReturnDate().toString() : "",
                "passengers",    request.getPassengers(),
                "currency",      request.getCurrency() != null ? request.getCurrency() : "GBP",
                "supplierCode",  request.getSupplierCode() != null ? request.getSupplierCode() : ""
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                flightSupplierUrl + "/api/bookings", requestBody, Map.class);

            long processingTimeMs = System.currentTimeMillis() - start;
            log.info("Flight supplier responded: bookingId={}, httpStatus={}, processingTimeMs={}",
                request.getBookingId(), response.getStatusCode().value(), processingTimeMs);

            return mapResponse(response.getBody(), processingTimeMs);

        } catch (ResourceAccessException e) {
            log.error("Flight supplier unreachable: bookingId={}, error={}", request.getBookingId(), e.getMessage());
            throw new SupplierUnavailableException(getSupplierId(), e);
        } catch (Exception e) {
            log.error("Flight supplier call failed: bookingId={}, error={}", request.getBookingId(), e.getMessage());
            return SupplierBookingResponse.builder()
                    .success(false)
                    .supplierId(getSupplierId())
                    .supplierType(getSupplierType())
                    .errorCode("SUPPLIER_ERROR")
                    .errorMessage(e.getMessage())
                    .processingTimeMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    @SuppressWarnings("unchecked")
    private SupplierBookingResponse mapResponse(Map<?, ?> body, long processingTimeMs) {
        if (body == null) {
            return SupplierBookingResponse.builder()
                    .success(false)
                    .supplierId(getSupplierId())
                    .supplierType(getSupplierType())
                    .errorCode("EMPTY_RESPONSE")
                    .errorMessage("Supplier returned an empty response body")
                    .processingTimeMs(processingTimeMs)
                    .build();
        }

        String status = (String) body.get("status");
        boolean success = "CONFIRMED".equalsIgnoreCase(status);

        String priceStr = body.get("confirmedPrice") != null ? body.get("confirmedPrice").toString() : null;

        return SupplierBookingResponse.builder()
                .success(success)
                .supplierId(getSupplierId())
                .supplierType(getSupplierType())
                .supplierReferenceId((String) body.get("referenceId"))
                .status(status)
                .confirmedPrice(priceStr != null ? new BigDecimal(priceStr) : null)
                .currency((String) body.getOrDefault("currency", "GBP"))
                .errorCode((String) body.get("errorCode"))
                .errorMessage((String) body.get("errorMessage"))
                .processingTimeMs(processingTimeMs)
                .build();
    }
}
