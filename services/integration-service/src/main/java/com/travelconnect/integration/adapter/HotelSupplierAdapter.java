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
 * Adapter for the Hotel Supplier REST API.
 *
 * Hotels use check-in / check-out dates rather than departure / return dates,
 * so the field mapping differs slightly from the flight adapter.
 */
@Component
@Slf4j
public class HotelSupplierAdapter implements SupplierAdapter {

    @Value("${suppliers.hotel.url:http://localhost:9002}")
    private String hotelSupplierUrl;

    private final RestTemplate restTemplate;

    public HotelSupplierAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getSupplierType() {
        return "HOTEL";
    }

    @Override
    public String getSupplierId() {
        return "HOTEL-SUPPLIER-01";
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(hotelSupplierUrl + "/api/bookings/health", String.class);
            return true;
        } catch (Exception e) {
            log.warn("Hotel supplier health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public SupplierBookingResponse sendBookingRequest(SupplierBookingRequest request) {
        long start = System.currentTimeMillis();
        log.info("Sending hotel booking request to supplier: bookingId={}, traceId={}, city={}",
            request.getBookingId(), request.getTraceId(), request.getDestination());

        try {
            Thread.sleep(200 + (long) (Math.random() * 300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            Map<String, Object> requestBody = Map.of(
                "bookingId",  request.getBookingId().toString(),
                "traceId",    request.getTraceId() != null ? request.getTraceId() : "",
                "checkIn",    request.getDepartureDate() != null ? request.getDepartureDate().toString() : "",
                "checkOut",   request.getReturnDate() != null ? request.getReturnDate().toString() : "",
                "city",       request.getDestination() != null ? request.getDestination() : "",
                "hotelCode",  request.getSupplierCode() != null ? request.getSupplierCode() : "",
                "roomCount",  request.getPassengers(),
                "currency",   request.getCurrency() != null ? request.getCurrency() : "GBP"
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                hotelSupplierUrl + "/api/bookings", requestBody, Map.class);

            long processingTimeMs = System.currentTimeMillis() - start;
            log.info("Hotel supplier responded: bookingId={}, httpStatus={}, processingTimeMs={}",
                request.getBookingId(), response.getStatusCode().value(), processingTimeMs);

            return mapResponse(response.getBody(), processingTimeMs);

        } catch (ResourceAccessException e) {
            log.error("Hotel supplier unreachable: bookingId={}, error={}", request.getBookingId(), e.getMessage());
            throw new SupplierUnavailableException(getSupplierId(), e);
        } catch (Exception e) {
            log.error("Hotel supplier call failed: bookingId={}, error={}", request.getBookingId(), e.getMessage());
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

        String priceStr = body.get("confirmedPricePerNight") != null
                ? body.get("confirmedPricePerNight").toString() : null;

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
