package com.travelconnect.booking.controller;

import com.travelconnect.booking.dto.request.CreateTripRequest;
import com.travelconnect.booking.dto.response.ApiResponse;
import com.travelconnect.booking.dto.response.TripResponse;
import com.travelconnect.booking.service.TripService;
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
 * REST controller for trip endpoints.
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method return value is serialised to JSON automatically.
 *
 * @RequiredArgsConstructor injects TripService via constructor.
 * This is preferred over @Autowired field injection because the dependency
 * is explicit and final, making unit testing straightforward.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final TripService tripService;

    /**
     * POST /api/v1/trips
     *
     * @Valid triggers bean validation on the request body.
     * @ResponseStatus(CREATED) sets the HTTP status to 201 on success.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TripResponse> createTrip(@Valid @RequestBody CreateTripRequest request) {
        log.info("POST /api/v1/trips - creating trip: destination={}, travelerId={}", request.destination(), request.travelerId());
        TripResponse response = tripService.createTrip(request);
        return ApiResponse.success("Trip created successfully", response);
    }

    /**
     * GET /api/v1/trips/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<TripResponse> getTripById(@PathVariable UUID id) {
        log.debug("GET /api/v1/trips/{}", id);
        return ApiResponse.success(tripService.getTripById(id));
    }

    /**
     * GET /api/v1/trips?travelerId={UUID}&page=0&size=20
     *
     * Returns a paginated list of trips for the given traveler.
     */
    @GetMapping
    public ApiResponse<Page<TripResponse>> getTripsByTravelerId(
            @RequestParam UUID travelerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/v1/trips?travelerId={}", travelerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        return ApiResponse.success(tripService.getTripsByTravelerId(travelerId, pageable));
    }

    /**
     * DELETE /api/v1/trips/{id}
     *
     * Returns 204 No Content on success — no body needed for a deletion.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrip(@PathVariable UUID id) {
        log.info("DELETE /api/v1/trips/{}", id);
        tripService.deleteTrip(id);
    }
}
