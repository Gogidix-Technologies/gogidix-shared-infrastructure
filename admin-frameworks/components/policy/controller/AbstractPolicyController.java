package com.gogidix.shared.admin.components.policy.controller;

import com.gogidix.shared.admin.components.policy.model.BasePolicy;
import com.gogidix.shared.admin.components.policy.service.AbstractPolicyService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Abstract controller for policy management APIs.
 * Provides standardized REST endpoints for policy operations across different domains.
 * 
 * @param <T> The specific policy type (extending BasePolicy)
 * @param <S> The specific policy service (extending AbstractPolicyService)
 */
public abstract class AbstractPolicyController<T extends BasePolicy, S extends AbstractPolicyService<T>> {
    
    protected final S policyService;
    
    public AbstractPolicyController(S policyService) {
        this.policyService = policyService;
    }
    
    /**
     * Creates a new policy
     * 
     * @param policy The policy to create
     * @return The created policy
     */
    public T createPolicy(T policy) {
        return policyService.createPolicy(policy);
    }
    
    /**
     * Updates an existing policy
     * 
     * @param policyId The ID of the policy to update
     * @param policy The updated policy data
     * @return The updated policy
     */
    public T updatePolicy(UUID policyId, T policy) {
        policy.setId(policyId);
        return policyService.updatePolicy(policy);
    }
    
    /**
     * Gets a policy by ID
     * 
     * @param policyId The ID of the policy to get
     * @return The policy if found
     */
    public T getPolicy(UUID policyId) {
        return policyService.findPolicyById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));
    }
    
    /**
     * Deletes a policy
     * 
     * @param policyId The ID of the policy to delete 
     * @return true if successfully deleted
     */
    public boolean deletePolicy(UUID policyId) {
        return policyService.deletePolicy(policyId);
    }
    
    /**
     * Archives a policy
     * 
     * @param policyId The ID of the policy to archive
     * @return The archived policy
     */
    public T archivePolicy(UUID policyId) {
        return policyService.archivePolicy(policyId);
    }
    
    /**
     * Activates a policy
     * 
     * @param policyId The ID of the policy to activate
     * @return The activated policy
     */
    public T activatePolicy(UUID policyId) {
        return policyService.activatePolicy(policyId);
    }
    
    /**
     * Deactivates a policy
     * 
     * @param policyId The ID of the policy to deactivate
     * @return The deactivated policy
     */
    public T deactivatePolicy(UUID policyId) {
        return policyService.deactivatePolicy(policyId);
    }
    
    /**
     * Gets all policies
     * 
     * @return List of all policies
     */
    public List<T> getAllPolicies() {
        return policyService.findAllPolicies();
    }
    
    /**
     * Gets active policies
     * 
     * @return List of active policies
     */
    public List<T> getActivePolicies() {
        return policyService.findActivePolicies();
    }
    
    /**
     * Gets policies by status
     * 
     * @param status The status to filter by
     * @return List of policies with the specified status
     */
    public List<T> getPoliciesByStatus(BasePolicy.PolicyStatus status) {
        return policyService.findPoliciesByStatus(status);
    }
    
    /**
     * Gets policies by region
     * 
     * @param regionCode The code of the region to filter by
     * @return List of policies applicable to the region
     */
    public List<T> getPoliciesByRegion(String regionCode) {
        return policyService.findPoliciesByRegion(regionCode);
    }
    
    /**
     * Gets policies by effective date range
     * 
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of policies effective within the date range
     */
    public List<T> getPoliciesByEffectiveDateRange(Date startDate, Date endDate) {
        return policyService.findPoliciesByEffectiveDateRange(startDate, endDate);
    }
    
    /**
     * Creates a new version of a policy
     * 
     * @param policyId The ID of the policy to version
     * @return The new version of the policy
     */
    public T createPolicyVersion(UUID policyId) {
        return policyService.createPolicyVersion(policyId);
    }
    
    /**
     * Gets all versions of a policy
     * 
     * @param policyName The name of the policy
     * @return List of all versions of the policy
     */
    public List<T> getPolicyVersions(String policyName) {
        return policyService.findPolicyVersions(policyName);
    }
    
    /**
     * Domain-specific endpoint implementation
     * Allows domains to add custom endpoints specific to their needs
     */
    protected abstract void registerDomainSpecificEndpoints();
}
