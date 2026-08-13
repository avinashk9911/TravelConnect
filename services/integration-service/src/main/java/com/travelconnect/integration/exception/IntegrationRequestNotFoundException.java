package com.travelconnect.integration.exception;

import java.util.UUID;

public class IntegrationRequestNotFoundException extends RuntimeException {

    public IntegrationRequestNotFoundException(UUID id) {
        super("Integration request not found: " + id);
    }
}
