package com.gogidix.ecosystem.shared.admin.audit;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.UUID;

/**
 * Implementation of AuditService for admin framework.
 * Provides comprehensive audit logging for compliance and security.
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Value("${admin.audit.enabled:true}")
    private boolean auditEnabled;

    @Value("${admin.audit.async:true}")
    private boolean asyncLogging;

    private final ConcurrentLinkedQueue<AuditEntry> auditQueue = new ConcurrentLinkedQueue<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void logExport(String userId, String exportType, String entityType, int recordCount, boolean success) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "EXPORT", entityType);
        entry.setSuccess(success);
        entry.setDescription(success ? "Export completed successfully" : "Export failed");
        
        // Add details instead of metadata
        entry.addDetail("exportType", exportType);
        entry.addDetail("recordCount", String.valueOf(recordCount));
        entry.addDetail("operation", "EXPORT");

        writeAuditEntry(entry);
    }

    @Override
    public void logDataAccess(String userId, String operation, String entityType, String entityId) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, operation, entityType, entityId, "Data access operation");
        
        entry.addDetail("accessType", "DATA_ACCESS");
        entry.addDetail("operation", operation);

        writeAuditEntry(entry);
    }

    @Override
    public void logConfigChange(String userId, String configKey, String oldValue, String newValue) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "CONFIG_CHANGE", "CONFIGURATION", configKey, "Configuration change");
        
        entry.addDetail("configKey", configKey);
        entry.addDetail("oldValue", oldValue != null ? oldValue : "null");
        entry.addDetail("newValue", newValue != null ? newValue : "null");
        entry.addDetail("operation", "CONFIG_CHANGE");

        writeAuditEntry(entry);
    }

    @Override
    public void logSecurityEvent(String userId, String eventType, String details, String severity) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "SECURITY_EVENT", "SECURITY");
        entry.setDescription(details != null ? details : "Security event");
        entry.setSuccess("INFO".equals(severity));

        entry.addDetail("eventType", eventType);
        entry.addDetail("details", details != null ? details : "No details provided");
        entry.addDetail("severity", severity != null ? severity : "WARNING");
        entry.addDetail("operation", "SECURITY_EVENT");

        writeAuditEntry(entry);
    }

    @Override
    public void logAdminAction(String userId, String action, Map<String, Object> metadata) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "ADMIN_ACTION", "ADMIN");
        entry.setDescription("Admin action: " + action);
        
        entry.addDetail("action", action);
        entry.addDetail("operation", "ADMIN_ACTION");
        
        // Add metadata as details
        if (metadata != null) {
            metadata.forEach((key, value) -> 
                entry.addDetail("meta_" + key, value != null ? value.toString() : "null"));
        }

        writeAuditEntry(entry);
    }

    @Override
    public boolean isAuditEnabled() {
        return auditEnabled;
    }

    /**
     * Write audit entry to storage (async or sync).
     */
    private void writeAuditEntry(AuditEntry entry) {
        if (asyncLogging) {
            CompletableFuture.runAsync(() -> persistAuditEntry(entry));
        } else {
            persistAuditEntry(entry);
        }
    }

    /**
     * Persist audit entry to storage.
     * TODO: Integrate with actual storage (database, file, external service).
     */
    private void persistAuditEntry(AuditEntry entry) {
        try {
            // Add to in-memory queue (for now)
            auditQueue.offer(entry);
            
            // Log to console (in production, this would go to proper logging system)
            String logMessage = String.format(
                "[AUDIT] %s | User: %s | Operation: %s | Entity: %s | Success: %s",
                entry.getCreatedAt().format(formatter),
                entry.getUserId(),
                entry.getOperation(),
                entry.getEntityType(),
                entry.getSuccess()
            );
            
            if (entry.getDetails() != null && !entry.getDetails().isEmpty()) {
                logMessage += " | Details: " + entry.getDetails();
            }
            
            System.out.println(logMessage);
            
            // TODO: Write to database, file, or external audit service
            // Examples:
            // - Save to audit table in database
            // - Write to audit log file
            // - Send to external compliance system
            // - Send to SIEM system
            
        } catch (Exception e) {
            System.err.printf("Failed to persist audit entry: %s%n", e.getMessage());
        }
    }

    /**
     * Generate unique audit ID.
     */
    private String generateAuditId() {
        return "AUDIT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Get audit queue size (for monitoring).
     */
    public int getQueueSize() {
        return auditQueue.size();
    }

    /**
     * Set audit enabled state (for testing).
     */
    public void setAuditEnabled(boolean enabled) {
        this.auditEnabled = enabled;
    }

    /**
     * Set async logging (for testing).
     */
    public void setAsyncLogging(boolean async) {
        this.asyncLogging = async;
    }
}
