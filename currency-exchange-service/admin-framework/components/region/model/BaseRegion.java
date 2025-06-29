package com.exalt.shared.admin.components.region.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base model for all regions in the admin framework.
 * Provides common properties and behavior for all region types.
 */
public abstract class BaseRegion {
    
    private String id;
    private String name;
    private String code;
    private String description;
    private RegionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private RegionType type;
    private Set<String> childRegionIds;
    private String parentRegionId;
    
    /**
     * Default constructor that initializes a new region with default values.
     */
    public BaseRegion() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RegionStatus.ACTIVE;
        this.childRegionIds = new HashSet<>();
    }
    
    /**
     * Constructor with essential region parameters.
     * 
     * @param name Region name
     * @param code Region code
     * @param description Region description
     * @param type Region type
     * @param createdBy User who created this region
     */
    public BaseRegion(String name, String code, String description, 
                     RegionType type, String createdBy) {
        this();
        this.name = name;
        this.code = code;
        this.description = description;
        this.type = type;
        this.createdBy = createdBy;
    }
    
    /**
     * Validate region data.
     * This method should be implemented by concrete region classes to
     * validate that all required data is present and valid.
     * 
     * @return true if region data is valid, false otherwise
     */
    public abstract boolean validate();
    
    /**
     * Add a child region to this region.
     * 
     * @param childRegionId ID of the child region
     * @return true if the child was added, false if it was already present
     */
    public boolean addChildRegion(String childRegionId) {
        return this.childRegionIds.add(childRegionId);
    }
    
    /**
     * Remove a child region from this region.
     * 
     * @param childRegionId ID of the child region
     * @return true if the child was removed, false if it wasn't a child
     */
    public boolean removeChildRegion(String childRegionId) {
        return this.childRegionIds.remove(childRegionId);
    }
    
    /**
     * Check if this region contains a specific child region.
     * 
     * @param childRegionId ID of the potential child region
     * @return true if this region contains the child, false otherwise
     */
    public boolean containsChildRegion(String childRegionId) {
        return this.childRegionIds.contains(childRegionId);
    }
    
    /**
     * Deactivate this region.
     */
    public void deactivate() {
        this.status = RegionStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Activate this region.
     */
    public void activate() {
        this.status = RegionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RegionStatus getStatus() {
        return status;
    }

    public void setStatus(RegionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }

    public RegionType getType() {
        return type;
    }

    public void setType(RegionType type) {
        this.type = type;
    }

    public Set<String> getChildRegionIds() {
        return new HashSet<>(childRegionIds);
    }

    public void setChildRegionIds(Set<String> childRegionIds) {
        this.childRegionIds = new HashSet<>(childRegionIds);
    }

    public String getParentRegionId() {
        return parentRegionId;
    }

    public void setParentRegionId(String parentRegionId) {
        this.parentRegionId = parentRegionId;
    }
    
    /**
     * Enum representing the possible statuses of a region.
     */
    public enum RegionStatus {
        ACTIVE,
        INACTIVE,
        PENDING_APPROVAL,
        DEPRECATED
    }
    
    /**
     * Enum representing the possible types of regions.
     */
    public enum RegionType {
        GLOBAL,
        CONTINENT,
        COUNTRY,
        STATE_PROVINCE,
        CITY,
        DISTRICT,
        CUSTOM
    }
}
