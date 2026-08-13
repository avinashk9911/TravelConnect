package com.travelconnect.booking.controller;

import com.travelconnect.booking.dto.request.SearchRequest;
import com.travelconnect.booking.dto.response.ApiResponse;
import com.travelconnect.booking.dto.response.SearchResultResponse;
import com.travelconnect.booking.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for travel search endpoints.
 *
 * Accepts a search request and returns available flights, hotels and car options.
 * The current implementation returns mock data — the real implementation
 * would delegate to the Integration Service.
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * POST /api/v1/search
     *
     * Search for available travel options based on origin, destination and dates.
     * Clients can control which categories to include via includeFlights, includeHotels, includeCars.
     */
    @PostMapping
    public ApiResponse<SearchResultResponse> search(@Valid @RequestBody SearchRequest request) {
        SearchResultResponse result = searchService.search(request);
        return ApiResponse.success(result);
    }
}
