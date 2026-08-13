package com.travelconnect.integration.exception;

/**
 * Thrown when a supplier endpoint cannot be reached (connection refused,
 * timeout, or health check failure).
 */
public class SupplierUnavailableException extends RuntimeException {

    public SupplierUnavailableException(String supplierId) {
        super("Supplier unavailable: " + supplierId);
    }

    public SupplierUnavailableException(String supplierId, Throwable cause) {
        super("Supplier unavailable: " + supplierId, cause);
    }
}
