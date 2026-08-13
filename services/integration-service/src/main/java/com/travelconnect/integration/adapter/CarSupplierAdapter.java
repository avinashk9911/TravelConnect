package com.travelconnect.integration.adapter;

import com.travelconnect.integration.model.SupplierBookingRequest;
import com.travelconnect.integration.model.SupplierBookingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Adapter for the Car Supplier SOAP/XML API.
 *
 * Builds a raw SOAP envelope as a string and POSTs it to the car supplier's
 * endpoint.  The response is a SOAP envelope which is parsed with simple
 * string extraction — sufficient for a portfolio demo; production code would
 * use JAXB or a SAX/StAX parser.
 *
 * Using SOAP here deliberately contrasts with the REST/JSON approach used by
 * the flight and hotel adapters, demonstrating the adapter pattern's ability to
 * hide protocol differences from the service layer.
 */
@Component
@Slf4j
public class CarSupplierAdapter implements SupplierAdapter {

    @Value("${suppliers.car.url:http://localhost:9003}")
    private String carSupplierUrl;

    private final RestTemplate restTemplate;

    public CarSupplierAdapter(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String getSupplierType() {
        return "CAR";
    }

    @Override
    public String getSupplierId() {
        return "CAR-SUPPLIER-01";
    }

    @Override
    public boolean isAvailable() {
        try {
            restTemplate.getForObject(carSupplierUrl + "/health", String.class);
            return true;
        } catch (Exception e) {
            log.warn("Car supplier health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public SupplierBookingResponse sendBookingRequest(SupplierBookingRequest request) {
        long start = System.currentTimeMillis();
        try {
            String soapRequest = buildSoapRequest(request);
            log.info("Sending SOAP request to car supplier: bookingId={}, traceId={}",
                request.getBookingId(), request.getTraceId());
            log.debug("SOAP request payload:\n{}", soapRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_XML);
            headers.set("SOAPAction", "urn:createCarBooking");

            HttpEntity<String> entity = new HttpEntity<>(soapRequest, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                carSupplierUrl + "/ws/car-booking",
                HttpMethod.POST, entity, String.class);

            log.debug("SOAP response:\n{}", response.getBody());
            long processingTimeMs = System.currentTimeMillis() - start;
            log.info("Car supplier responded: bookingId={}, httpStatus={}, processingTimeMs={}",
                request.getBookingId(), response.getStatusCode().value(), processingTimeMs);

            return parseSoapResponse(response.getBody(), processingTimeMs);

        } catch (Exception e) {
            log.error("Car supplier SOAP call failed: bookingId={}, error={}",
                request.getBookingId(), e.getMessage());
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

    private String buildSoapRequest(SupplierBookingRequest request) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                           xmlns:car="http://travelconnect.com/car-supplier">
                <soap:Header/>
                <soap:Body>
                    <car:CreateCarBookingRequest>
                        <car:bookingId>%s</car:bookingId>
                        <car:traceId>%s</car:traceId>
                        <car:pickupLocation>%s</car:pickupLocation>
                        <car:dropoffLocation>%s</car:dropoffLocation>
                        <car:pickupDate>%s</car:pickupDate>
                        <car:returnDate>%s</car:returnDate>
                        <car:driverCount>%d</car:driverCount>
                        <car:currency>%s</car:currency>
                        <car:supplierCode>%s</car:supplierCode>
                    </car:CreateCarBookingRequest>
                </soap:Body>
            </soap:Envelope>
            """.formatted(
                request.getBookingId(),
                request.getTraceId() != null ? request.getTraceId() : "",
                request.getOrigin() != null ? request.getOrigin() : "",
                request.getDestination() != null ? request.getDestination() : "",
                request.getDepartureDate() != null ? request.getDepartureDate() : "",
                request.getReturnDate() != null ? request.getReturnDate() : "",
                request.getPassengers(),
                request.getCurrency() != null ? request.getCurrency() : "GBP",
                request.getSupplierCode() != null ? request.getSupplierCode() : "");
    }

    private SupplierBookingResponse parseSoapResponse(String soapXml, long processingTimeMs) {
        boolean success = soapXml != null && soapXml.contains("<car:status>SUCCESS</car:status>");
        String refId   = extractXmlValue(soapXml, "car:referenceId");
        String price   = extractXmlValue(soapXml, "car:confirmedPrice");
        String errCode = extractXmlValue(soapXml, "car:errorCode");
        String errMsg  = extractXmlValue(soapXml, "car:errorMessage");

        return SupplierBookingResponse.builder()
                .success(success)
                .supplierId(getSupplierId())
                .supplierType(getSupplierType())
                .supplierReferenceId(refId)
                .status(success ? "CONFIRMED" : "FAILED")
                .confirmedPrice(price != null ? new BigDecimal(price) : null)
                .currency("GBP")
                .errorCode(errCode)
                .errorMessage(errMsg)
                .processingTimeMs(processingTimeMs)
                .build();
    }

    private String extractXmlValue(String xml, String tag) {
        if (xml == null) return null;
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start < 0 || end < 0) return null;
        return xml.substring(start + open.length(), end);
    }
}
