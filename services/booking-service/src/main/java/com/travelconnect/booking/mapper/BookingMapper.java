package com.travelconnect.booking.mapper;

import com.travelconnect.booking.domain.Booking;
import com.travelconnect.booking.domain.BookingItem;
import com.travelconnect.booking.domain.Trip;
import com.travelconnect.booking.dto.request.BookingItemRequest;
import com.travelconnect.booking.dto.request.CreateTripRequest;
import com.travelconnect.booking.dto.response.BookingItemResponse;
import com.travelconnect.booking.dto.response.BookingResponse;
import com.travelconnect.booking.dto.response.TripResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public TripResponse toTripResponse(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getTravelerId(),
                trip.getName(),
                trip.getDescription(),
                trip.getDestination(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getStatus(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }

    public BookingResponse toBookingResponse(Booking booking) {
        List<BookingItemResponse> itemResponses = (booking.getItems() == null)
                ? Collections.emptyList()
                : booking.getItems().stream()
                        .map(this::toBookingItemResponse)
                        .collect(Collectors.toList());

        return new BookingResponse(
                booking.getId(),
                booking.getTrip() != null ? booking.getTrip().getId() : null,
                booking.getTravelerId(),
                booking.getBookingReference(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getCurrency(),
                booking.getTraceId(),
                itemResponses,
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }

    public BookingItemResponse toBookingItemResponse(BookingItem item) {
        return new BookingItemResponse(
                item.getId(),
                item.getItemType(),
                item.getSupplierCode(),
                item.getOrigin(),
                item.getDestination(),
                item.getDepartureDate(),
                item.getReturnDate(),
                item.getPassengers(),
                item.getPricePerUnit(),
                item.getQuantity(),
                item.getCurrency()
        );
    }

    public Trip toTrip(CreateTripRequest request) {
        return Trip.builder()
                .travelerId(request.travelerId())
                .name(request.name())
                .description(request.description())
                .destination(request.destination())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
    }

    public BookingItem toBookingItem(BookingItemRequest request) {
        return BookingItem.builder()
                .itemType(request.itemType())
                .supplierCode(request.supplierCode())
                .origin(request.origin())
                .destination(request.destination())
                .departureDate(request.departureDate())
                .returnDate(request.returnDate())
                .passengers(request.passengers() != null ? request.passengers() : 1)
                .pricePerUnit(request.pricePerUnit())
                .quantity(request.quantity() != null ? request.quantity() : 1)
                .currency(request.currency() != null ? request.currency() : "GBP")
                .build();
    }
}
