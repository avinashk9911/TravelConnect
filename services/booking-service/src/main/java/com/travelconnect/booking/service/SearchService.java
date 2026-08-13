package com.travelconnect.booking.service;

import com.travelconnect.booking.dto.request.SearchRequest;
import com.travelconnect.booking.dto.response.SearchResultResponse;

public interface SearchService {

    SearchResultResponse search(SearchRequest request);
}
