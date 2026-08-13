package com.travelconnect.mock.flight.controller;

import com.travelconnect.mock.flight.dto.FlightBookingRequest;
import com.travelconnect.mock.flight.dto.FlightBookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Mock flight supplier — simulates an external airline booking API.
 *
 * Behaviours designed to exercise integration-service resilience:
 *   - 200-800 ms random processing delay (realistic API latency)
 *   - 10% random failure rate (SEAT_UNAVAILABLE)
 *   - Reference ID derived from booking UUID prefix
 *
 * NOT for production use.
 */
@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class FlightBookingController {

    @PostMapping
    public ResponseEntity<FlightBookingResponse> createBooking(
            @RequestBody FlightBookingRequest request) {

        log.info("[MOCK FLIGHT SUPPLIER] Received booking request: bookingId={}, route={}->{}, passengers={}",
            request.bookingId(), request.origin(), request.destination(), request.passengers());

        // Simulate processing delay (200-800 ms)
        try {
            Thread.sleep(200 + (long) (Math.random() * 600));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate occasional supplier failures (10% chance)
        if (Math.random() < 0.1) {
            log.warn("[MOCK FLIGHT SUPPLIER] Simulating failure: bookingId={}", request.bookingId());
            return ResponseEntity.status(503).body(new FlightBookingResponse(
                null,
                request.bookingId(),
                "FAILED",
                null,
                "GBP",
                "SEAT_UNAVAILABLE",
                "No seats available on this flight"
            ));
        }

        String ref = "FL-" + request.bookingId().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK FLIGHT SUPPLIER] Booking confirmed: ref={}, bookingId={}", ref, request.bookingId());

        return ResponseEntity.ok(new FlightBookingResponse(
            ref,
            request.bookingId(),
            "CONFIRMED",
            new BigDecimal("299.99"),
            "GBP",
            null,
            null
        ));
    }

    @GetMapping("/health")
    public String health() {
        return "FLIGHT_SUPPLIER_OK";
    }
}
