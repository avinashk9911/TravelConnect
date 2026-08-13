package com.travelconnect.booking.service;

import com.travelconnect.booking.domain.Booking;
import com.travelconnect.booking.domain.BookingStatus;
import com.travelconnect.booking.domain.Trip;
import com.travelconnect.booking.dto.request.BookingItemRequest;
import com.travelconnect.booking.dto.request.CreateBookingRequest;
import com.travelconnect.booking.dto.response.BookingResponse;
import com.travelconnect.booking.exception.BookingNotFoundException;
import com.travelconnect.booking.exception.TripNotFoundException;
import com.travelconnect.booking.mapper.BookingMapper;
import com.travelconnect.booking.messaging.BookingEventPublisher;
import com.travelconnect.booking.messaging.event.BookingCreatedEvent;
import com.travelconnect.booking.domain.BookingItemType;
import com.travelconnect.booking.repository.BookingRepository;
import com.travelconnect.booking.repository.TripRepository;
import com.travelconnect.booking.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_success() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();

        Trip trip = Trip.builder()
                .id(tripId)
                .travelerId(travelerId)
                .name("Business Trip to Paris")
                .destination("Paris")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(15))
                .build();

        BookingItemRequest itemRequest = new BookingItemRequest(
                BookingItemType.FLIGHT,
                "BA-001",
                "LHR",
                "CDG",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(15),
                1,
                new BigDecimal("299.99"),
                1,
                "GBP"
        );

        CreateBookingRequest request = new CreateBookingRequest(tripId, travelerId, "GBP", List.of(itemRequest));

        Booking savedBooking = Booking.builder()
                .id(UUID.randomUUID())
                .trip(trip)
                .travelerId(travelerId)
                .bookingReference("TC-ABCD1234")
                .status(BookingStatus.PENDING)
                .totalAmount(new BigDecimal("299.99"))
                .currency("GBP")
                .traceId(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        BookingResponse expectedResponse = new BookingResponse(
                savedBooking.getId(), tripId, travelerId, "TC-ABCD1234",
                BookingStatus.PENDING, new BigDecimal("299.99"), "GBP",
                savedBooking.getTraceId(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(bookingMapper.toBookingItem(itemRequest)).thenReturn(
                com.travelconnect.booking.domain.BookingItem.builder()
                        .itemType(BookingItemType.FLIGHT)
                        .pricePerUnit(new BigDecimal("299.99"))
                        .quantity(1)
                        .passengers(1)
                        .currency("GBP")
                        .build()
        );
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingMapper.toBookingResponse(savedBooking)).thenReturn(expectedResponse);

        // When
        BookingResponse result = bookingService.createBooking(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.bookingReference()).isEqualTo("TC-ABCD1234");
        assertThat(result.status()).isEqualTo(BookingStatus.PENDING);

        ArgumentCaptor<BookingCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(bookingEventPublisher).publishBookingCreated(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getTripId()).isEqualTo(tripId);
        assertThat(eventCaptor.getValue().getTravelerId()).isEqualTo(travelerId);
    }

    @Test
    void createBooking_tripNotFound() {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();

        BookingItemRequest itemRequest = new BookingItemRequest(
                BookingItemType.FLIGHT, "BA-001", "LHR", "CDG",
                LocalDate.now().plusDays(10), null, 1, new BigDecimal("299.99"), 1, "GBP"
        );

        CreateBookingRequest request = new CreateBookingRequest(tripId, travelerId, "GBP", List.of(itemRequest));

        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining(tripId.toString());

        verify(bookingRepository, never()).save(any());
        verify(bookingEventPublisher, never()).publishBookingCreated(any());
    }

    @Test
    void getBookingById_success() {
        // Given
        UUID bookingId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();

        Booking booking = Booking.builder()
                .id(bookingId)
                .travelerId(travelerId)
                .bookingReference("TC-EFGH5678")
                .status(BookingStatus.CONFIRMED)
                .currency("GBP")
                .createdAt(LocalDateTime.now())
                .build();

        BookingResponse expectedResponse = new BookingResponse(
                bookingId, UUID.randomUUID(), travelerId, "TC-EFGH5678",
                BookingStatus.CONFIRMED, null, "GBP", null,
                List.of(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingResponse(booking)).thenReturn(expectedResponse);

        // When
        BookingResponse result = bookingService.getBookingById(bookingId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(bookingId);
        assertThat(result.bookingReference()).isEqualTo("TC-EFGH5678");
        assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void getBookingById_notFound() {
        // Given
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining(bookingId.toString());
    }
}
