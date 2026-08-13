package com.travelconnect.traveler.exception;

import java.util.UUID;

/**
 * Thrown when a requested traveler does not exist in the database.
 * The GlobalExceptionHandler maps this to an HTTP 404 response.
 */
public class TravelerNotFoundException extends RuntimeException {

    public TravelerNotFoundException(UUID id) {
        super("Traveler not found with id: " + id);
    }

    public TravelerNotFoundException(String email) {
        super("Traveler not found with email: " + email);
    }
}
