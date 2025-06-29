package com.exalt.ecommerce.admin.components.policy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Policy management component for admin applications.
 * This component provides functionality for managing policies
 * in the admin application, including creating, updating, and
 * deleting policies.
 */
public class PolicyManagement {
    private UUID id;
    private String name;
    private String description;
    private List<Policy> policies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public PolicyManagement() {
        this.id = UUID.randomUUID();
        this.policies = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name and description
     * 
     * @param name The name of the policy management component
     * @param description The description of the policy management component
     */
    public PolicyManagement(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    /**
     * Add a policy
     * 
     * @param policy The policy to add
     */
    public void addPolicy(Policy policy) {
        this.policies.add(policy);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Remove a policy
     * 
     * @param policyId The ID of the policy to remove
     * @return true if the policy was removed, false otherwise
     */
    public boolean removePolicy(UUID policyId) {
        boolean removed = this.policies.removeIf(policy -> policy.getId().equals(policyId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }
    
    /**
     * Get a policy by ID
     * 
     * @param policyId The ID of the policy to get
     * @return An Optional containing the policy if found, empty otherwise
     */
    public Optional<Policy> getPolicyById(UUID policyId) {
        return this.policies.stream()
                .filter(policy -> policy.getId().equals(policyId))
                .findFirst();
    }
    
    /**
     * Get all policies
     * 
     * @return The list of policies
     */
    public List<Policy> getPolicies() {
        return policies;
    }
    
    /**
     * Get active policies
     * 
     * @return A list of active policies
     */
    public List<Policy> getActivePolicies() {
        return this.policies.stream()
                .filter(Policy::isActive)
                .toList();
    }
    
    /**
     * Get policies by type
     * 
     * @param policyType The policy type
     * @return A list of policies of the specified type
     */
    public List<Policy> getPoliciesByType(String policyType) {
        return this.policies.stream()
                .filter(policy -> policy.getPolicyType().equals(policyType))
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
