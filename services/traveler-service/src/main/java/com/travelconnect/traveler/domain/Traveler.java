package com.travelconnect.traveler.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity representing a corporate traveler.
 *
 * Rule: never expose this class directly through a REST API.
 * Use TravelerResponse DTO instead. This keeps your API contract
 * independent of your database schema — if you rename a column,
 * the API doesn't break.
 */
@Entity
@Table(
    name = "travelers",
    indexes = {
        @Index(name = "idx_traveler_email", columnList = "email"),
        @Index(name = "idx_traveler_last_name", columnList = "last_name")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Traveler {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    /*
     * unique = true creates a DB-level UNIQUE constraint.
     * We also validate uniqueness in the service layer before inserting,
     * so we get a meaningful error message rather than a raw DB constraint violation.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "passport_number", length = 50)
    private String passportNumber;

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    /*
     * @CreationTimestamp / @UpdateTimestamp are Hibernate-specific.
     * They set the value automatically on insert/update at the ORM layer,
     * before hitting the database.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
