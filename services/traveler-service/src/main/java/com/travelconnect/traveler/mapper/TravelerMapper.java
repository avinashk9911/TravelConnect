package com.travelconnect.traveler.mapper;

import com.travelconnect.traveler.domain.Traveler;
import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import com.travelconnect.traveler.dto.request.UpdateTravelerRequest;
import com.travelconnect.traveler.dto.response.TravelerResponse;
import org.springframework.stereotype.Component;

/**
 * Converts between the Traveler entity and its DTOs.
 *
 * We write this manually to keep things transparent and easy to debug.
 * In a larger project you might generate this with MapStruct (an annotation
 * processor that creates the implementation at compile time). MapStruct is
 * worth mentioning in an interview because it eliminates boilerplate mapping
 * code across dozens of entities.
 *
 * The key rule: mappers should be pure functions with no side effects.
 * No database calls, no business logic — just field-to-field copying.
 */
@Component
public class TravelerMapper {

    /** Entity → response DTO (used when returning data to the API caller) */
    public TravelerResponse toResponse(Traveler traveler) {
        return new TravelerResponse(
                traveler.getId(),
                traveler.getFirstName(),
                traveler.getLastName(),
                traveler.getEmail(),
                traveler.getPhone(),
                traveler.getDateOfBirth(),
                traveler.getNationality(),
                traveler.getPassportNumber(),
                traveler.getPassportExpiry(),
                traveler.getCreatedAt(),
                traveler.getUpdatedAt()
        );
    }

    /** Create request → new entity (used before saving a new traveler) */
    public Traveler toEntity(CreateTravelerRequest request) {
        return Traveler.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email().toLowerCase().trim())
                .phone(request.phone())
                .dateOfBirth(request.dateOfBirth())
                .nationality(request.nationality())
                .passportNumber(request.passportNumber())
                .passportExpiry(request.passportExpiry())
                .build();
    }

    /** Apply update request fields onto an existing entity */
    public void updateEntity(Traveler traveler, UpdateTravelerRequest request) {
        traveler.setFirstName(request.firstName());
        traveler.setLastName(request.lastName());
        traveler.setPhone(request.phone());
        traveler.setDateOfBirth(request.dateOfBirth());
        traveler.setNationality(request.nationality());
        traveler.setPassportNumber(request.passportNumber());
        traveler.setPassportExpiry(request.passportExpiry());
    }
}
