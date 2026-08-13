package com.travelconnect.mock.hotel.controller;

import com.travelconnect.mock.hotel.dto.HotelBookingRequest;
import com.travelconnect.mock.hotel.dto.HotelBookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Mock hotel supplier — simulates an external hotel booking API.
 *
 * Behaviours designed to exercise integration-service resilience:
 *   - 200-800 ms random processing delay (realistic API latency)
 *   - 15% random failure rate (ROOM_UNAVAILABLE) — slightly higher than flight
 *     to produce more interesting retry/failure scenarios
 *   - Price returned as per-night rate
 *
 * NOT for production use.
 */
@RestController
@RequestMapping("/api/bookings")
@Slf4j
public class HotelBookingController {

    @PostMapping
    public ResponseEntity<HotelBookingResponse> createBooking(
            @RequestBody HotelBookingRequest request) {

        log.info("[MOCK HOTEL SUPPLIER] Received booking request: bookingId={}, city={}, checkIn={}, checkOut={}, rooms={}",
            request.bookingId(), request.city(), request.checkIn(), request.checkOut(), request.roomCount());

        // Simulate processing delay (200-800 ms)
        try {
            Thread.sleep(200 + (long) (Math.random() * 600));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate occasional supplier failures (15% chance)
        if (Math.random() < 0.15) {
            log.warn("[MOCK HOTEL SUPPLIER] Simulating failure: bookingId={}", request.bookingId());
            return ResponseEntity.status(503).body(new HotelBookingResponse(
                null,
                request.bookingId(),
                "FAILED",
                null,
                "GBP",
                "ROOM_UNAVAILABLE",
                "No rooms available for the requested dates"
            ));
        }

        String ref = "HL-" + request.bookingId().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK HOTEL SUPPLIER] Booking confirmed: ref={}, bookingId={}", ref, request.bookingId());

        return ResponseEntity.ok(new HotelBookingResponse(
            ref,
            request.bookingId(),
            "CONFIRMED",
            new BigDecimal("149.99"),
            "GBP",
            null,
            null
        ));
    }

    @GetMapping("/health")
    public String health() {
        return "HOTEL_SUPPLIER_OK";
    }
}
