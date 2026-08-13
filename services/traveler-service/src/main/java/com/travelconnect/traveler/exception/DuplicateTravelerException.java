package com.travelconnect.traveler.exception;

/**
 * Thrown when a traveler with the given email already exists.
 * The GlobalExceptionHandler maps this to an HTTP 409 Conflict response.
 *
 * We check for duplicates at the service layer before the DB insert,
 * so the caller gets a clear business error rather than a raw
 * DataIntegrityViolationException with a cryptic message.
 */
public class DuplicateTravelerException extends RuntimeException {

    public DuplicateTravelerException(String email) {
        super("A traveler with email '" + email + "' already exists");
    }
}
