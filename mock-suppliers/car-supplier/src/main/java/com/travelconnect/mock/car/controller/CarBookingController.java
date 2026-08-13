package com.travelconnect.mock.car.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Mock car supplier — accepts SOAP/XML envelopes and returns SOAP/XML responses.
 *
 * The endpoint deliberately accepts raw SOAP envelopes rather than using full
 * Spring WS @Endpoint processing, to keep the mock simple while still proving
 * that the integration-service can send and receive real SOAP messages.
 *
 * Behaviours:
 *   - 300-1000 ms random processing delay (car APIs are typically slower)
 *   - 20% random failure rate (higher than flight/hotel to make retry demos interesting)
 *   - Reference prefix CR-
 *
 * NOT for production use.
 */
@RestController
@Slf4j
public class CarBookingController {

    @PostMapping(
        path     = "/ws/car-booking",
        consumes = { MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE },
        produces = { MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE }
    )
    public ResponseEntity<String> createCarBooking(@RequestBody String soapRequest) {
        log.info("[MOCK CAR SUPPLIER] Received SOAP booking request");
        log.debug("[MOCK CAR SUPPLIER] SOAP payload:\n{}", soapRequest);

        // Simulate processing delay (300-1000 ms)
        try {
            Thread.sleep(300 + (long) (Math.random() * 700));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Extract bookingId from the SOAP envelope
        String bookingId = extractValue(soapRequest, "car:bookingId");
        String ref = "CR-" + (bookingId != null && bookingId.length() >= 8
                ? bookingId.substring(0, 8).toUpperCase()
                : "UNKNOWN");

        // Simulate occasional supplier failures (20% chance)
        if (Math.random() < 0.2) {
            log.warn("[MOCK CAR SUPPLIER] Simulating failure for bookingId={}", bookingId);
            return ResponseEntity.ok(buildSoapErrorResponse("CAR_UNAVAILABLE", "No cars available for the requested dates"));
        }

        log.info("[MOCK CAR SUPPLIER] Car booking confirmed: ref={}, bookingId={}", ref, bookingId);
        return ResponseEntity.ok(buildSoapSuccessResponse(ref, bookingId, "99.99"));
    }

    @GetMapping("/health")
    public String health() {
        return "CAR_SUPPLIER_OK";
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String extractValue(String xml, String tag) {
        if (xml == null) return null;
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start < 0 || end < 0) return null;
        return xml.substring(start + open.length(), end).trim();
    }

    private String buildSoapSuccessResponse(String refId, String bookingId, String price) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                           xmlns:car="http://travelconnect.com/car-supplier">
                <soap:Header/>
                <soap:Body>
                    <car:CreateCarBookingResponse>
                        <car:referenceId>%s</car:referenceId>
                        <car:bookingId>%s</car:bookingId>
                        <car:status>SUCCESS</car:status>
                        <car:confirmedPrice>%s</car:confirmedPrice>
                        <car:currency>GBP</car:currency>
                    </car:CreateCarBookingResponse>
                </soap:Body>
            </soap:Envelope>
            """.formatted(refId, bookingId != null ? bookingId : "", price);
    }

    private String buildSoapErrorResponse(String errorCode, String errorMessage) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                           xmlns:car="http://travelconnect.com/car-supplier">
                <soap:Header/>
                <soap:Body>
                    <car:CreateCarBookingResponse>
                        <car:status>FAILED</car:status>
                        <car:errorCode>%s</car:errorCode>
                        <car:errorMessage>%s</car:errorMessage>
                    </car:CreateCarBookingResponse>
                </soap:Body>
            </soap:Envelope>
            """.formatted(errorCode, errorMessage);
    }
}
