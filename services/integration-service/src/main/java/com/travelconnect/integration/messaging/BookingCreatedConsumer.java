package com.travelconnect.integration.messaging;

import com.travelconnect.integration.messaging.event.BookingCreatedEvent;
import com.travelconnect.integration.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for new bookings published by booking-service and triggers the
 * outbound supplier integration flow for each booking item.
 *
 * Spring's @RabbitListener handles connection management, deserialization,
 * and retry (configured in application.yml under
 * spring.rabbitmq.listener.simple.retry).
 *
 * If all retry attempts are exhausted the message is discarded (dead-lettering
 * can be added later by configuring a DLQ binding in RabbitMQConfig).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCreatedConsumer {

    private final IntegrationService integrationService;

    @RabbitListener(queues = "booking.created.queue")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Received BookingCreatedEvent: bookingId={}, traceId={}, items={}",
            event.getBookingId(), event.getTraceId(),
            event.getItems() != null ? event.getItems().size() : 0);

        try {
            integrationService.processBookingRequest(event);
        } catch (Exception e) {
            log.error("Failed to process BookingCreatedEvent: bookingId={}, traceId={}, error={}",
                event.getBookingId(), event.getTraceId(), e.getMessage(), e);
            // Re-throw so Spring AMQP retry mechanism can attempt again
            throw e;
        }
    }
}
