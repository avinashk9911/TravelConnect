package com.travelconnect.booking.service.impl;

import com.travelconnect.booking.domain.Trip;
import com.travelconnect.booking.domain.TripStatus;
import com.travelconnect.booking.dto.request.CreateTripRequest;
import com.travelconnect.booking.dto.response.TripResponse;
import com.travelconnect.booking.exception.TripNotFoundException;
import com.travelconnect.booking.mapper.BookingMapper;
import com.travelconnect.booking.repository.TripRepository;
import com.travelconnect.booking.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final BookingMapper bookingMapper;

    @Override
    public TripResponse createTrip(CreateTripRequest request) {
        log.info("Creating trip for travelerId={}, destination={}", request.travelerId(), request.destination());
        Trip trip = bookingMapper.toTrip(request);
        Trip saved = tripRepository.save(trip);
        log.info("Trip created successfully: id={}", saved.getId());
        return bookingMapper.toTripResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(UUID id) {
        log.debug("Fetching trip by id: {}", id);
        Trip trip = findTripById(id);
        return bookingMapper.toTripResponse(trip);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> getTripsByTravelerId(UUID travelerId, Pageable pageable) {
        log.debug("Fetching trips for travelerId={}, page={}, size={}", travelerId, pageable.getPageNumber(), pageable.getPageSize());
        return tripRepository.findByTravelerId(travelerId, pageable)
                .map(bookingMapper::toTripResponse);
    }

    @Override
    public TripResponse updateTripStatus(UUID id, TripStatus status) {
        log.info("Updating trip status: id={}, newStatus={}", id, status);
        Trip trip = findTripById(id);
        trip.setStatus(status);
        Trip updated = tripRepository.save(trip);
        log.info("Trip status updated: id={}, status={}", id, status);
        return bookingMapper.toTripResponse(updated);
    }

    @Override
    public void deleteTrip(UUID id) {
        log.info("Deleting trip: id={}", id);
        Trip trip = findTripById(id);
        tripRepository.delete(trip);
        log.info("Trip deleted: id={}", id);
    }

    /** Private helper — avoids repeating the "findById + orElseThrow" pattern. */
    private Trip findTripById(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException(id));
    }
}
