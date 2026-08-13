package com.travelconnect.booking.messaging;

import com.travelconnect.booking.config.RabbitMQConfig;
import com.travelconnect.booking.messaging.event.BookingCompletedEvent;
import com.travelconnect.booking.messaging.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishBookingCreated(BookingCreatedEvent event) {
        log.info("Publishing BookingCreatedEvent: bookingId={}, traceId={}", event.getBookingId(), event.getTraceId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.BOOKING_CREATED_ROUTING_KEY, event);
        log.info("BookingCreatedEvent published successfully: bookingId={}", event.getBookingId());
    }

    public void publishBookingCompleted(BookingCompletedEvent event) {
        log.info("Publishing BookingCompletedEvent: bookingId={}, reference={}", event.getBookingId(), event.getBookingReference());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.BOOKING_COMPLETED_ROUTING_KEY, event);
        log.info("BookingCompletedEvent published successfully: bookingId={}", event.getBookingId());
    }
}
