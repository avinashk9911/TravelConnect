package com.travelconnect.booking.controller;

import com.travelconnect.booking.dto.request.CreateBookingRequest;
import com.travelconnect.booking.dto.response.ApiResponse;
import com.travelconnect.booking.dto.response.BookingResponse;
import com.travelconnect.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for booking endpoints.
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method return value is serialised to JSON automatically.
 *
 * @RequiredArgsConstructor injects BookingService via constructor.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/v1/bookings
     *
     * @Valid triggers bean validation on the request body.
     * @ResponseStatus(CREATED) sets the HTTP status to 201 on success.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        log.info("POST /api/v1/bookings - creating booking: tripId={}, travelerId={}", request.tripId(), request.travelerId());
        BookingResponse response = bookingService.createBooking(request);
        return ApiResponse.success("Booking created successfully", response);
    }

    /**
     * GET /api/v1/bookings/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBookingById(@PathVariable UUID id) {
        log.debug("GET /api/v1/bookings/{}", id);
        return ApiResponse.success(bookingService.getBookingById(id));
    }

    /**
     * GET /api/v1/bookings/{id}/status
     *
     * Returns the current status of a booking — useful for polling.
     */
    @GetMapping("/{id}/status")
    public ApiResponse<BookingResponse> getBookingStatus(@PathVariable UUID id) {
        log.debug("GET /api/v1/bookings/{}/status", id);
        return ApiResponse.success(bookingService.getBookingStatus(id));
    }

    /**
     * GET /api/v1/bookings?travelerId={UUID}&page=0&size=20
     *
     * Returns a paginated list of bookings for the given traveler.
     */
    @GetMapping
    public ApiResponse<Page<BookingResponse>> getBookingsByTravelerId(
            @RequestParam UUID travelerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/v1/bookings?travelerId={}", travelerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ApiResponse.success(bookingService.getBookingsByTravelerId(travelerId, pageable));
    }

    /**
     * GET /api/v1/bookings/reference/{reference}
     *
     * Lookup a booking by its human-readable booking reference (e.g. TC-AB123456).
     */
    @GetMapping("/reference/{reference}")
    public ApiResponse<BookingResponse> getBookingByReference(@PathVariable String reference) {
        log.debug("GET /api/v1/bookings/reference/{}", reference);
        return ApiResponse.success(bookingService.getBookingByReference(reference));
    }
}
