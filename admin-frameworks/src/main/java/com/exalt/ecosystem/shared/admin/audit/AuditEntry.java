package com.exalt.ecosystem.shared.admin.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents an audit entry for tracking administrative operations.
 * Used for compliance, security monitoring, and operational tracking.
 */
@Entity
@Table(name = "audit_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "operation", nullable = false)
    private String operation;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "description")
    private String description;

    @ElementCollection
    @CollectionTable(name = "audit_entry_details", joinColumns = @JoinColumn(name = "audit_entry_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value")
    private Map<String, String> details;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "tenant_id")
    private String tenantId;

    /**
     * Constructor for basic audit entry creation.
     * 
     * @param userId The ID of the user performing the operation
     * @param operation The type of operation being performed
     * @param entityType The type of entity being operated on
     */
    public AuditEntry(String userId, String operation, String entityType) {
        this.userId = userId;
        this.operation = operation;
        this.entityType = entityType;
        this.createdAt = LocalDateTime.now();
        this.success = true; // Default to success, can be updated
    }

    /**
     * Constructor with additional details.
     * 
     * @param userId The ID of the user performing the operation
     * @param operation The type of operation being performed
     * @param entityType The type of entity being operated on
     * @param entityId The ID of the specific entity
     * @param description A description of the operation
     */
    public AuditEntry(String userId, String operation, String entityType, String entityId, String description) {
        this(userId, operation, entityType);
        this.entityId = entityId;
        this.description = description;
    }

    /**
     * Add a detail to the audit entry.
     * 
     * @param key The detail key
     * @param value The detail value
     */
    public void addDetail(String key, String value) {
        if (this.details == null) {
            this.details = new java.util.HashMap<>();
        }
        this.details.put(key, value);
    }

    /**
     * Mark the operation as failed with an error message.
     * 
     * @param errorMessage The error message
     */
    public void markAsFailed(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    /**
     * Set context information for the audit entry.
     * 
     * @param ipAddress The IP address of the request
     * @param userAgent The user agent of the request
     * @param sessionId The session ID
     */
    public void setContext(String ipAddress, String userAgent, String sessionId) {
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}