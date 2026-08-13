package com.travelconnect.traveler.controller;

import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import com.travelconnect.traveler.dto.request.UpdateTravelerRequest;
import com.travelconnect.traveler.dto.response.ApiResponse;
import com.travelconnect.traveler.dto.response.TravelerResponse;
import com.travelconnect.traveler.service.TravelerService;
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
 * REST controller for traveler endpoints.
 *
 * @RestController = @Controller + @ResponseBody
 *   Every method return value is serialised to JSON automatically.
 *
 * @RequestMapping sets the base path for all methods in this class.
 *
 * Versioning (/api/v1/...) is important — it lets you release breaking
 * changes as /api/v2/... without breaking existing clients.
 *
 * @RequiredArgsConstructor injects TravelerService via constructor.
 * This is preferred over @Autowired field injection because:
 * - The dependency is explicit and final
 * - Easier to unit test (you can pass a mock in the constructor)
 */
@RestController
@RequestMapping("/api/v1/travelers")
@RequiredArgsConstructor
@Slf4j
public class TravelerController {

    private final TravelerService travelerService;

    /**
     * POST /api/v1/travelers
     *
     * @Valid triggers bean validation on the request body.
     * If validation fails, MethodArgumentNotValidException is thrown
     * and handled by GlobalExceptionHandler (returns HTTP 400).
     *
     * @ResponseStatus(CREATED) sets the HTTP status to 201 on success.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TravelerResponse> createTraveler(
            @Valid @RequestBody CreateTravelerRequest request) {
        log.info("POST /api/v1/travelers - creating traveler: {}", request.email());
        TravelerResponse response = travelerService.createTraveler(request);
        return ApiResponse.success("Traveler created successfully", response);
    }

    /**
     * GET /api/v1/travelers/{id}
     *
     * Path variable {id} is bound to the UUID parameter.
     * Spring automatically converts the String "abc-123..." to UUID.
     */
    @GetMapping("/{id}")
    public ApiResponse<TravelerResponse> getTraveler(@PathVariable UUID id) {
        return ApiResponse.success(travelerService.getTravelerById(id));
    }

    /**
     * GET /api/v1/travelers?page=0&size=20&lastName=smith
     *
     * Pagination prevents loading thousands of records into memory.
     * We build the Pageable manually here so the controller controls
     * the default sort — lastName ascending.
     */
    @GetMapping
    public ApiResponse<Page<TravelerResponse>> getAllTravelers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String lastName) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastName").ascending());

        Page<TravelerResponse> result = (lastName != null && !lastName.isBlank())
                ? travelerService.searchByLastName(lastName, pageable)
                : travelerService.getAllTravelers(pageable);

        return ApiResponse.success(result);
    }

    /**
     * GET /api/v1/travelers/by-email?email=john@example.com
     */
    @GetMapping("/by-email")
    public ApiResponse<TravelerResponse> getTravelerByEmail(@RequestParam String email) {
        return ApiResponse.success(travelerService.getTravelerByEmail(email));
    }

    /**
     * PUT /api/v1/travelers/{id}
     *
     * Full update — replaces all updatable fields with the request values.
     */
    @PutMapping("/{id}")
    public ApiResponse<TravelerResponse> updateTraveler(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTravelerRequest request) {
        log.info("PUT /api/v1/travelers/{}", id);
        return ApiResponse.success("Traveler updated successfully", travelerService.updateTraveler(id, request));
    }

    /**
     * DELETE /api/v1/travelers/{id}
     *
     * Returns 204 No Content on success — no body needed for a deletion.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTraveler(@PathVariable UUID id) {
        log.info("DELETE /api/v1/travelers/{}", id);
        travelerService.deleteTraveler(id);
    }
}
