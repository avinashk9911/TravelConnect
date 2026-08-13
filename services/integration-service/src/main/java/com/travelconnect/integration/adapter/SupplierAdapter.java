package com.travelconnect.integration.adapter;

import com.travelconnect.integration.model.SupplierBookingRequest;
import com.travelconnect.integration.model.SupplierBookingResponse;

/**
 * Strategy interface for supplier adapters.
 *
 * Each adapter knows how to talk to ONE specific supplier using whatever
 * protocol that supplier requires (REST/JSON, SOAP/XML, etc.).
 * The service layer is completely decoupled from those details — it only
 * ever calls this interface.
 *
 * Adding a new supplier = implementing this interface + registering the bean.
 * No other code changes required.
 */
public interface SupplierAdapter {

    /** Returns the item type this adapter handles: FLIGHT, HOTEL, or CAR. */
    String getSupplierType();

    /** Returns the supplier's unique identifier (e.g. "FLIGHT-SUPPLIER-01"). */
    String getSupplierId();

    /**
     * Sends a booking request to the supplier and returns the canonical response.
     *
     * @param request supplier-agnostic booking request
     * @return canonical response (success or failure) — never throws
     */
    SupplierBookingResponse sendBookingRequest(SupplierBookingRequest request);

    /** Returns {@code true} if the supplier endpoint is reachable. */
    boolean isAvailable();
}
