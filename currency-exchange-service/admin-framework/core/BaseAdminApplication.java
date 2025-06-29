package com.exalt.shared.ecommerce.admin.core;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all admin applications in the ecosystem.
 * This class provides common functionality for admin applications,
 * including security, user management, and dashboard configuration.
 */
public class BaseAdminApplication {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public BaseAdminApplication() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name and description
     * 
     * @param name The name of the admin application
     * @param description The description of the admin application
     */
    public BaseAdminApplication(String name, String description) {
        this();
        this.name = name;
        this.description = description;
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
    
    @Override
    public String toString() {
        return "BaseAdminApplication{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
