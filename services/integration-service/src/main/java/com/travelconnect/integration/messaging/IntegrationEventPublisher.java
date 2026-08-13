package com.travelconnect.integration.messaging;

import com.travelconnect.integration.config.RabbitMQConfig;
import com.travelconnect.integration.messaging.event.SupplierResponseReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes supplier response events onto the RabbitMQ topic exchange.
 *
 * Routing key pattern: supplier.response.{SUPPLIER_TYPE}
 * e.g. supplier.response.FLIGHT, supplier.response.HOTEL, supplier.response.CAR
 *
 * booking-service binds its queue to this routing key to learn when a
 * supplier has confirmed or rejected a booking item.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntegrationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSupplierResponse(SupplierResponseReceivedEvent event) {
        String routingKey = "supplier.response." + event.getSupplierType();
        log.info("Publishing supplier response event: bookingId={}, supplierType={}, success={}, routingKey={}",
            event.getBookingId(), event.getSupplierType(), event.isSuccess(), routingKey);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);

        log.debug("Supplier response event published: integrationRequestId={}",
            event.getIntegrationRequestId());
    }
}
