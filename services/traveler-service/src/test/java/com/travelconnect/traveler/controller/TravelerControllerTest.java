package com.travelconnect.traveler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import com.travelconnect.traveler.dto.response.TravelerResponse;
import com.travelconnect.traveler.exception.GlobalExceptionHandler;
import com.travelconnect.traveler.exception.TravelerNotFoundException;
import com.travelconnect.traveler.service.TravelerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice test using @WebMvcTest.
 *
 * @WebMvcTest(TravelerController.class) — starts ONLY the web layer:
 *   - Spring MVC infrastructure (DispatcherServlet, request mapping)
 *   - Jackson serialisation
 *   - Validation
 *   Does NOT start: JPA, database, service beans.
 *
 * This makes the test fast — we test the HTTP layer in isolation.
 *
 * @MockBean — creates a Mockito mock and registers it as a Spring bean,
 *   replacing the real TravelerService. We control what it returns per test.
 *
 * MockMvc — lets us fire fake HTTP requests and assert on the response
 *   without starting a real HTTP server.
 */
@WebMvcTest(TravelerController.class)
@Import(GlobalExceptionHandler.class)
class TravelerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TravelerService travelerService;

    private static final UUID TRAVELER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    @Test
    @DisplayName("POST /api/v1/travelers - should return 201 and traveler on success")
    void shouldCreateTraveler() throws Exception {
        CreateTravelerRequest request = new CreateTravelerRequest(
                "John", "Smith", "john.smith@example.com",
                "+441234567890", LocalDate.of(1990, 5, 15),
                "British", "AB123456", LocalDate.of(2030, 5, 15)
        );

        TravelerResponse response = new TravelerResponse(
                TRAVELER_ID, "John", "Smith", "john.smith@example.com",
                "+441234567890", LocalDate.of(1990, 5, 15),
                "British", "AB123456", LocalDate.of(2030, 5, 15),
                LocalDateTime.now(), null
        );

        when(travelerService.createTraveler(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/travelers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("john.smith@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.message").value("Traveler created successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/travelers - should return 400 when request is invalid")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        CreateTravelerRequest invalidRequest = new CreateTravelerRequest(
                "John", "Smith", "not-a-valid-email",   // invalid email
                null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/travelers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.email").exists());
    }

    @Test
    @DisplayName("GET /api/v1/travelers/{id} - should return traveler when found")
    void shouldReturnTravelerById() throws Exception {
        TravelerResponse response = new TravelerResponse(
                TRAVELER_ID, "John", "Smith", "john.smith@example.com",
                null, null, null, null, null, LocalDateTime.now(), null
        );
        when(travelerService.getTravelerById(TRAVELER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/v1/travelers/{id}", TRAVELER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(TRAVELER_ID.toString()));
    }

    @Test
    @DisplayName("GET /api/v1/travelers/{id} - should return 404 when not found")
    void shouldReturn404WhenTravelerNotFound() throws Exception {
        when(travelerService.getTravelerById(TRAVELER_ID))
                .thenThrow(new TravelerNotFoundException(TRAVELER_ID));

        mockMvc.perform(get("/api/v1/travelers/{id}", TRAVELER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Traveler not found with id: " + TRAVELER_ID));
    }

    @Test
    @DisplayName("POST /api/v1/travelers - should return 400 when required fields are missing")
    void shouldReturn400WhenRequiredFieldsMissing() throws Exception {
        String requestBody = "{}"; // Empty JSON object

        mockMvc.perform(post("/api/v1/travelers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.firstName").exists())
                .andExpect(jsonPath("$.data.lastName").exists())
                .andExpect(jsonPath("$.data.email").exists());
    }
}
