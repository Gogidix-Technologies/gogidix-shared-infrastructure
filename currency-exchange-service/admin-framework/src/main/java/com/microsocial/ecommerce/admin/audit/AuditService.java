package com.gogidix.shared.ecommerce.admin.audit;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service for audit logging in the admin framework.
 * Tracks admin actions, data access, and system changes for compliance.
 */
public interface AuditService {

    /**
     * Log export operation.
     * 
     * @param userId User performing the export
     * @param exportType Type of export (CSV, PDF, Excel)
     * @param entityType Type of data exported (orders, customers, etc.)
     * @param recordCount Number of records exported
     * @param success Whether export was successful
     */
    void logExport(String userId, String exportType, String entityType, int recordCount, boolean success);

    /**
     * Log data access operation.
     * 
     * @param userId User accessing data
     * @param operation Operation type (READ, CREATE, UPDATE, DELETE)
     * @param entityType Type of entity accessed
     * @param entityId ID of specific entity (if applicable)
     */
    void logDataAccess(String userId, String operation, String entityType, String entityId);

    /**
     * Log configuration change.
     * 
     * @param userId User making the change
     * @param configKey Configuration key changed
     * @param oldValue Previous value
     * @param newValue New value
     */
    void logConfigChange(String userId, String configKey, String oldValue, String newValue);

    /**
     * Log security event.
     * 
     * @param userId User involved (can be null for system events)
     * @param eventType Type of security event
     * @param details Additional event details
     * @param severity Severity level (INFO, WARNING, ERROR)
     */
    void logSecurityEvent(String userId, String eventType, String details, String severity);

    /**
     * Log custom admin action.
     * 
     * @param userId User performing action
     * @param action Action description
     * @param metadata Additional metadata
     */
    void logAdminAction(String userId, String action, Map<String, Object> metadata);

    /**
     * Check if audit logging is enabled.
     * 
     * @return true if audit logging is active
     */
    boolean isAuditEnabled();
}
