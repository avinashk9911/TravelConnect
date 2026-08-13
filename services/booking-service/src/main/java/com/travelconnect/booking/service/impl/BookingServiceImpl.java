package com.travelconnect.booking.service.impl;

import com.travelconnect.booking.domain.Booking;
import com.travelconnect.booking.domain.BookingItem;
import com.travelconnect.booking.domain.BookingStatus;
import com.travelconnect.booking.domain.Trip;
import com.travelconnect.booking.dto.request.CreateBookingRequest;
import com.travelconnect.booking.dto.response.BookingResponse;
import com.travelconnect.booking.exception.BookingNotFoundException;
import com.travelconnect.booking.exception.TripNotFoundException;
import com.travelconnect.booking.mapper.BookingMapper;
import com.travelconnect.booking.messaging.BookingEventPublisher;
import com.travelconnect.booking.messaging.event.BookingCompletedEvent;
import com.travelconnect.booking.messaging.event.BookingCreatedEvent;
import com.travelconnect.booking.messaging.event.BookingItemEventData;
import com.travelconnect.booking.messaging.event.SupplierResponseReceivedEvent;
import com.travelconnect.booking.repository.BookingRepository;
import com.travelconnect.booking.repository.TripRepository;
import com.travelconnect.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final BookingMapper bookingMapper;
    private final BookingEventPublisher bookingEventPublisher;

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for tripId={}, travelerId={}", request.tripId(), request.travelerId());

        Trip trip = tripRepository.findById(request.tripId())
                .orElseThrow(() -> new TripNotFoundException(request.tripId()));

        String traceId = UUID.randomUUID().toString();
        String currency = (request.currency() != null && !request.currency().isBlank()) ? request.currency() : "GBP";

        Booking booking = Booking.builder()
                .trip(trip)
                .travelerId(request.travelerId())
                .currency(currency)
                .traceId(traceId)
                .build();

        List<BookingItem> items = request.items().stream()
                .map(itemRequest -> {
                    BookingItem item = bookingMapper.toBookingItem(itemRequest);
                    item.setBooking(booking);
                    return item;
                })
                .collect(Collectors.toList());

        booking.setItems(items);

        BigDecimal totalAmount = items.stream()
                .filter(item -> item.getPricePerUnit() != null)
                .map(item -> {
                    int qty = (item.getQuantity() != null) ? item.getQuantity() : 1;
                    int pax = (item.getPassengers() != null) ? item.getPassengers() : 1;
                    return item.getPricePerUnit().multiply(BigDecimal.valueOf((long) qty * pax));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        booking.setTotalAmount(totalAmount);

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created successfully: id={}, reference={}", saved.getId(), saved.getBookingReference());

        List<BookingItemEventData> itemEventData = saved.getItems().stream()
                .map(item -> BookingItemEventData.builder()
                        .itemType(item.getItemType() != null ? item.getItemType().name() : null)
                        .supplierCode(item.getSupplierCode())
                        .origin(item.getOrigin())
                        .destination(item.getDestination())
                        .departureDate(item.getDepartureDate() != null ? item.getDepartureDate().toString() : null)
                        .returnDate(item.getReturnDate() != null ? item.getReturnDate().toString() : null)
                        .passengers(item.getPassengers())
                        .pricePerUnit(item.getPricePerUnit())
                        .quantity(item.getQuantity())
                        .currency(item.getCurrency())
                        .build())
                .collect(Collectors.toList());

        BookingCreatedEvent event = BookingCreatedEvent.builder()
                .bookingId(saved.getId())
                .tripId(trip.getId())
                .travelerId(saved.getTravelerId())
                .traceId(traceId)
                .items(itemEventData)
                .build();

        bookingEventPublisher.publishBookingCreated(event);

        return bookingMapper.toBookingResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID id) {
        log.debug("Fetching booking by id: {}", id);
        Booking booking = findBookingById(id);
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference) {
        log.debug("Fetching booking by reference: {}", reference);
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new BookingNotFoundException(reference));
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getBookingsByTravelerId(UUID travelerId, Pageable pageable) {
        log.debug("Fetching bookings for travelerId={}", travelerId);
        return bookingRepository.findByTravelerId(travelerId, pageable)
                .map(bookingMapper::toBookingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByTripId(UUID tripId) {
        log.debug("Fetching bookings for tripId={}", tripId);
        return bookingRepository.findByTripId(tripId).stream()
                .map(bookingMapper::toBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingStatus(UUID id) {
        log.debug("Fetching booking status for id: {}", id);
        Booking booking = findBookingById(id);
        return bookingMapper.toBookingResponse(booking);
    }

    @Override
    public void handleSupplierResponse(SupplierResponseReceivedEvent event) {
        log.info("Handling supplier response: bookingId={}, status={}", event.getBookingId(), event.getStatus());

        Booking booking = findBookingById(event.getBookingId());

        if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
            booking.setStatus(BookingStatus.PROCESSING);
            bookingRepository.save(booking);

            long pendingCount = bookingRepository.countByTripIdAndStatusNot(
                    booking.getTrip().getId(), BookingStatus.CONFIRMED);

            if (pendingCount == 0) {
                booking.setStatus(BookingStatus.CONFIRMED);
                Booking confirmed = bookingRepository.save(booking);
                log.info("All supplier responses received — booking confirmed: id={}", confirmed.getId());

                BookingCompletedEvent completedEvent = BookingCompletedEvent.builder()
                        .bookingId(confirmed.getId())
                        .travelerId(confirmed.getTravelerId())
                        .bookingReference(confirmed.getBookingReference())
                        .totalAmount(confirmed.getTotalAmount())
                        .currency(confirmed.getCurrency())
                        .traceId(confirmed.getTraceId())
                        .completedAt(LocalDateTime.now().toString())
                        .supplierSummary(event.getResponseData())
                        .build();

                bookingEventPublisher.publishBookingCompleted(completedEvent);
            }
        } else if ("FAILED".equalsIgnoreCase(event.getStatus())) {
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            log.warn("Booking failed due to supplier response: id={}", booking.getId());
        } else {
            log.warn("Unknown supplier response status '{}' for bookingId={}", event.getStatus(), event.getBookingId());
        }
    }

    /** Private helper — avoids repeating the "findById + orElseThrow" pattern. */
    private Booking findBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
