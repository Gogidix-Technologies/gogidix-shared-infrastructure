package com.exalt.shared.ecommerce.admin.components.reporting;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Report template for the reporting component.
 * This class defines a report template that can be used
 * to generate reports in admin applications.
 */
public class ReportTemplate {
    private UUID id;
    private String name;
    private String description;
    private String templateContent;
    private String templateType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public ReportTemplate() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name, description, and template content
     * 
     * @param name The name of the report template
     * @param description The description of the report template
     * @param templateContent The content of the report template
     * @param templateType The type of the report template (e.g., PDF, Excel, CSV)
     */
    public ReportTemplate(String name, String description, String templateContent, String templateType) {
        this();
        this.name = name;
        this.description = description;
        this.templateContent = templateContent;
        this.templateType = templateType;
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
    
    public String getTemplateContent() {
        return templateContent;
    }
    
    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getTemplateType() {
        return templateType;
    }
    
    public void setTemplateType(String templateType) {
        this.templateType = templateType;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
