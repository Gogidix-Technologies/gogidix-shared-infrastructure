package com.exalt.shared.ecommerce.admin.components.policy;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Policy entity for the policy management component.
 * This class defines a policy that can be managed by the
 * policy management component.
 */
public class Policy {
    private UUID id;
    private String name;
    private String description;
    private String policyText;
    private String policyType;
    private LocalDateTime effectiveDate;
    private LocalDateTime expirationDate;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Policy() {
        this.id = UUID.randomUUID();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with name, description, and policy text
     * 
     * @param name The name of the policy
     * @param description The description of the policy
     * @param policyText The text of the policy
     * @param policyType The type of the policy
     */
    public Policy(String name, String description, String policyText, String policyType) {
        this();
        this.name = name;
        this.description = description;
        this.policyText = policyText;
        this.policyType = policyType;
    }
    
    /**
     * Constructor with name, description, policy text, and dates
     * 
     * @param name The name of the policy
     * @param description The description of the policy
     * @param policyText The text of the policy
     * @param policyType The type of the policy
     * @param effectiveDate The effective date of the policy
     * @param expirationDate The expiration date of the policy
     */
    public Policy(String name, String description, String policyText, String policyType,
                 LocalDateTime effectiveDate, LocalDateTime expirationDate) {
        this(name, description, policyText, policyType);
        this.effectiveDate = effectiveDate;
        this.expirationDate = expirationDate;
    }
    
    /**
     * Check if the policy is currently effective
     * 
     * @return true if the policy is currently effective, false otherwise
     */
    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();
        
        boolean afterEffectiveDate = effectiveDate == null || !now.isBefore(effectiveDate);
        boolean beforeExpirationDate = expirationDate == null || !now.isAfter(expirationDate);
        
        return active && afterEffectiveDate && beforeExpirationDate;
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
    
    public String getPolicyText() {
        return policyText;
    }
    
    public void setPolicyText(String policyText) {
        this.policyText = policyText;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPolicyType() {
        return policyType;
    }
    
    public void setPolicyType(String policyType) {
        this.policyType = policyType;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }
    
    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
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
        return "Policy{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", policyType='" + policyType + '\'' +
                ", effectiveDate=" + effectiveDate +
                ", expirationDate=" + expirationDate +
                ", active=" + active +
                '}';
    }
}
