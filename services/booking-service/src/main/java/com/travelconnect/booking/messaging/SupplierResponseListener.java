package com.travelconnect.booking.messaging;

import com.travelconnect.booking.config.RabbitMQConfig;
import com.travelconnect.booking.messaging.event.SupplierResponseReceivedEvent;
import com.travelconnect.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SupplierResponseListener {

    private final BookingService bookingService;

    @RabbitListener(queues = RabbitMQConfig.SUPPLIER_RESPONSE_QUEUE)
    public void handleSupplierResponse(SupplierResponseReceivedEvent event) {
        log.info("Received SupplierResponseReceivedEvent: bookingId={}, supplierId={}, status={}, traceId={}",
                event.getBookingId(), event.getSupplierId(), event.getStatus(), event.getTraceId());
        try {
            bookingService.handleSupplierResponse(event);
            log.info("Supplier response processed successfully: bookingId={}", event.getBookingId());
        } catch (Exception ex) {
            log.error("Error processing supplier response: bookingId={}, error={}", event.getBookingId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
