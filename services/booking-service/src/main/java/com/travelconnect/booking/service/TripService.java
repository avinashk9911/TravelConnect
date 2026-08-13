package com.travelconnect.booking.service;

import com.travelconnect.booking.domain.TripStatus;
import com.travelconnect.booking.dto.request.CreateTripRequest;
import com.travelconnect.booking.dto.response.TripResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TripService {

    TripResponse createTrip(CreateTripRequest request);

    TripResponse getTripById(UUID id);

    Page<TripResponse> getTripsByTravelerId(UUID travelerId, Pageable pageable);

    TripResponse updateTripStatus(UUID id, TripStatus status);

    void deleteTrip(UUID id);
}
