package com.travelconnect.traveler.service;

import com.travelconnect.traveler.domain.Traveler;
import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import com.travelconnect.traveler.dto.request.UpdateTravelerRequest;
import com.travelconnect.traveler.dto.response.TravelerResponse;
import com.travelconnect.traveler.exception.DuplicateTravelerException;
import com.travelconnect.traveler.exception.TravelerNotFoundException;
import com.travelconnect.traveler.mapper.TravelerMapper;
import com.travelconnect.traveler.repository.TravelerRepository;
import com.travelconnect.traveler.service.impl.TravelerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TravelerServiceImpl.
 *
 * @ExtendWith(MockitoExtension.class) — activates Mockito without needing a
 *   full Spring context. Tests run fast (no application startup).
 *
 * We mock the repository so the test doesn't touch a real database.
 * The goal is to test the SERVICE LOGIC (duplicate check, mapping, exception throwing),
 * not the database.
 *
 * @Nested classes group related tests for readability.
 * @DisplayName provides human-readable test names in IDE and CI reports.
 */
@ExtendWith(MockitoExtension.class)
class TravelerServiceTest {

    @Mock
    private TravelerRepository travelerRepository;

    @Mock
    private TravelerMapper travelerMapper;

    @InjectMocks
    private TravelerServiceImpl travelerService;

    private Traveler sampleTraveler;
    private TravelerResponse sampleResponse;
    private CreateTravelerRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleTraveler = Traveler.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .phone("+441234567890")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .nationality("British")
                .passportNumber("AB123456")
                .passportExpiry(LocalDate.of(2030, 5, 15))
                .build();

        sampleResponse = new TravelerResponse(
                sampleTraveler.getId(),
                "John", "Smith", "john.smith@example.com",
                "+441234567890", LocalDate.of(1990, 5, 15),
                "British", "AB123456", LocalDate.of(2030, 5, 15),
                null, null
        );

        createRequest = new CreateTravelerRequest(
                "John", "Smith", "john.smith@example.com",
                "+441234567890", LocalDate.of(1990, 5, 15),
                "British", "AB123456", LocalDate.of(2030, 5, 15)
        );
    }

    @Nested
    @DisplayName("createTraveler")
    class CreateTravelerTests {

        @Test
        @DisplayName("should create traveler successfully when email is unique")
        void shouldCreateTravelerSuccessfully() {
            when(travelerRepository.existsByEmail(any())).thenReturn(false);
            when(travelerMapper.toEntity(createRequest)).thenReturn(sampleTraveler);
            when(travelerRepository.save(sampleTraveler)).thenReturn(sampleTraveler);
            when(travelerMapper.toResponse(sampleTraveler)).thenReturn(sampleResponse);

            TravelerResponse result = travelerService.createTraveler(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("john.smith@example.com");
            verify(travelerRepository).save(sampleTraveler);
        }

        @Test
        @DisplayName("should throw DuplicateTravelerException when email already exists")
        void shouldThrowExceptionForDuplicateEmail() {
            when(travelerRepository.existsByEmail(any())).thenReturn(true);

            assertThatThrownBy(() -> travelerService.createTraveler(createRequest))
                    .isInstanceOf(DuplicateTravelerException.class)
                    .hasMessageContaining("john.smith@example.com");

            verify(travelerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getTravelerById")
    class GetTravelerByIdTests {

        @Test
        @DisplayName("should return traveler when found")
        void shouldReturnTravelerWhenFound() {
            UUID id = sampleTraveler.getId();
            when(travelerRepository.findById(id)).thenReturn(Optional.of(sampleTraveler));
            when(travelerMapper.toResponse(sampleTraveler)).thenReturn(sampleResponse);

            TravelerResponse result = travelerService.getTravelerById(id);

            assertThat(result.id()).isEqualTo(id);
        }

        @Test
        @DisplayName("should throw TravelerNotFoundException when not found")
        void shouldThrowExceptionWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(travelerRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> travelerService.getTravelerById(id))
                    .isInstanceOf(TravelerNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    @DisplayName("deleteTraveler")
    class DeleteTravelerTests {

        @Test
        @DisplayName("should delete traveler successfully when found")
        void shouldDeleteTravelerWhenFound() {
            UUID id = sampleTraveler.getId();
            when(travelerRepository.findById(id)).thenReturn(Optional.of(sampleTraveler));

            travelerService.deleteTraveler(id);

            verify(travelerRepository).delete(sampleTraveler);
        }

        @Test
        @DisplayName("should throw exception when traveler to delete does not exist")
        void shouldThrowExceptionWhenTravelerToDeleteNotFound() {
            UUID id = UUID.randomUUID();
            when(travelerRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> travelerService.deleteTraveler(id))
                    .isInstanceOf(TravelerNotFoundException.class);

            verify(travelerRepository, never()).delete(any());
        }
    }
}
