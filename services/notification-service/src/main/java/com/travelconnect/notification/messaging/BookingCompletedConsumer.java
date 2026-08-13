package com.travelconnect.notification.messaging;

import com.travelconnect.notification.config.RabbitMQConfig;
import com.travelconnect.notification.messaging.event.BookingCompletedEvent;
import com.travelconnect.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ consumer for BookingCompleted events.
 *
 * Spring AMQP's @RabbitListener handles:
 * - Deserializing the JSON message into a BookingCompletedEvent (via Jackson2JsonMessageConverter)
 * - Retry on failure (configured in application.yml: max 3 attempts, exponential back-off)
 * - Dead-letter routing after all retries are exhausted (see RabbitMQConfig)
 *
 * We re-throw exceptions so that Spring AMQP knows the message was not processed
 * successfully and can apply the retry/DLQ policy.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCompletedConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_COMPLETED_QUEUE)
    public void handleBookingCompleted(BookingCompletedEvent event) {
        log.info("Received BookingCompleted event: bookingId={}, traceId={}",
                event.getBookingId(), event.getTraceId());
        try {
            notificationService.processBookingCompleted(event);
            log.info("BookingCompleted event processed successfully: bookingId={}", event.getBookingId());
        } catch (Exception e) {
            log.error("Failed to process BookingCompleted event: bookingId={}, error={}",
                    event.getBookingId(), e.getMessage(), e);
            throw e; // re-throw so RabbitMQ retry/DLQ kicks in
        }
    }
}
