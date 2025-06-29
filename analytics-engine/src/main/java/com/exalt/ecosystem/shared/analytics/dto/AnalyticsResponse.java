package com.exalt.ecosystem.shared.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for analytics responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    /**
     * Unique identifier for this analytics response
     */
    private String id;

    /**
     * Type of analytics that was processed
     */
    private String type;

    /**
     * Start date used for the analysis
     */
    private String startDate;

    /**
     * End date used for the analysis
     */
    private String endDate;

    /**
     * Timestamp when the analytics were generated
     */
    private LocalDateTime timestamp;

    /**
     * Status of the analytics processing (e.g., "completed", "partial", "failed")
     */
    private String status;

    /**
     * Optional error message if processing failed
     */
    private String errorMessage;

    /**
     * Summary statistics for quick reference
     */
    private Map<String, Object> summary;

    /**
     * Detailed results of the analytics processing
     */
    private List<Map<String, Object>> data;

    /**
     * Metadata about the analysis
     */
    private Map<String, Object> metadata;

    /**
     * Processing time in milliseconds
     */
    private Long processingTimeMs;
} 
