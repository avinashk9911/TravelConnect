package com.travelconnect.booking.exception;

import java.util.UUID;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(UUID id) {
        super("Booking not found with id: " + id);
    }

    public BookingNotFoundException(String reference) {
        super("Booking not found with reference: " + reference);
    }
}
