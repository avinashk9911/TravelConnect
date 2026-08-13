package com.travelconnect.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ topology for the notification service.
 *
 * Exchange:  travelconnect.events  (topic)
 * Queue:     booking.completed.queue  — receives BOOKING_COMPLETED events
 * DLQ:       booking.completed.queue.dlq  — receives messages that failed all retries
 * DLX:       travelconnect.events.dlx  (direct) — routes failed messages to DLQ
 *
 * Why a DLQ?
 * If the notification fails (e.g. external email service is down) and all
 * retry attempts are exhausted, we don't want to lose the message.
 * The DLQ holds it for manual inspection and reprocessing.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "travelconnect.events";
    public static final String BOOKING_COMPLETED_QUEUE = "booking.completed.queue";
    public static final String BOOKING_COMPLETED_DLQ = "booking.completed.queue.dlq";
    public static final String BOOKING_COMPLETED_ROUTING_KEY = "booking.completed";

    @Bean
    public TopicExchange travelConnectExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(EXCHANGE + ".dlx");
    }

    @Bean
    public Queue bookingCompletedQueue() {
        // Dead-letter arguments: when a message is rejected/expired, route it to the DLX
        return QueueBuilder.durable(BOOKING_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", EXCHANGE + ".dlx")
                .withArgument("x-dead-letter-routing-key", BOOKING_COMPLETED_DLQ)
                .build();
    }

    @Bean
    public Queue bookingCompletedDlq() {
        return new Queue(BOOKING_COMPLETED_DLQ, true);
    }

    @Bean
    public Binding bookingCompletedBinding() {
        return BindingBuilder.bind(bookingCompletedQueue())
                .to(travelConnectExchange())
                .with(BOOKING_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(bookingCompletedDlq())
                .to(deadLetterExchange())
                .with(BOOKING_COMPLETED_DLQ);
    }

    /**
     * Serialize/deserialize messages as JSON.
     * Without this, Spring AMQP defaults to Java serialization — fragile across services.
     */
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
