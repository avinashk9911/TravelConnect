package com.travelconnect.traveler.service.impl;

import com.travelconnect.traveler.domain.Traveler;
import com.travelconnect.traveler.dto.request.CreateTravelerRequest;
import com.travelconnect.traveler.dto.request.UpdateTravelerRequest;
import com.travelconnect.traveler.dto.response.TravelerResponse;
import com.travelconnect.traveler.exception.DuplicateTravelerException;
import com.travelconnect.traveler.exception.TravelerNotFoundException;
import com.travelconnect.traveler.mapper.TravelerMapper;
import com.travelconnect.traveler.repository.TravelerRepository;
import com.travelconnect.traveler.service.TravelerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Business logic for traveler operations.
 *
 * Key annotations:
 *
 * @Service — marks this as a Spring-managed service bean.
 *   Spring creates one instance of this class and injects it
 *   wherever TravelerService is @Autowired or constructor-injected.
 *
 * @RequiredArgsConstructor (Lombok) — generates a constructor for all
 *   final fields. Spring uses this constructor for dependency injection.
 *   This is the modern, recommended injection style (no @Autowired on fields).
 *
 * @Slf4j (Lombok) — injects a SLF4J logger named after this class.
 *   Use log.info/warn/error for structured logging.
 *
 * @Transactional — wraps each public method in a database transaction.
 *   If an exception is thrown, the transaction is rolled back automatically.
 *   readOnly = true on query methods is a performance hint — the DB can
 *   skip dirty-checking and use read replicas if available.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TravelerServiceImpl implements TravelerService {

    private final TravelerRepository travelerRepository;
    private final TravelerMapper travelerMapper;

    @Override
    public TravelerResponse createTraveler(CreateTravelerRequest request) {
        log.info("Creating traveler with email: {}", request.email());

        // Check for duplicate before inserting so we get a clean 409 error.
        // Without this check, we'd get a DataIntegrityViolationException from
        // the DB unique constraint — harder to handle cleanly.
        if (travelerRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new DuplicateTravelerException(request.email());
        }

        Traveler traveler = travelerMapper.toEntity(request);
        Traveler saved = travelerRepository.save(traveler);

        log.info("Traveler created successfully: id={}", saved.getId());
        return travelerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TravelerResponse getTravelerById(UUID id) {
        log.debug("Fetching traveler by id: {}", id);
        Traveler traveler = findTravelerById(id);
        return travelerMapper.toResponse(traveler);
    }

    @Override
    @Transactional(readOnly = true)
    public TravelerResponse getTravelerByEmail(String email) {
        log.debug("Fetching traveler by email: {}", email);
        Traveler traveler = travelerRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new TravelerNotFoundException(email));
        return travelerMapper.toResponse(traveler);
    }

    @Override
    public TravelerResponse updateTraveler(UUID id, UpdateTravelerRequest request) {
        log.info("Updating traveler: id={}", id);

        Traveler traveler = findTravelerById(id);
        travelerMapper.updateEntity(traveler, request);

        // No explicit save() needed here — the entity is already managed
        // (attached to the Hibernate session). Any changes to it will be
        // persisted when the transaction commits (dirty checking).
        Traveler updated = travelerRepository.save(traveler);

        log.info("Traveler updated: id={}", id);
        return travelerMapper.toResponse(updated);
    }

    @Override
    public void deleteTraveler(UUID id) {
        log.info("Deleting traveler: id={}", id);
        Traveler traveler = findTravelerById(id);
        travelerRepository.delete(traveler);
        log.info("Traveler deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TravelerResponse> getAllTravelers(Pageable pageable) {
        log.debug("Fetching all travelers, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return travelerRepository.findAll(pageable)
                .map(travelerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TravelerResponse> searchByLastName(String lastName, Pageable pageable) {
        log.debug("Searching travelers by lastName: {}", lastName);
        return travelerRepository.findByLastNameContainingIgnoreCase(lastName, pageable)
                .map(travelerMapper::toResponse);
    }

    /** Private helper — avoids repeating the "findById + orElseThrow" pattern. */
    private Traveler findTravelerById(UUID id) {
        return travelerRepository.findById(id)
                .orElseThrow(() -> new TravelerNotFoundException(id));
    }
}
