package com.travelconnect.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelconnect.booking.domain.BookingItemType;
import com.travelconnect.booking.domain.BookingStatus;
import com.travelconnect.booking.dto.request.BookingItemRequest;
import com.travelconnect.booking.dto.request.CreateBookingRequest;
import com.travelconnect.booking.dto.response.BookingResponse;
import com.travelconnect.booking.exception.BookingNotFoundException;
import com.travelconnect.booking.exception.GlobalExceptionHandler;
import com.travelconnect.booking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBooking_returns201() throws Exception {
        // Given
        UUID tripId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();

        BookingItemRequest itemRequest = new BookingItemRequest(
                BookingItemType.FLIGHT,
                "BA-001",
                "LHR",
                "CDG",
                LocalDate.now().plusDays(10),
                null,
                1,
                new BigDecimal("299.99"),
                1,
                "GBP"
        );

        CreateBookingRequest request = new CreateBookingRequest(tripId, travelerId, "GBP", List.of(itemRequest));

        BookingResponse mockResponse = new BookingResponse(
                bookingId, tripId, travelerId, "TC-ABCD1234",
                BookingStatus.PENDING, new BigDecimal("299.99"), "GBP",
                UUID.randomUUID().toString(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(bookingService.createBooking(any(CreateBookingRequest.class))).thenReturn(mockResponse);

        // When / Then
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("TC-ABCD1234"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getBookingById_returns200() throws Exception {
        // Given
        UUID bookingId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();

        BookingResponse mockResponse = new BookingResponse(
                bookingId, tripId, travelerId, "TC-ABCD1234",
                BookingStatus.CONFIRMED, new BigDecimal("299.99"), "GBP",
                UUID.randomUUID().toString(), List.of(),
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(bookingService.getBookingById(bookingId)).thenReturn(mockResponse);

        // When / Then
        mockMvc.perform(get("/api/v1/bookings/{id}", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(bookingId.toString()))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void getBookingById_returns404_whenNotFound() throws Exception {
        // Given
        UUID unknownId = UUID.randomUUID();
        when(bookingService.getBookingById(unknownId))
                .thenThrow(new BookingNotFoundException(unknownId));

        // When / Then
        mockMvc.perform(get("/api/v1/bookings/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Booking not found with id: " + unknownId));
    }
}
