package com.travelconnect.notification.service;

import com.travelconnect.notification.messaging.event.BookingCompletedEvent;

/**
 * Notification service contract.
 *
 * Separating interface from implementation allows easy unit-testing via mocks
 * and keeps the consumer (BookingCompletedConsumer) decoupled from impl details.
 */
public interface NotificationService {

    /**
     * Process a BookingCompleted event: send email notification and trigger audit Lambda.
     *
     * @param event the completed booking details
     */
    void processBookingCompleted(BookingCompletedEvent event);

    /**
     * Forward the booking event to the AWS Lambda audit function.
     * In local mode this is a no-op log; in AWS mode it invokes the real Lambda.
     *
     * @param event the completed booking details
     */
    void sendAuditToLambda(BookingCompletedEvent event);
}
