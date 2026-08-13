package com.travelconnect.booking.exception;

import java.util.UUID;

public class TripNotFoundException extends RuntimeException {

    public TripNotFoundException(UUID id) {
        super("Trip not found with id: " + id);
    }
}
