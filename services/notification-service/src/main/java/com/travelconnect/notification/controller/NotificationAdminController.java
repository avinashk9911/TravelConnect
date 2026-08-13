package com.travelconnect.notification.controller;

import com.travelconnect.notification.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin endpoints for the notification service.
 *
 * These are NOT customer-facing — they exist for operational and demo purposes.
 * In a real deployment they would be protected behind an admin role or internal network.
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@Slf4j
public class NotificationAdminController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Notification service is running");
    }

    /**
     * Performance test endpoint — generates a controlled CPU workload.
     *
     * Educational purpose: demonstrates how to investigate performance issues
     * using /actuator/threaddump and /actuator/heapdump while this is running.
     *
     * NOT dangerous — iteration count is hard-capped at 10 million,
     * and the work is purely arithmetic with no I/O or memory accumulation.
     */
    @PostMapping("/perf-test")
    public ApiResponse<String> perfTest(@RequestParam(defaultValue = "100000") int iterations) {
        log.info("Starting performance test with {} iterations", iterations);
        long start = System.currentTimeMillis();
        // Bounded CPU workload — safe for demos
        long sum = 0;
        for (int i = 0; i < Math.min(iterations, 10_000_000); i++) {
            sum += (long) (Math.sqrt(i) * Math.PI);
        }
        long elapsed = System.currentTimeMillis() - start;
        log.info("Performance test completed in {}ms, result={}", elapsed, sum);
        return ApiResponse.success(String.format("Completed %d iterations in %dms", iterations, elapsed));
    }
}
