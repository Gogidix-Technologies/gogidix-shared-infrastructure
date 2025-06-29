package com.exalt.ecosystem.shared.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Password History Entity for Password Reuse Prevention
 * 
 * SECURITY IMPLEMENTATION: Password History Tracking
 * - Prevents password reuse for specified number of changes
 * - Maintains audit trail of password changes
 * - Supports compliance requirements for password rotation
 */
@Entity
@Table(name = "password_history", indexes = {
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * Hashed password from history
     * Used for checking against new password to prevent reuse
     */
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;
    
    /**
     * When this password was created
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * IP address from which password was changed
     */
    @Column(name = "changed_from_ip", length = 45)
    private String changedFromIp;
    
    /**
     * Reason for password change (RESET, EXPIRED, USER_INITIATED, ADMIN_FORCED)
     */
    @Column(name = "change_reason", length = 50)
    private String changeReason;
    
    // =============================================
    // CONSTRUCTORS
    // =============================================
    
    public PasswordHistory(User user, String passwordHash, String changedFromIp, String changeReason) {
        this.user = user;
        this.passwordHash = passwordHash;
        this.changedFromIp = changedFromIp;
        this.changeReason = changeReason;
    }
}