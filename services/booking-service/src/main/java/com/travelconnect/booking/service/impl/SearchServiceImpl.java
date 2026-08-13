package com.travelconnect.booking.service.impl;

import com.travelconnect.booking.dto.request.SearchRequest;
import com.travelconnect.booking.dto.response.*;
import com.travelconnect.booking.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock search service — returns realistic-looking hardcoded data.
 * The real implementation would delegate to the Integration Service,
 * which in turn calls external airline/hotel/car supplier APIs.
 */
@Service
@Slf4j
public class SearchServiceImpl implements SearchService {

    @Override
    public SearchResultResponse search(SearchRequest request) {
        log.info("Search request: origin={}, destination={}, departure={}, passengers={}, includeFlights={}, includeHotels={}, includeCars={}",
                request.origin(), request.destination(), request.departureDate(),
                request.passengers(), request.includeFlights(), request.includeHotels(), request.includeCars());

        List<FlightOption> flights = request.includeFlights() ? buildMockFlights(request) : Collections.emptyList();
        List<HotelOption> hotels = request.includeHotels() ? buildMockHotels(request) : Collections.emptyList();
        List<CarOption> cars = request.includeCars() ? buildMockCars(request) : Collections.emptyList();

        log.info("Search completed: {} flights, {} hotels, {} cars found", flights.size(), hotels.size(), cars.size());

        return new SearchResultResponse(
                request.origin(),
                request.destination(),
                request.departureDate(),
                flights,
                hotels,
                cars
        );
    }

    private List<FlightOption> buildMockFlights(SearchRequest request) {
        List<FlightOption> flights = new ArrayList<>();
        flights.add(new FlightOption(
                "BA-001",
                "British Airways",
                "BA" + request.origin().toUpperCase() + request.destination().toUpperCase() + "001",
                request.origin(),
                request.destination(),
                request.departureDate(),
                request.departureDate().plusDays(1),
                new BigDecimal("349.99"),
                "GBP",
                42
        ));
        flights.add(new FlightOption(
                "LH-002",
                "Lufthansa",
                "LH" + request.origin().toUpperCase() + request.destination().toUpperCase() + "002",
                request.origin(),
                request.destination(),
                request.departureDate(),
                request.departureDate().plusDays(1),
                new BigDecimal("289.50"),
                "GBP",
                15
        ));
        flights.add(new FlightOption(
                "EK-003",
                "Emirates",
                "EK" + request.origin().toUpperCase() + request.destination().toUpperCase() + "003",
                request.origin(),
                request.destination(),
                request.departureDate(),
                request.departureDate().plusDays(1),
                new BigDecimal("415.00"),
                "GBP",
                8
        ));
        return flights;
    }

    private List<HotelOption> buildMockHotels(SearchRequest request) {
        List<HotelOption> hotels = new ArrayList<>();
        hotels.add(new HotelOption(
                "HTL-GRAND-001",
                "The Grand Hotel",
                request.destination(),
                request.departureDate(),
                request.returnDate() != null ? request.returnDate() : request.departureDate().plusDays(3),
                "Standard Double",
                new BigDecimal("159.00"),
                "GBP",
                5
        ));
        hotels.add(new HotelOption(
                "HTL-PLAZA-002",
                "City Plaza Hotel",
                request.destination(),
                request.departureDate(),
                request.returnDate() != null ? request.returnDate() : request.departureDate().plusDays(3),
                "Superior King",
                new BigDecimal("210.00"),
                "GBP",
                12
        ));
        hotels.add(new HotelOption(
                "HTL-EXECUTIVE-003",
                "Executive Suites",
                request.destination(),
                request.departureDate(),
                request.returnDate() != null ? request.returnDate() : request.departureDate().plusDays(3),
                "Business Suite",
                new BigDecimal("320.00"),
                "GBP",
                3
        ));
        return hotels;
    }

    private List<CarOption> buildMockCars(SearchRequest request) {
        List<CarOption> cars = new ArrayList<>();
        cars.add(new CarOption(
                "CAR-AVIS-001",
                "Economy",
                request.destination() + " Airport",
                request.destination() + " Airport",
                request.departureDate(),
                request.returnDate() != null ? request.returnDate() : request.departureDate().plusDays(3),
                new BigDecimal("45.00"),
                "GBP"
        ));
        cars.add(new CarOption(
                "CAR-HERTZ-002",
                "Compact SUV",
                request.destination() + " Airport",
                request.destination() + " City Centre",
                request.departureDate(),
                request.returnDate() != null ? request.returnDate() : request.departureDate().plusDays(3),
                new BigDecimal("75.00"),
                "GBP"
        ));
        return cars;
    }
}
