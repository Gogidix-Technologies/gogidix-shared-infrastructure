package com.gogidix.ecommerce.admin.components.reporting;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Reporting component for admin applications.
 * This component provides reporting functionality for admin applications,
 * including report generation, scheduling, and distribution.
 */
public class ReportingComponent {
    private UUID id;
    private String name;
    private String description;
    private List<ReportTemplate> reportTemplates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public ReportingComponent() {
        this.id = UUID.randomUUID();
        this.reportTemplates = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name and description
     * 
     * @param name The name of the reporting component
     * @param description The description of the reporting component
     */
    public ReportingComponent(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    /**
     * Add a report template to the component
     * 
     * @param template The report template to add
     */
    public void addReportTemplate(ReportTemplate template) {
        this.reportTemplates.add(template);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove a report template from the component
     * 
     * @param templateId The ID of the template to remove
     * @return true if the template was removed, false otherwise
     */
    public boolean removeReportTemplate(UUID templateId) {
        boolean removed = this.reportTemplates.removeIf(template -> template.getId().equals(templateId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }
    
    /**
     * Get all report templates
     * 
     * @return The list of report templates
     */
    public List<ReportTemplate> getReportTemplates() {
        return reportTemplates;
    }
    
    // Getters and setters
    
    public UUID getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
