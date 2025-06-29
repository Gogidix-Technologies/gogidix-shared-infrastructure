package com.exalt.ecosystem.shared.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Permission Entity for Fine-Grained Access Control
 * 
 * SECURITY IMPLEMENTATION: Permission-Based Authorization
 * - Granular permission management
 * - Resource and action based permissions
 * - Audit trail for permission changes
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_name", columnList = "name", unique = true),
    @Index(name = "idx_permission_resource", columnList = "resource"),
    @Index(name = "idx_permission_action", columnList = "action")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "resource", nullable = false, length = 50)
    private String resource;
    
    @Column(name = "action", nullable = false, length = 50)
    private String action;
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    // =============================================
    // AUDIT FIELDS
    // =============================================
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    // =============================================
    // RELATIONSHIPS
    // =============================================
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
    
    // =============================================
    // BUSINESS METHODS
    // =============================================
    
    /**
     * Create permission name from resource and action
     */
    public static String createPermissionName(String resource, String action) {
        return resource.toUpperCase() + "_" + action.toUpperCase();
    }
    
    /**
     * Check if this permission matches resource and action
     */
    public boolean matches(String resource, String action) {
        return this.resource.equals(resource) && this.action.equals(action);
    }
}