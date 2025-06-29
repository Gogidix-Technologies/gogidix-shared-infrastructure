package com.exalt.ecosystem.shared.analytics.dto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for analytics requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsRequest {

    /**
     * Type of analytics to process (e.g., "sales", "user-behavior", "inventory")
     */
    @NotBlank(message = "Analytics type is required")
    private String type;

    /**
     * Start date for the analysis period in ISO format (YYYY-MM-DD)
     */
    @NotBlank(message = "Start date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must be in format YYYY-MM-DD")
    private String startDate;

    /**
     * End date for the analysis period in ISO format (YYYY-MM-DD)
     */
    @NotBlank(message = "End date is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "End date must be in format YYYY-MM-DD")
    private String endDate;

    /**
     * Metrics to include in the analysis
     */
    @NotNull(message = "Metrics cannot be null")
    private List<String> metrics;

    /**
     * Optional filters to apply to the analysis
     */
    private Map<String, String> filters;

    /**
     * Optional grouping parameters
     */
    private List<String> groupBy;

    /**
     * Optional sorting parameters
     */
    private Map<String, String> sortBy;

    /**
     * Maximum number of results to return
     */
    private Integer limit;
} 
