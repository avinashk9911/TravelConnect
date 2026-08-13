package com.travelconnect.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "travelconnect.events";
    public static final String BOOKING_CREATED_QUEUE = "booking.created.queue";
    public static final String SUPPLIER_RESPONSE_QUEUE = "supplier.response.queue";
    public static final String BOOKING_COMPLETED_QUEUE = "booking.completed.queue";
    public static final String BOOKING_CREATED_ROUTING_KEY = "booking.created";
    public static final String SUPPLIER_RESPONSE_ROUTING_KEY = "supplier.response.#";
    public static final String BOOKING_COMPLETED_ROUTING_KEY = "booking.completed";
    public static final String DLQ_SUFFIX = ".dlq";

    @Bean
    public TopicExchange travelConnectExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue bookingCreatedQueue() {
        return QueueBuilder.durable(BOOKING_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", BOOKING_CREATED_QUEUE + DLQ_SUFFIX)
                .build();
    }

    @Bean
    public Queue supplierResponseQueue() {
        return new Queue(SUPPLIER_RESPONSE_QUEUE, true);
    }

    @Bean
    public Queue bookingCompletedQueue() {
        return new Queue(BOOKING_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding bookingCreatedBinding() {
        return BindingBuilder.bind(bookingCreatedQueue()).to(travelConnectExchange()).with(BOOKING_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding supplierResponseBinding() {
        return BindingBuilder.bind(supplierResponseQueue()).to(travelConnectExchange()).with(SUPPLIER_RESPONSE_ROUTING_KEY);
    }

    @Bean
    public Binding bookingCompletedBinding() {
        return BindingBuilder.bind(bookingCompletedQueue()).to(travelConnectExchange()).with(BOOKING_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
