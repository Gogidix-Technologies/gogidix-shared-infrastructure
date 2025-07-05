package com.gogidix.shared.shared.admin.components.policy.service;

import com.microsocial.shared.admin.components.policy.model.BasePolicy;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract service for policy management.
 * Provides common functionality for managing policies across different domains.
 */
public abstract class AbstractPolicyService<T extends BasePolicy> {
    
    /**
     * Creates a new policy
     * 
     * @param policy The policy to create
     * @return The created policy
     */
    public abstract T createPolicy(T policy);
    
    /**
     * Updates an existing policy
     * 
     * @param policy The policy to update
     * @return The updated policy
     */
    public abstract T updatePolicy(T policy);
    
    /**
     * Finds a policy by ID
     * 
     * @param policyId The ID of the policy to find
     * @return Optional containing the policy if found, empty otherwise
     */
    public abstract Optional<T> findPolicyById(UUID policyId);
    
    /**
     * Deletes a policy
     * 
     * @param policyId The ID of the policy to delete
     * @return true if successfully deleted
     */
    public abstract boolean deletePolicy(UUID policyId);
    
    /**
     * Archives a policy
     * 
     * @param policyId The ID of the policy to archive
     * @return The archived policy
     */
    public abstract T archivePolicy(UUID policyId);
    
    /**
     * Activates a policy
     * 
     * @param policyId The ID of the policy to activate
     * @return The activated policy
     */
    public abstract T activatePolicy(UUID policyId);
    
    /**
     * Deactivates a policy
     * 
     * @param policyId The ID of the policy to deactivate
     * @return The deactivated policy
     */
    public abstract T deactivatePolicy(UUID policyId);
    
    /**
     * Finds all policies
     * 
     * @return List of all policies
     */
    public abstract List<T> findAllPolicies();
    
    /**
     * Finds active policies
     * 
     * @return List of active policies
     */
    public abstract List<T> findActivePolicies();
    
    /**
     * Finds policies by status
     * 
     * @param status The status to filter by
     * @return List of policies with the specified status
     */
    public abstract List<T> findPoliciesByStatus(BasePolicy.PolicyStatus status);
    
    /**
     * Finds policies by region
     * 
     * @param regionCode The code of the region to filter by
     * @return List of policies applicable to the region
     */
    public abstract List<T> findPoliciesByRegion(String regionCode);
    
    /**
     * Finds policies by effective date range
     * 
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of policies effective within the date range
     */
    public abstract List<T> findPoliciesByEffectiveDateRange(Date startDate, Date endDate);
    
    /**
     * Creates a new version of a policy
     * 
     * @param policyId The ID of the policy to version
     * @return The new version of the policy
     */
    public abstract T createPolicyVersion(UUID policyId);
    
    /**
     * Finds all versions of a policy
     * 
     * @param policyName The name of the policy
     * @return List of all versions of the policy
     */
    public abstract List<T> findPolicyVersions(String policyName);
    
    /**
     * Domain-specific method to apply any domain-specific rules or processing
     * 
     * @param policy The policy to process
     * @return The processed policy
     */
    protected abstract T applyDomainSpecificRules(T policy);
    
    /**
     * Domain-specific method to validate a policy against domain-specific rules
     * 
     * @param policy The policy to validate
     * @return true if the policy is valid
     */
    protected abstract boolean validateDomainPolicy(T policy);
}
