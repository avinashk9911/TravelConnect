package com.travelconnect.booking.service;

import com.travelconnect.booking.dto.request.CreateBookingRequest;
import com.travelconnect.booking.dto.response.BookingResponse;
import com.travelconnect.booking.messaging.event.SupplierResponseReceivedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(UUID id);

    BookingResponse getBookingByReference(String reference);

    Page<BookingResponse> getBookingsByTravelerId(UUID travelerId, Pageable pageable);

    List<BookingResponse> getBookingsByTripId(UUID tripId);

    BookingResponse getBookingStatus(UUID id);

    void handleSupplierResponse(SupplierResponseReceivedEvent event);
}
