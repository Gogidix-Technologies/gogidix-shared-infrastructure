package com.gogidix.admin.components.region.controller;

import com.gogidix.admin.components.region.model.BaseRegion;
import com.gogidix.admin.components.region.service.AbstractRegionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

/**
 * Abstract controller for region management.
 * Provides common endpoints for all region controllers.
 * 
 * @param <T> Region type, which must extend BaseRegion
 * @param <S> Region service type, which must extend AbstractRegionService
 */
public abstract class AbstractRegionController<T extends BaseRegion, S extends AbstractRegionService<T>> {
    
    protected final S regionService;
    
    /**
     * Constructor with required service.
     * 
     * @param regionService Region service
     */
    public AbstractRegionController(S regionService) {
        this.regionService = regionService;
    }
    
    /**
     * Get all regions.
     * 
     * @return List of all regions
     */
    @GetMapping
    public ResponseEntity<List<T>> getAllRegions() {
        List<T> regions = regionService.getAllRegions();
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get a region by ID.
     * 
     * @param id Region ID
     * @return Region if found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<T> getRegionById(@PathVariable String id) {
        Optional<T> regionOpt = regionService.getRegionById(id);
        return regionOpt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get regions by name (partial match).
     * 
     * @param name Region name to search for
     * @return List of matching regions
     */
    @GetMapping("/search/name")
    public ResponseEntity<List<T>> getRegionsByName(@RequestParam String name) {
        List<T> regions = regionService.getRegionsByName(name);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get a region by code (exact match).
     * 
     * @param code Region code
     * @return Region if found, 404 otherwise
     */
    @GetMapping("/search/code")
    public ResponseEntity<T> getRegionByCode(@RequestParam String code) {
        Optional<T> regionOpt = regionService.getRegionByCode(code);
        return regionOpt.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get regions by status.
     * 
     * @param status Region status
     * @return List of regions with the specified status
     */
    @GetMapping("/search/status")
    public ResponseEntity<List<T>> getRegionsByStatus(@RequestParam BaseRegion.RegionStatus status) {
        List<T> regions = regionService.getRegionsByStatus(status);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get regions by type.
     * 
     * @param type Region type
     * @return List of regions with the specified type
     */
    @GetMapping("/search/type")
    public ResponseEntity<List<T>> getRegionsByType(@RequestParam BaseRegion.RegionType type) {
        List<T> regions = regionService.getRegionsByType(type);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get child regions of a parent region.
     * 
     * @param parentId Parent region ID
     * @return List of child regions
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<T>> getChildRegions(@PathVariable String parentId) {
        List<T> regions = regionService.getChildRegions(parentId);
        return ResponseEntity.ok(regions);
    }
    
    /**
     * Get the hierarchy of regions starting from the specified region.
     * 
     * @param rootId ID of the root region
     * @return List of all regions in the hierarchy
     */
    @GetMapping("/{rootId}/hierarchy")
    public ResponseEntity<List<T>> getRegionHierarchy(@PathVariable String rootId) {
        List<T> hierarchy = regionService.getRegionHierarchy(rootId);
        return ResponseEntity.ok(hierarchy);
    }
    
    /**
     * Create a new region.
     * 
     * @param region Region to create
     * @param authentication Current user authentication
     * @return Created region with 201 status
     */
    @PostMapping
    public ResponseEntity<T> createRegion(@RequestBody T region, Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            T createdRegion = regionService.createRegion(region, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRegion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update an existing region.
     * 
     * @param id Region ID
     * @param region Updated region data
     * @param authentication Current user authentication
     * @return Updated region if found, 404 otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<T> updateRegion(@PathVariable String id, @RequestBody T region, 
                                         Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            T updatedRegion = regionService.updateRegion(id, region, userId);
            return ResponseEntity.ok(updatedRegion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Deactivate a region.
     * 
     * @param id Region ID
     * @param authentication Current user authentication
     * @return Deactivated region if found, 404 otherwise
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<T> deactivateRegion(@PathVariable String id, Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            T region = regionService.deactivateRegion(id, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Activate a region.
     * 
     * @param id Region ID
     * @param authentication Current user authentication
     * @return Activated region if found, 404 otherwise
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<T> activateRegion(@PathVariable String id, Authentication authentication) {
        try {
            String userId = getUserIdFromAuthentication(authentication);
            T region = regionService.activateRegion(id, userId);
            return ResponseEntity.ok(region);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Delete a region.
     * 
     * @param id Region ID
     * @return 204 if deleted, 404 if not found, 409 if has children
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRegion(@PathVariable String id) {
        try {
            regionService.deleteRegion(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    
    /**
     * Extract the user ID from the authentication object.
     * This method should be implemented by concrete controllers to
     * extract the user ID based on the authentication mechanism.
     * 
     * @param authentication Authentication object
     * @return User ID
     */
    protected abstract String getUserIdFromAuthentication(Authentication authentication);
}
