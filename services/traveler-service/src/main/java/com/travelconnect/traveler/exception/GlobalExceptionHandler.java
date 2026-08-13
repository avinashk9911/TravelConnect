package com.travelconnect.traveler.exception;

import com.travelconnect.traveler.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralised exception handling for all controllers.
 *
 * Without this class, Spring would return a default 500 error page
 * or a raw exception stack trace. This handler intercepts known
 * exceptions and converts them into our consistent ApiResponse format.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * It applies to all @RestController classes in this service.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles @Valid validation failures.
     *
     * When a request body fails bean validation (e.g. @NotBlank, @Email),
     * Spring throws MethodArgumentNotValidException. We collect all field
     * errors and return them in a map so the client knows exactly what to fix.
     *
     * Example response:
     * {
     *   "success": false,
     *   "message": "Validation failed",
     *   "data": { "email": "must be a valid email address", "firstName": "must not be blank" }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation failed: {}", errors);
        return ApiResponse.<Map<String, String>>builder()
                .success(false)
                .message("Validation failed")
                .data(errors)
                .build();
    }

    /**
     * 404 Not Found — traveler does not exist.
     */
    @ExceptionHandler(TravelerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleTravelerNotFound(TravelerNotFoundException ex) {
        log.warn("Traveler not found: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    /**
     * 409 Conflict — duplicate email.
     */
    @ExceptionHandler(DuplicateTravelerException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDuplicateTraveler(DuplicateTravelerException ex) {
        log.warn("Duplicate traveler: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    /**
     * 500 Internal Server Error — catch-all for unexpected failures.
     *
     * We log the full stack trace internally but return only a safe,
     * generic message to the caller. Never expose stack traces in API responses.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ApiResponse.error("An unexpected error occurred. Please try again later.");
    }
}
