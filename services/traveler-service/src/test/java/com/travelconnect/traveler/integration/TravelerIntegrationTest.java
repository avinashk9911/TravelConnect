package com.travelconnect.traveler.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test — tests the full request/response cycle against a REAL PostgreSQL database.
 *
 * @SpringBootTest — starts the complete Spring application context.
 * @AutoConfigureMockMvc — auto-configures MockMvc so we can fire HTTP requests.
 * @Testcontainers — manages Docker container lifecycle during the test run.
 *
 * Testcontainers starts a real PostgreSQL Docker container before the tests run
 * and stops it after. This means:
 * - The test runs against a real database (not H2 or mocks)
 * - Flyway migrations are applied just as they would be in production
 * - You catch schema/query bugs that H2 wouldn't catch
 *
 * Prerequisite: Docker must be running on the machine.
 *
 * @DynamicPropertySource — overrides the datasource URL/username/password
 * to point at the Testcontainers-managed PostgreSQL instance.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TravelerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("travelconnect_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("should create a traveler and retrieve it by ID")
    void shouldCreateAndRetrieveTraveler() throws Exception {
        CreateTravelerRequest request = new CreateTravelerRequest(
                "Jane", "Doe", "jane.doe@example.com",
                "+441234567891", LocalDate.of(1985, 3, 20),
                "British", "CD789012", LocalDate.of(2032, 3, 20)
        );

        // Create the traveler
        String responseBody = mockMvc.perform(post("/api/v1/travelers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract the ID from the response
        String id = objectMapper.readTree(responseBody).at("/data/id").asText();

        // Retrieve by ID
        mockMvc.perform(get("/api/v1/travelers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    @DisplayName("should return 409 when creating duplicate traveler email")
    void shouldReturn409ForDuplicateEmail() throws Exception {
        CreateTravelerRequest request = new CreateTravelerRequest(
                "Alice", "Johnson", "alice.johnson@example.com",
                null, null, null, null, null
        );

        // First creation — should succeed
        mockMvc.perform(post("/api/v1/travelers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second creation with same email — should conflict
        mockMvc.perform(post("/api/v1/travelers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
