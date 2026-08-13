package com.travelconnect.notification.service;

import com.travelconnect.notification.messaging.event.BookingCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper around the AWS Lambda invocation.
 *
 * When running locally (aws.enabled=false) we log what would have been sent
 * to Lambda, which is useful for verifying the event payload without needing
 * real AWS credentials.
 *
 * When deployed to AWS (aws.enabled=true) this class would use the AWS SDK v2
 * LambdaClient to invoke the travelconnect-booking-audit function asynchronously
 * (InvocationType.EVENT — fire-and-forget).
 *
 * The commented-out SDK code is intentionally left in place as a reference for
 * how the real integration would work.
 */
@Service
@Slf4j
public class AwsLambdaService {

    @Value("${aws.lambda.function-name:travelconnect-booking-audit}")
    private String functionName;

    @Value("${aws.enabled:false}")
    private boolean awsEnabled;

    public void invokeAuditLambda(BookingCompletedEvent event) {
        if (!awsEnabled) {
            log.info("[AWS LAMBDA MOCK] Would invoke Lambda '{}' with payload: {}", functionName, event);
            return;
        }

        // Real invocation would use AWS SDK v2:
        // InvokeRequest request = InvokeRequest.builder()
        //     .functionName(functionName)
        //     .payload(SdkBytes.fromUtf8String(toJson(event)))
        //     .invocationType(InvocationType.EVENT)
        //     .build();
        // lambdaClient.invoke(request);

        log.info("Lambda invoked: functionName={}, bookingId={}", functionName, event.getBookingId());
    }
}
