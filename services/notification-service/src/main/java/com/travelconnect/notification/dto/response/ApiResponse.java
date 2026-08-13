package com.travelconnect.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Consistent envelope for every API response.
 *
 * Every endpoint returns this structure, so the frontend always knows
 * what shape to expect:
 *
 * Success:
 * {
 *   "success": true,
 *   "message": "Notification service is running",
 *   "data": { ... },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * Error (handled by GlobalExceptionHandler):
 * {
 *   "success": false,
 *   "message": "Something went wrong",
 *   "data": null,
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 *
 * @JsonInclude(NON_NULL) prevents null fields from appearing in the JSON output.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
