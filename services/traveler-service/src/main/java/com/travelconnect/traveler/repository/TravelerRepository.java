package com.travelconnect.traveler.repository;

import com.travelconnect.traveler.domain.Traveler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the Traveler entity.
 *
 * By extending JpaRepository<Traveler, UUID> we get standard CRUD methods
 * for free (no implementation needed):
 *   save(), findById(), findAll(), deleteById(), count(), existsById() etc.
 *
 * Spring Data generates the SQL from the method name at startup time.
 * e.g. findByEmail → SELECT * FROM travelers WHERE email = ?
 *
 * This is one of Spring Boot's most powerful features — zero boilerplate
 * for standard queries.
 */
public interface TravelerRepository extends JpaRepository<Traveler, UUID> {

    /** Used to check for duplicate emails before insert */
    Optional<Traveler> findByEmail(String email);

    /** Useful for admin searches */
    boolean existsByEmail(String email);

    /**
     * Search by partial last name match (case-insensitive).
     *
     * When method name-based queries get complex, use @Query with JPQL.
     * JPQL uses entity class names and field names (not table/column names),
     * so it stays independent of the database schema.
     */
    @Query("SELECT t FROM Traveler t WHERE LOWER(t.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    Page<Traveler> findByLastNameContainingIgnoreCase(@Param("lastName") String lastName, Pageable pageable);

    /**
     * Count travelers by nationality — useful for a dashboard.
     *
     * This is a native SQL query (nativeQuery = true) for demonstration.
     * Use native queries sparingly — they bypass JPA's entity mapping
     * and are harder to maintain when the schema changes.
     */
    @Query(value = "SELECT nationality, COUNT(*) as count FROM travelers GROUP BY nationality ORDER BY count DESC",
           nativeQuery = true)
    java.util.List<Object[]> countByNationality();
}
