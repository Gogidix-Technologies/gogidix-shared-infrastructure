package com.gogidix.shared.ecommerce.admin.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Audit log entry representing a tracked admin operation.
 */
public class AuditEntry {
    
    private String id;
    private String userId;
    private String operation;
    private String entityType;
    private String entityId;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private Map<String, Object> metadata;
    private String severity;
    private boolean success;

    /**
     * Default constructor.
     */
    public AuditEntry() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor with basic fields.
     */
    public AuditEntry(String userId, String operation, String entityType) {
        this();
        this.userId = userId;
        this.operation = operation;
        this.entityType = entityType;
        this.success = true;
        this.severity = "INFO";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s %s on %s (Success: %s)", 
                timestamp, userId, operation, entityType, entityId, success);
    }
}
