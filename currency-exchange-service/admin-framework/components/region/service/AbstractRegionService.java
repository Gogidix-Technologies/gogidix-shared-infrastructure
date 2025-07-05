package com.gogidix.shared.admin.components.region.service;

import com.microsocial.admin.components.region.model.BaseRegion;
import com.microsocial.admin.components.region.repository.BaseRegionRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract service for region management.
 * Provides common functionality for all region services.
 * 
 * @param <T> Region type, which must extend BaseRegion
 */
public abstract class AbstractRegionService<T extends BaseRegion> {
    
    protected BaseRegionRepository<T> regionRepository;
    
    /**
     * Get all regions.
     * 
     * @return List of all regions
     */
    public List<T> getAllRegions() {
        return regionRepository.findAll();
    }
    
    /**
     * Get a region by ID.
     * 
     * @param id Region ID
     * @return Optional containing the region if found, empty otherwise
     */
    public Optional<T> getRegionById(String id) {
        return regionRepository.findById(id);
    }
    
    /**
     * Get regions by name (partial match).
     * 
     * @param name Region name to search for
     * @return List of matching regions
     */
    public List<T> getRegionsByName(String name) {
        return regionRepository.findByNameContaining(name);
    }
    
    /**
     * Get a region by code (exact match).
     * 
     * @param code Region code
     * @return Optional containing the region if found, empty otherwise
     */
    public Optional<T> getRegionByCode(String code) {
        return regionRepository.findByCode(code);
    }
    
    /**
     * Get regions by status.
     * 
     * @param status Region status
     * @return List of regions with the specified status
     */
    public List<T> getRegionsByStatus(BaseRegion.RegionStatus status) {
        return regionRepository.findByStatus(status);
    }
    
    /**
     * Get regions by type.
     * 
     * @param type Region type
     * @return List of regions with the specified type
     */
    public List<T> getRegionsByType(BaseRegion.RegionType type) {
        return regionRepository.findByType(type);
    }
    
    /**
     * Get child regions of a parent region.
     * 
     * @param parentId Parent region ID
     * @return List of child regions
     */
    public List<T> getChildRegions(String parentId) {
        return regionRepository.findByParentRegionId(parentId);
    }
    
    /**
     * Get the hierarchy of regions starting from the specified region.
     * 
     * @param rootId ID of the root region
     * @return List of all regions in the hierarchy
     */
    public List<T> getRegionHierarchy(String rootId) {
        List<T> hierarchy = new ArrayList<>();
        
        Optional<T> rootOpt = regionRepository.findById(rootId);
        if (rootOpt.isEmpty()) {
            return hierarchy;
        }
        
        T root = rootOpt.get();
        hierarchy.add(root);
        
        // Recursively add all children
        addChildrenToHierarchy(root, hierarchy);
        
        return hierarchy;
    }
    
    /**
     * Recursively add children to the hierarchy list.
     * 
     * @param parent Parent region
     * @param hierarchy List to add children to
     */
    private void addChildrenToHierarchy(T parent, List<T> hierarchy) {
        List<T> children = regionRepository.findByParentRegionId(parent.getId());
        
        for (T child : children) {
            hierarchy.add(child);
            addChildrenToHierarchy(child, hierarchy);
        }
    }
    
    /**
     * Create a new region.
     * 
     * @param region Region to create
     * @param userId ID of the user creating the region
     * @return Created region
     * @throws IllegalArgumentException if the region is invalid or code already exists
     */
    public T createRegion(T region, String userId) {
        // Validate the region
        if (!region.validate()) {
            throw new IllegalArgumentException("Invalid region data");
        }
        
        // Check if code already exists
        if (regionRepository.existsByCode(region.getCode())) {
            throw new IllegalArgumentException("Region code already exists: " + region.getCode());
        }
        
        // Set metadata
        region.setCreatedBy(userId);
        region.setUpdatedBy(userId);
        region.setCreatedAt(LocalDateTime.now());
        region.setUpdatedAt(LocalDateTime.now());
        
        // Pre-processing hook
        preProcessRegion(region);
        
        // Save the region
        T savedRegion = regionRepository.save(region);
        
        // If this region has a parent, add it as a child of the parent
        if (region.getParentRegionId() != null) {
            addChildToParent(savedRegion);
        }
        
        // Post-processing hook
        postProcessRegion(savedRegion);
        
        return savedRegion;
    }
    
    /**
     * Update an existing region.
     * 
     * @param id Region ID
     * @param regionData Updated region data
     * @param userId ID of the user updating the region
     * @return Updated region
     * @throws IllegalArgumentException if the region is invalid, doesn't exist, or code conflicts
     */
    public T updateRegion(String id, T regionData, String userId) {
        // Validate the region
        if (!regionData.validate()) {
            throw new IllegalArgumentException("Invalid region data");
        }
        
        // Check if region exists
        Optional<T> existingOpt = regionRepository.findById(id);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Region not found: " + id);
        }
        
        T existing = existingOpt.get();
        
        // Check if code is changing and if new code already exists
        if (!existing.getCode().equals(regionData.getCode()) && 
            regionRepository.existsByCode(regionData.getCode())) {
            throw new IllegalArgumentException("Region code already exists: " + regionData.getCode());
        }
        
