package com.exalt.shared.ecommerce.admin.notification;

import java.util.Map;

/**
 * Service for sending notifications in the admin framework.
 * Focused on essential admin operations like export completion and system alerts.
 */
public interface NotificationService {

    /**
     * Send email notification for export completion.
     * 
     * @param email Recipient email
     * @param fileName Name of exported file
     * @param fileSize Size of exported file
     * @return true if sent successfully
     */
    boolean notifyExportComplete(String email, String fileName, long fileSize);

    /**
     * Send email notification for export failure.
     * 
     * @param email Recipient email
     * @param fileName Name of failed export
     * @param errorMessage Error details
     * @return true if sent successfully
     */
    boolean notifyExportFailed(String email, String fileName, String errorMessage);

    /**
     * Send system alert to admin users.
     * 
     * @param message Alert message
     * @param severity Severity level (INFO, WARNING, ERROR)
     * @return true if sent successfully
     */
    boolean sendSystemAlert(String message, String severity);

    /**
     * Send custom email with template variables.
     * 
     * @param email Recipient email
     * @param templateId Template identifier
     * @param variables Template variables
     * @return true if sent successfully
     */
    boolean sendTemplatedEmail(String email, String templateId, Map<String, Object> variables);

    /**
     * Check if notification service is available.
     * 
     * @return true if service is configured and ready
     */
    boolean isAvailable();
}
