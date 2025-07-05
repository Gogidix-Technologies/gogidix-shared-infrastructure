package com.gogidix.shared.ecommerce.admin.validation;

import java.util.List;
import java.util.Map;

/**
 * Service for validating data in the admin framework.
 * Provides comprehensive validation for exports, inputs, and configurations.
 */
public interface ValidationService {

    /**
     * Validate export request parameters.
     * 
     * @param exportType Export format type
     * @param entityType Type of entity to export
     * @param parameters Export parameters
     * @return Validation result
     */
    ValidationResult validateExportRequest(String exportType, String entityType, Map<String, Object> parameters);

    /**
     * Validate export template configuration.
     * 
     * @param templateId Template identifier
     * @param templateData Template configuration data
     * @return Validation result
     */
    ValidationResult validateExportTemplate(String templateId, Map<String, Object> templateData);

    /**
     * Validate user input data.
     * 
     * @param inputData User input data to validate
     * @param validationRules Validation rules to apply
     * @return Validation result
     */
    ValidationResult validateUserInput(Map<String, Object> inputData, Map<String, String> validationRules);

    /**
     * Validate configuration change.
     * 
     * @param configKey Configuration key
     * @param configValue New configuration value
     * @return Validation result
     */
    ValidationResult validateConfigChange(String configKey, Object configValue);

    /**
     * Validate file upload.
     * 
     * @param fileName Name of uploaded file
     * @param fileSize Size of uploaded file in bytes
     * @param contentType MIME type of uploaded file
     * @return Validation result
     */
    ValidationResult validateFileUpload(String fileName, long fileSize, String contentType);

    /**
     * Check if validation service is enabled.
     * 
     * @return true if validation is active
     */
    boolean isValidationEnabled();
}