        // Update fields
        existing.setName(regionData.getName());
        existing.setCode(regionData.getCode());
        existing.setDescription(regionData.getDescription());
        existing.setType(regionData.getType());
        existing.setStatus(regionData.getStatus());
        existing.setUpdatedBy(userId);
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Handle parent region change
        handleParentRegionChange(existing, regionData.getParentRegionId());
        
        // Pre-processing hook
        preProcessRegion(existing);
        
        // Save the updated region
        T savedRegion = regionRepository.save(existing);
        
        // Post-processing hook
        postProcessRegion(savedRegion);
        
        return savedRegion;
    }
    
    /**
     * Deactivate a region.
     * 
     * @param id Region ID
     * @param userId ID of the user deactivating the region
     * @return Deactivated region
     * @throws IllegalArgumentException if the region doesn't exist
     * @throws IllegalStateException if the region has active children
     */
    public T deactivateRegion(String id, String userId) {
        // Check if region exists
        Optional<T> regionOpt = regionRepository.findById(id);
        if (regionOpt.isEmpty()) {
            throw new IllegalArgumentException("Region not found: " + id);
        }
        
        T region = regionOpt.get();
        
        // Check if region has active children
        List<T> children = regionRepository.findByParentRegionId(id);
        for (T child : children) {
            if (child.getStatus() == BaseRegion.RegionStatus.ACTIVE) {
                throw new IllegalStateException("Cannot deactivate a region with active children");
            }
        }
        
        // Deactivate the region
        region.deactivate();
        region.setUpdatedBy(userId);
        
        // Save the updated region
        return regionRepository.save(region);
    }
    
    /**
     * Activate a region.
     * 
     * @param id Region ID
     * @param userId ID of the user activating the region
     * @return Activated region
     * @throws IllegalArgumentException if the region doesn't exist
     * @throws IllegalStateException if the region's parent is not active
     */
    public T activateRegion(String id, String userId) {
        // Check if region exists
        Optional<T> regionOpt = regionRepository.findById(id);
        if (regionOpt.isEmpty()) {
            throw new IllegalArgumentException("Region not found: " + id);
        }
        
        T region = regionOpt.get();
        
        // Check if parent is active
        if (region.getParentRegionId() != null) {
            Optional<T> parentOpt = regionRepository.findById(region.getParentRegionId());
            if (parentOpt.isPresent() && parentOpt.get().getStatus() != BaseRegion.RegionStatus.ACTIVE) {
                throw new IllegalStateException("Cannot activate a region whose parent is not active");
            }
        }
        
        // Activate the region
        region.activate();
        region.setUpdatedBy(userId);
        
        // Save the updated region
        return regionRepository.save(region);
    }
    
    /**
     * Delete a region.
     * 
     * @param id Region ID
     * @throws IllegalArgumentException if the region doesn't exist
     * @throws IllegalStateException if the region has children
     */
    public void deleteRegion(String id) {
        // Check if region exists
        if (!regionRepository.existsById(id)) {
            throw new IllegalArgumentException("Region not found: " + id);
        }
        
        // Check if region has children
        List<T> children = regionRepository.findByParentRegionId(id);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete a region with children");
        }
        
        // Remove from parent's children list if needed
        Optional<T> regionOpt = regionRepository.findById(id);
        if (regionOpt.isPresent() && regionOpt.get().getParentRegionId() != null) {
            removeChildFromParent(regionOpt.get());
        }
        
        // Delete the region
        regionRepository.deleteById(id);
    }
    
    /**
     * Add a region as a child of its parent.
     * 
     * @param child Child region
     */
    private void addChildToParent(T child) {
        Optional<T> parentOpt = regionRepository.findById(child.getParentRegionId());
        
        if (parentOpt.isPresent()) {
            T parent = parentOpt.get();
            parent.addChildRegion(child.getId());
            regionRepository.save(parent);
        }
    }
    
    /**
     * Remove a region from its parent's children list.
     * 
     * @param child Child region
     */
    private void removeChildFromParent(T child) {
        Optional<T> parentOpt = regionRepository.findById(child.getParentRegionId());
        
        if (parentOpt.isPresent()) {
            T parent = parentOpt.get();
            parent.removeChildRegion(child.getId());
            regionRepository.save(parent);
        }
    }
    
    /**
     * Handle changes to a region's parent.
     * 
     * @param region Region being updated
     * @param newParentId New parent region ID
     */
    private void handleParentRegionChange(T region, String newParentId) {
        // If the parent hasn't changed, do nothing
        if ((region.getParentRegionId() == null && newParentId == null) ||
            (region.getParentRegionId() != null && region.getParentRegionId().equals(newParentId))) {
            return;
        }
        
        // If the region had a parent, remove it from that parent's children
        if (region.getParentRegionId() != null) {
            removeChildFromParent(region);
        }
        
        // Set the new parent
        region.setParentRegionId(newParentId);
        
        // If the region has a new parent, add it to that parent's children
        if (newParentId != null) {
            // We'll add it to the parent's children after saving the region
            // in the calling method
        }
    }
    
    /**
     * Pre-process a region before saving.
     * This method can be overridden by concrete services to apply domain-specific
     * processing to the region before it is saved.
     * 
     * @param region Region to process
     */
    protected void preProcessRegion(T region) {
        // Default implementation does nothing
    }
    
    /**
     * Post-process a region after saving.
     * This method can be overridden by concrete services to apply domain-specific
     * processing to the region after it is saved.
     * 
     * @param region Saved region
     */
    protected void postProcessRegion(T region) {
        // Default implementation does nothing
    }
}
