package com.travelconnect.integration.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology declarations for the integration service.
 *
 * These declarations are idempotent — RabbitMQ silently ignores attempts to
 * re-declare an exchange or queue that already exists with the same settings.
 * Both booking-service and integration-service declare the same topology; this
 * is intentional and ensures either service can start first without errors.
 *
 * Exchange: travelconnect.events (topic)
 * Queues consumed here:
 *   - booking.created.queue  (routing key: booking.created)
 * Queues published to from here:
 *   - supplier.response.FLIGHT / .HOTEL / .CAR  (routing key: supplier.response.{type})
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "travelconnect.events";

    public static final String BOOKING_CREATED_QUEUE     = "booking.created.queue";
    public static final String BOOKING_CREATED_ROUTING   = "booking.created";

    public static final String SUPPLIER_RESPONSE_FLIGHT_QUEUE   = "supplier.response.flight.queue";
    public static final String SUPPLIER_RESPONSE_HOTEL_QUEUE    = "supplier.response.hotel.queue";
    public static final String SUPPLIER_RESPONSE_CAR_QUEUE      = "supplier.response.car.queue";

    // ── Exchange ─────────────────────────────────────────────────────────────

    @Bean
    public TopicExchange travelconnectExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    // ── Queues ────────────────────────────────────────────────────────────────

    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(BOOKING_CREATED_QUEUE).build();
    }

    @Bean
    public Queue supplierResponseFlightQueue() {
        return QueueBuilder.durable(SUPPLIER_RESPONSE_FLIGHT_QUEUE).build();
    }

    @Bean
    public Queue supplierResponseHotelQueue() {
        return QueueBuilder.durable(SUPPLIER_RESPONSE_HOTEL_QUEUE).build();
    }

    @Bean
    public Queue supplierResponseCarQueue() {
        return QueueBuilder.durable(SUPPLIER_RESPONSE_CAR_QUEUE).build();
    }

    // ── Bindings ──────────────────────────────────────────────────────────────

    @Bean
    public Binding bookingCreatedBinding(Queue bookingCreatedQueue, TopicExchange travelconnectExchange) {
        return BindingBuilder
                .bind(bookingCreatedQueue)
                .to(travelconnectExchange)
                .with(BOOKING_CREATED_ROUTING);
    }

    @Bean
    public Binding supplierResponseFlightBinding(Queue supplierResponseFlightQueue,
                                                 TopicExchange travelconnectExchange) {
        return BindingBuilder
                .bind(supplierResponseFlightQueue)
                .to(travelconnectExchange)
                .with("supplier.response.FLIGHT");
    }

    @Bean
    public Binding supplierResponseHotelBinding(Queue supplierResponseHotelQueue,
                                                TopicExchange travelconnectExchange) {
        return BindingBuilder
                .bind(supplierResponseHotelQueue)
                .to(travelconnectExchange)
                .with("supplier.response.HOTEL");
    }

    @Bean
    public Binding supplierResponseCarBinding(Queue supplierResponseCarQueue,
                                              TopicExchange travelconnectExchange) {
        return BindingBuilder
                .bind(supplierResponseCarQueue)
                .to(travelconnectExchange)
                .with("supplier.response.CAR");
    }

    // ── Serialisation ─────────────────────────────────────────────────────────

    /**
     * Serialise/deserialise messages as JSON.
     * JavaTimeModule handles LocalDate, LocalDateTime etc.
     */
    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
