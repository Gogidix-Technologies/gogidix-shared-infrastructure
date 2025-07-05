package com.gogidix.shared.ecommerce.admin.components.region;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Region management component for admin applications.
 * This component provides functionality for managing regions
 * in the admin application, including creating, updating, and
 * deleting regions.
 */
public class RegionManagement {
    private UUID id;
    private String name;
    private String description;
    private List<Region> regions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public RegionManagement() {
        this.id = UUID.randomUUID();
        this.regions = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name and description
     * 
     * @param name The name of the region management component
     * @param description The description of the region management component
     */
    public RegionManagement(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    /**
     * Add a region
     * 
     * @param region The region to add
     */
    public void addRegion(Region region) {
        this.regions.add(region);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove a region
     * 
     * @param regionId The ID of the region to remove
     * @return true if the region was removed, false otherwise
     */
    public boolean removeRegion(UUID regionId) {
        boolean removed = this.regions.removeIf(region -> region.getId().equals(regionId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }
    
    /**
     * Get a region by ID
     * 
     * @param regionId The ID of the region to get
     * @return An Optional containing the region if found, empty otherwise
     */
    public Optional<Region> getRegionById(UUID regionId) {
        return this.regions.stream()
                .filter(region -> region.getId().equals(regionId))
                .findFirst();
    }
    
    /**
     * Get all regions
     * 
     * @return The list of regions
     */
    public List<Region> getRegions() {
        return regions;
    }
    
    /**
     * Get regions by parent region ID
     * 
     * @param parentRegionId The ID of the parent region
     * @return A list of regions that have the specified parent region
     */
    public List<Region> getRegionsByParentId(UUID parentRegionId) {
        return this.regions.stream()
                .filter(region -> region.getParentRegion() != null && 
                        region.getParentRegion().getId().equals(parentRegionId))
                .toList();
    }
    
    /**
     * Get top-level regions (regions without a parent)
     * 
     * @return A list of top-level regions
     */
    public List<Region> getTopLevelRegions() {
        return this.regions.stream()
                .filter(region -> region.getParentRegion() == null)
                .toList();
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
