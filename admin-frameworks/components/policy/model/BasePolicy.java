package com.exalt.shared.admin.components.policy.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Base policy model providing common functionality for all domain policies.
 * This abstract class defines the structure and common behavior for policy objects
 * across different domains in the Micro-Social-Ecommerce ecosystem.
 */
public abstract class BasePolicy {
    
    private UUID id;
    private String name;
    private String description;
    private String version;
    private Date effectiveDate;
    private Date expirationDate;
    private PolicyScope scope;
    private PolicyStatus status;
    private Set<String> applicableRegions;
    private Set<String> excludedRegions;
    private String createdBy;
    private Date createdDate;
    private String lastModifiedBy;
    private Date lastModifiedDate;
    
    public BasePolicy() {
        this.id = UUID.randomUUID();
        this.status = PolicyStatus.DRAFT;
        this.version = "1.0";
        this.scope = PolicyScope.GLOBAL;
        this.applicableRegions = new HashSet<>();
        this.excludedRegions = new HashSet<>();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
    }
    
    public BasePolicy(String name, String description, PolicyScope scope) {
        this();
        this.name = name;
        this.description = description;
        this.scope = scope;
    }
    
    /**
     * Checks if the policy is applicable to a specific region
     * 
     * @param regionCode The code of the region to check
     * @return true if the policy is applicable to the region
     */
    public boolean isApplicableToRegion(String regionCode) {
        // If excluded specifically, not applicable
        if (excludedRegions.contains(regionCode)) {
            return false;
        }
        
        // If global scope and not excluded, applicable
        if (scope == PolicyScope.GLOBAL) {
            return true;
        }
        
        // If region scope, check if specifically included
        return applicableRegions.contains(regionCode);
    }
    
    /**
     * Checks if the policy is active at the current date
     * 
     * @return true if the policy is active
     */
    public boolean isActive() {
        Date now = new Date();
        boolean afterEffective = effectiveDate == null || !now.before(effectiveDate);
        boolean beforeExpiration = expirationDate == null || !now.after(expirationDate);
        return status == PolicyStatus.ACTIVE && afterEffective && beforeExpiration;
    }
    
    /**
     * Activates the policy
     */
    public void activate() {
        this.status = PolicyStatus.ACTIVE;
        this.lastModifiedDate = new Date();
    }
    
    /**
     * Deactivates the policy
     */
    public void deactivate() {
        this.status = PolicyStatus.INACTIVE;
        this.lastModifiedDate = new Date();
    }
    
    /**
     * Adds a region to the applicable regions
     * 
     * @param regionCode The code of the region to add
     * @return true if successfully added
     */
    public boolean addApplicableRegion(String regionCode) {
        this.lastModifiedDate = new Date();
        return applicableRegions.add(regionCode);
    }
    
    /**
     * Removes a region from the applicable regions
     * 
     * @param regionCode The code of the region to remove
     * @return true if successfully removed
     */
    public boolean removeApplicableRegion(String regionCode) {
        this.lastModifiedDate = new Date();
        return applicableRegions.remove(regionCode);
    }
    
    /**
     * Adds a region to the excluded regions
     * 
     * @param regionCode The code of the region to add
     * @return true if successfully added
     */
    public boolean addExcludedRegion(String regionCode) {
        this.lastModifiedDate = new Date();
        return excludedRegions.add(regionCode);
    }
    
    /**
     * Removes a region from the excluded regions
     * 
     * @param regionCode The code of the region to remove
     * @return true if successfully removed
     */
    public boolean removeExcludedRegion(String regionCode) {
        this.lastModifiedDate = new Date();
        return excludedRegions.remove(regionCode);
    }
    
    /**
     * Domain-specific validation method to be implemented by each domain policy
     * to validate the policy content against domain-specific rules
     * 
     * @return true if the policy is valid
     */
    public abstract boolean validate();
    
    // Getters and Setters
    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.lastModifiedDate = new Date();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.lastModifiedDate = new Date();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        this.lastModifiedDate = new Date();
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
        this.lastModifiedDate = new Date();
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
        this.lastModifiedDate = new Date();
    }

    public PolicyScope getScope() {
        return scope;
    }

    public void setScope(PolicyScope scope) {
        this.scope = scope;
        this.lastModifiedDate = new Date();
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
        this.lastModifiedDate = new Date();
    }

    public Set<String> getApplicableRegions() {
        return applicableRegions;
    }

    public void setApplicableRegions(Set<String> applicableRegions) {
        this.applicableRegions = applicableRegions;
        this.lastModifiedDate = new Date();
    }

    public Set<String> getExcludedRegions() {
        return excludedRegions;
    }

    public void setExcludedRegions(Set<String> excludedRegions) {
        this.excludedRegions = excludedRegions;
        this.lastModifiedDate = new Date();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        this.lastModifiedDate = new Date();
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    /**
     * Policy scope enum
     */
    public enum PolicyScope {
        GLOBAL,    // Applies globally by default
        REGIONAL   // Applies only to specified regions
    }
    
    /**
     * Policy status enum
     */
    public enum PolicyStatus {
        DRAFT,     // In development
        ACTIVE,    // In effect
        INACTIVE,  // Not in effect but not archived
        ARCHIVED   // No longer in use
    }
}
