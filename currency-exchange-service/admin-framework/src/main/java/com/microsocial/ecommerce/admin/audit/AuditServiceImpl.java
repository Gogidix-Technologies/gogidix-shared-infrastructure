package com.exalt.shared.ecommerce.admin.audit;

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
        entry.setId(generateAuditId());
        entry.setSuccess(success);
        entry.setSeverity(success ? "INFO" : "WARNING");
        
        Map<String, Object> metadata = Map.of(
            "exportType", exportType,
            "recordCount", recordCount,
            "operation", "EXPORT"
        );
        entry.setMetadata(metadata);

        writeAuditEntry(entry);
    }

    @Override
    public void logDataAccess(String userId, String operation, String entityType, String entityId) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, operation, entityType);
        entry.setId(generateAuditId());
        entry.setEntityId(entityId);
        entry.setSeverity("INFO");

        Map<String, Object> metadata = Map.of(
            "accessType", "DATA_ACCESS",
            "operation", operation
        );
        entry.setMetadata(metadata);

        writeAuditEntry(entry);
    }

    @Override
    public void logConfigChange(String userId, String configKey, String oldValue, String newValue) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "CONFIG_CHANGE", "CONFIGURATION");
        entry.setId(generateAuditId());
        entry.setEntityId(configKey);
        entry.setSeverity("WARNING");

        Map<String, Object> metadata = Map.of(
            "configKey", configKey,
            "oldValue", oldValue != null ? oldValue : "null",
            "newValue", newValue != null ? newValue : "null",
            "operation", "CONFIG_CHANGE"
        );
        entry.setMetadata(metadata);

        writeAuditEntry(entry);
    }

    @Override
    public void logSecurityEvent(String userId, String eventType, String details, String severity) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "SECURITY_EVENT", "SECURITY");
        entry.setId(generateAuditId());
        entry.setSeverity(severity != null ? severity : "WARNING");
        entry.setSuccess("INFO".equals(severity));

        Map<String, Object> metadata = Map.of(
            "eventType", eventType,
            "details", details != null ? details : "No details provided",
            "operation", "SECURITY_EVENT"
        );
        entry.setMetadata(metadata);

        writeAuditEntry(entry);
    }

    @Override
    public void logAdminAction(String userId, String action, Map<String, Object> metadata) {
        if (!isAuditEnabled()) return;

        AuditEntry entry = new AuditEntry(userId, "ADMIN_ACTION", "ADMIN");
        entry.setId(generateAuditId());
        entry.setSeverity("INFO");
        entry.setMetadata(metadata);

        Map<String, Object> enrichedMetadata = Map.of(
            "action", action,
            "additionalData", metadata != null ? metadata : Map.of(),
            "operation", "ADMIN_ACTION"
        );
        entry.setMetadata(enrichedMetadata);

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
                "[AUDIT] %s | User: %s | Operation: %s | Entity: %s | Success: %s | Severity: %s",
                entry.getTimestamp().format(formatter),
                entry.getUserId(),
                entry.getOperation(),
                entry.getEntityType(),
                entry.isSuccess(),
                entry.getSeverity()
            );
            
            if (entry.getMetadata() != null && !entry.getMetadata().isEmpty()) {
                logMessage += " | Metadata: " + entry.getMetadata();
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
