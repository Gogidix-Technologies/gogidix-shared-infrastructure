package com.gogidix.ecosystem.shared.analytics.service;

import com.gogidix.ecosystem.shared.analytics.dto.AnalyticsRequest;
import com.gogidix.ecosystem.shared.analytics.dto.AnalyticsResponse;

import java.util.Map;

/**
 * Service interface for analytics processing.
 */
public interface AnalyticsService {

    /**
     * Process an analytics request and return results.
     *
     * @param request The analytics request containing parameters
     * @return The analytics response with results
     */
    AnalyticsResponse processAnalytics(AnalyticsRequest request);

    /**
     * Get data for a specific metric.
     *
     * @param metricName Name of the metric to retrieve
     * @param startDate  Optional start date for the data range
     * @param endDate    Optional end date for the data range
     * @return Map of metric data
     */
    Map<String, Object> getMetricData(String metricName, String startDate, String endDate);

    /**
     * Get a dashboard summary with all key metrics.
     *
     * @return Map of dashboard data
     */
    Map<String, Object> getDashboardSummary();
} 
