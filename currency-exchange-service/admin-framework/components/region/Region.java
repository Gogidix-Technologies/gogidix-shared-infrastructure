package com.exalt.shared.ecommerce.admin.components.region;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Region entity for the region management component.
 * This class defines a region that can be managed by the
 * region management component.
 */
public class Region {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private Region parentRegion;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Region() {
        this.id = UUID.randomUUID();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name, code, and description
     * 
     * @param name The name of the region
     * @param code The code of the region
     * @param description The description of the region
     */
    public Region(String name, String code, String description) {
        this();
        this.name = name;
        this.code = code;
        this.description = description;
    }
    
    /**
     * Constructor with name, code, description, and parent region
     * 
     * @param name The name of the region
     * @param code The code of the region
     * @param description The description of the region
     * @param parentRegion The parent region
     */
    public Region(String name, String code, String description, Region parentRegion) {
        this(name, code, description);
        this.parentRegion = parentRegion;
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
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Region getParentRegion() {
        return parentRegion;
    }
    
    public void setParentRegion(Region parentRegion) {
        this.parentRegion = parentRegion;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
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
        return "Region{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentRegion=" + (parentRegion != null ? parentRegion.getName() : "null") +
                ", active=" + active +
                '}';
    }
}
