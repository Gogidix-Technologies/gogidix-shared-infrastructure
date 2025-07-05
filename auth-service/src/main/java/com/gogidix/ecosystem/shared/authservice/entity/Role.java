package com.gogidix.ecosystem.shared.authservice.entity;

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
 * Role Entity for Role-Based Access Control (RBAC)
 * 
 * SECURITY IMPLEMENTATION: RBAC System
 * - Hierarchical role management
 * - Permission-based authorization
 * - Audit trail for role changes
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    
    @Column(name = "description", length = 255)
    private String description;
    
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
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    // =============================================
    // BUSINESS METHODS
    // =============================================
    
    /**
     * Add permission to role
     */
    public void addPermission(Permission permission) {
        permissions.add(permission);
        permission.getRoles().add(this);
    }
    
    /**
     * Remove permission from role
     */
    public void removePermission(Permission permission) {
        permissions.remove(permission);
        permission.getRoles().remove(this);
    }
    
    /**
     * Check if role has specific permission
     */
    public boolean hasPermission(String permissionName) {
        return permissions.stream()
                         .anyMatch(permission -> permission.getName().equals(permissionName));
    }
    
    /**
     * Get all permission names for this role
     */
    public Set<String> getPermissionNames() {
        Set<String> permissionNames = new HashSet<>();
        for (Permission permission : permissions) {
            permissionNames.add(permission.getName());
        }
        return permissionNames;
    }
}