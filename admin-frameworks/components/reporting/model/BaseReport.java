package com.exalt.admin.components.reporting.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Base model for all reports in the admin framework.
 * Provides common properties and behavior for all report types.
 */
public abstract class BaseReport {
    
    private String id;
    private String name;
    private String description;
    private LocalDateTime generatedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Object> parameters;
    private ReportStatus status;
    
    /**
     * Default constructor that initializes a new report with default values.
     */
    public BaseReport() {
        this.id = UUID.randomUUID().toString();
        this.generatedAt = LocalDateTime.now();
        this.status = ReportStatus.PENDING;
    }
    
    /**
     * Constructor with essential report parameters.
     * 
     * @param name Report name
     * @param description Report description
     * @param startDate Start date for report data
     * @param endDate End date for report data
     * @param parameters Additional report parameters
     */
    public BaseReport(String name, String description, LocalDate startDate, 
                     LocalDate endDate, Map<String, Object> parameters) {
        this();
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.parameters = parameters;
    }
    
    /**
     * Generate the report data.
     * This method should be implemented by concrete report classes to
     * perform the actual report generation.
     */
    public abstract void generate();
    
    /**
     * Validate report parameters.
     * This method should be implemented by concrete report classes to
     * validate that all required parameters are present and valid.
     * 
     * @return true if parameters are valid, false otherwise
     */
    public abstract boolean validateParameters();
    
    /**
     * Mark the report as complete.
     */
    public void markComplete() {
        this.status = ReportStatus.COMPLETED;
    }
    
    /**
     * Mark the report as failed with an error message.
     * 
     * @param errorMessage Error message describing the failure
     */
    public void markFailed(String errorMessage) {
        this.status = ReportStatus.FAILED;
        // Store error message in parameters
        this.parameters.put("errorMessage", errorMessage);
    }
    
    // Getters and setters
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }
    
    /**
     * Enum representing the possible statuses of a report.
     */
    public enum ReportStatus {
        PENDING,
        GENERATING,
        COMPLETED,
        FAILED
    }
}
