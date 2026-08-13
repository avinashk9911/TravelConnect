package com.travelconnect.notification.service.impl;

import com.travelconnect.notification.messaging.event.BookingCompletedEvent;
import com.travelconnect.notification.service.AwsLambdaService;
import com.travelconnect.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Handles notification processing for completed bookings.
 *
 * Responsibilities:
 * 1. Log / simulate sending a booking confirmation email to the traveler.
 * 2. Forward the event to AWS Lambda for immutable audit logging in DynamoDB.
 *
 * Email sending is intentionally simulated with log statements here —
 * in a real project this would integrate with an email provider such as
 * AWS SES, SendGrid, or Mailgun.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final AwsLambdaService awsLambdaService;

    @Value("${aws.enabled:false}")
    private boolean awsEnabled;

    @Override
    public void processBookingCompleted(BookingCompletedEvent event) {
        log.info("Processing BookingCompleted event: bookingId={}, travelerId={}, traceId={}",
                event.getBookingId(), event.getTravelerId(), event.getTraceId());

        // Simulate sending email notification
        log.info("Sending booking confirmation email to traveler: travelerId={}, bookingRef={}",
                event.getTravelerId(), event.getBookingReference());

        // Trigger audit Lambda
        sendAuditToLambda(event);
    }

    @Override
    public void sendAuditToLambda(BookingCompletedEvent event) {
        // When running locally: log the audit record (Lambda not available)
        // When deployed to AWS: this invokes Lambda via AWS SDK
        if (awsEnabled) {
            awsLambdaService.invokeAuditLambda(event);
        } else {
            log.info("[LOCAL MODE] Would invoke AWS Lambda for audit: bookingId={}, amount={} {}",
                    event.getBookingId(), event.getTotalAmount(), event.getCurrency());
        }
    }
}
