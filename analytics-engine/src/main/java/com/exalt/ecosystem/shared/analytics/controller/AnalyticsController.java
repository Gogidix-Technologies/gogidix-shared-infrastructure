package com.exalt.ecosystem.shared.analytics.controller;

import com.exalt.ecosystem.shared.analytics.service.AnalyticsService;
import com.exalt.ecosystem.shared.analytics.dto.AnalyticsRequest;
import com.exalt.ecosystem.shared.analytics.dto.AnalyticsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller for analytics operations.
 * Provides endpoints for generating and retrieving analytics.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Get system health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        log.info("Health check requested");
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    /**
     * Process an analytics request for a specific time range
     */
    @PostMapping("/process")
    public ResponseEntity<AnalyticsResponse> processAnalytics(
            @Valid @RequestBody AnalyticsRequest request) {
        log.info("Processing analytics request: {}", request);
        AnalyticsResponse response = analyticsService.processAnalytics(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get analytics data for a specified metric
     */
    @GetMapping("/metrics/{metricName}")
    public ResponseEntity<Map<String, Object>> getMetric(
            @PathVariable String metricName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("Getting metric: {} for period {} to {}", metricName, startDate, endDate);
        Map<String, Object> result = analyticsService.getMetricData(metricName, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * Get dashboard summary with all key metrics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        log.info("Getting dashboard summary");
        Map<String, Object> dashboard = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(dashboard);
    }
} 