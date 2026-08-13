package com.travelconnect.booking.repository;

import com.travelconnect.booking.domain.Booking;
import com.travelconnect.booking.domain.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByTravelerId(UUID travelerId, Pageable pageable);

    List<Booking> findByTripId(UUID tripId);

    long countByTripIdAndStatusNot(UUID tripId, BookingStatus status);
}
