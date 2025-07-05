package com.gogidix.shared.admin.components.region.repository;

import com.microsocial.admin.components.region.model.BaseRegion;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface for region data operations.
 * Defines common methods for accessing and manipulating region data.
 * 
 * @param <T> Region type, which must extend BaseRegion
 */
public interface BaseRegionRepository<T extends BaseRegion> {
    
    /**
     * Find all regions.
     * 
     * @return List of all regions
     */
    List<T> findAll();
    
    /**
     * Find a region by ID.
     * 
     * @param id Region ID
     * @return Optional containing the region if found, empty otherwise
     */
    Optional<T> findById(String id);
    
    /**
     * Find regions by name (partial match).
     * 
     * @param name Region name to search for
     * @return List of matching regions
     */
    List<T> findByNameContaining(String name);
    
    /**
     * Find a region by code (exact match).
     * 
     * @param code Region code
     * @return Optional containing the region if found, empty otherwise
     */
    Optional<T> findByCode(String code);
    
    /**
     * Find regions by status.
     * 
     * @param status Region status
     * @return List of regions with the specified status
     */
    List<T> findByStatus(BaseRegion.RegionStatus status);
    
    /**
     * Find regions by type.
     * 
     * @param type Region type
     * @return List of regions with the specified type
     */
    List<T> findByType(BaseRegion.RegionType type);
    
    /**
     * Find child regions of a parent region.
     * 
     * @param parentId Parent region ID
     * @return List of child regions
     */
    List<T> findByParentRegionId(String parentId);
    
    /**
     * Save a region.
     * 
     * @param region Region to save
     * @return Saved region
     */
    T save(T region);
    
    /**
     * Delete a region by ID.
     * 
     * @param id Region ID
     */
    void deleteById(String id);
    
    /**
     * Check if a region exists by ID.
     * 
     * @param id Region ID
     * @return true if the region exists, false otherwise
     */
    boolean existsById(String id);
    
    /**
     * Check if a region exists by code.
     * 
     * @param code Region code
     * @return true if the region exists, false otherwise
     */
    boolean existsByCode(String code);
}
