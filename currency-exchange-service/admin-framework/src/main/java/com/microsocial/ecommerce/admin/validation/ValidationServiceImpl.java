package com.gogidix.shared.ecommerce.admin.validation;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of ValidationService for admin framework.
 * Provides comprehensive validation for all admin operations.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

    @Value("${admin.validation.enabled:true}")
    private boolean validationEnabled;

    @Value("${admin.file.max-size:10485760}")  // 10MB default
    private long maxFileSize;

    private static final Set<String> SUPPORTED_EXPORT_TYPES = Set.of("CSV", "PDF", "EXCEL", "JSON", "XML");
    private static final Set<String> SUPPORTED_ENTITY_TYPES = Set.of("USERS", "ORDERS", "PRODUCTS", "REPORTS");
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of("application/pdf", "text/csv", "application/vnd.ms-excel");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    @Override
    public ValidationResult validateExportRequest(String exportType, String entityType, Map<String, Object> parameters) {
        if (!isValidationEnabled()) {
            return ValidationResult.valid();
        }

        ValidationResult result = new ValidationResult();

        // Validate export type
        if (exportType == null || exportType.trim().isEmpty()) {
            result.addError("Export type is required");
        } else if (!SUPPORTED_EXPORT_TYPES.contains(exportType.toUpperCase())) {
            result.addError("Unsupported export type: " + exportType + ". Supported types: " + SUPPORTED_EXPORT_TYPES);
        }

        // Validate entity type
        if (entityType == null || entityType.trim().isEmpty()) {
            result.addError("Entity type is required");
        } else if (!SUPPORTED_ENTITY_TYPES.contains(entityType.toUpperCase())) {
            result.addError("Unsupported entity type: " + entityType + ". Supported types: " + SUPPORTED_ENTITY_TYPES);
        }

        // Validate parameters
        if (parameters != null) {
            result.combine(validateExportParameters(parameters));
        }

        return result;
    }

    @Override
    public ValidationResult validateExportTemplate(String templateId, Map<String, Object> templateData) {
        if (!isValidationEnabled()) {
            return ValidationResult.valid();
        }

        ValidationResult result = new ValidationResult();

        // Validate template ID
        if (templateId == null || templateId.trim().isEmpty()) {
            result.addError("Template ID is required");
        } else if (templateId.length() > 50) {
            result.addError("Template ID must be 50 characters or less");
        }

        // Validate template data
        if (templateData == null || templateData.isEmpty()) {
            result.addError("Template data is required");
        } else {
            result.combine(validateTemplateData(templateData));
        }

        return result;
    }

    @Override
    public ValidationResult validateUserInput(Map<String, Object> inputData, Map<String, String> validationRules) {
        if (!isValidationEnabled()) {
            return ValidationResult.valid();
        }

        ValidationResult result = new ValidationResult();

        if (inputData == null) {
            return ValidationResult.invalid("Input data is required");
        }

        if (validationRules != null) {
            for (Map.Entry<String, String> rule : validationRules.entrySet()) {
                String fieldName = rule.getKey();
                String ruleType = rule.getValue();
                Object fieldValue = inputData.get(fieldName);

                result.combine(validateField(fieldName, fieldValue, ruleType));
            }
        }

        return result;
    }

    @Override
    public ValidationResult validateConfigChange(String configKey, Object configValue) {
        if (!isValidationEnabled()) {
            return ValidationResult.valid();
        }

        ValidationResult result = new ValidationResult();

        // Validate config key
        if (configKey == null || configKey.trim().isEmpty()) {
            result.addError("Configuration key is required");
        }

        // Validate based on config key type
        if (configKey != null) {
            result.combine(validateConfigValue(configKey, configValue));
        }

        return result;
    }

    @Override
    public ValidationResult validateFileUpload(String fileName, long fileSize, String contentType) {
        if (!isValidationEnabled()) {
            return ValidationResult.valid();
        }

        ValidationResult result = new ValidationResult();

        // Validate file name
        if (fileName == null || fileName.trim().isEmpty()) {
            result.addError("File name is required");
        } else if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            result.addError("File name contains invalid characters");
        }

        // Validate file size
        if (fileSize <= 0) {
            result.addError("File size must be greater than 0");
        } else if (fileSize > maxFileSize) {
            result.addError("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        // Validate content type
        if (contentType != null && !ALLOWED_FILE_TYPES.contains(contentType)) {
            result.addWarning("File type " + contentType + " may not be supported");
        }

        return result;
    }

    @Override
    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    /**
     * Validate export parameters.
     */
    private ValidationResult validateExportParameters(Map<String, Object> parameters) {
        ValidationResult result = new ValidationResult();

        // Validate limit parameter
        Object limit = parameters.get("limit");
        if (limit != null) {
            try {
                int limitValue = Integer.parseInt(limit.toString());
                if (limitValue <= 0) {
                    result.addError("Limit must be greater than 0");
                } else if (limitValue > 10000) {
                    result.addWarning("Large limit (" + limitValue + ") may impact performance");
                }
            } catch (NumberFormatException e) {
                result.addError("Limit must be a valid number");
            }
        }

        // Validate date range parameters
        Object startDate = parameters.get("startDate");
        Object endDate = parameters.get("endDate");
        if (startDate != null && endDate != null) {
            // Basic date validation (in production, use proper date parsing)
            if (startDate.toString().isEmpty() || endDate.toString().isEmpty()) {
                result.addError("Date range parameters cannot be empty");
            }
        }

        return result;
    }

    /**
     * Validate template data structure.
     */
    private ValidationResult validateTemplateData(Map<String, Object> templateData) {
        ValidationResult result = new ValidationResult();

        // Required fields for template
        String[] requiredFields = {"name", "description", "fields"};
        for (String field : requiredFields) {
            if (!templateData.containsKey(field) || templateData.get(field) == null) {
                result.addError("Template field '" + field + "' is required");
            }
        }

        // Validate fields configuration
        Object fields = templateData.get("fields");
        if (fields instanceof List) {
            List<?> fieldList = (List<?>) fields;
            if (fieldList.isEmpty()) {
                result.addError("Template must have at least one field");
            }
        }

        return result;
    }

    /**
     * Validate individual field based on rule type.
     */
    private ValidationResult validateField(String fieldName, Object fieldValue, String ruleType) {
        ValidationResult result = new ValidationResult();

        switch (ruleType.toUpperCase()) {
            case "REQUIRED":
                if (fieldValue == null || fieldValue.toString().trim().isEmpty()) {
                    result.addError("Field '" + fieldName + "' is required");
                }
                break;
            case "EMAIL":
                if (fieldValue != null && !EMAIL_PATTERN.matcher(fieldValue.toString()).matches()) {
                    result.addError("Field '" + fieldName + "' must be a valid email address");
                }
                break;
            case "NUMERIC":
                if (fieldValue != null) {
                    try {
                        Double.parseDouble(fieldValue.toString());
                    } catch (NumberFormatException e) {
                        result.addError("Field '" + fieldName + "' must be a valid number");
                    }
                }
                break;
            case "POSITIVE":
                if (fieldValue != null) {
                    try {
                        double value = Double.parseDouble(fieldValue.toString());
                        if (value <= 0) {
                            result.addError("Field '" + fieldName + "' must be a positive number");
                        }
                    } catch (NumberFormatException e) {
                        result.addError("Field '" + fieldName + "' must be a valid positive number");
                    }
                }
                break;
            default:
                result.addWarning("Unknown validation rule: " + ruleType);
        }

        return result;
    }

    /**
     * Validate configuration values based on key.
     */
    private ValidationResult validateConfigValue(String configKey, Object configValue) {
        ValidationResult result = new ValidationResult();

        if (configKey.contains("timeout")) {
            // Timeout values should be positive numbers
            try {
                long timeout = Long.parseLong(configValue.toString());
                if (timeout <= 0) {
                    result.addError("Timeout values must be positive");
                }
            } catch (NumberFormatException e) {
                result.addError("Timeout values must be valid numbers");
            }
        } else if (configKey.contains("enabled")) {
            // Boolean configuration values
            if (!"true".equalsIgnoreCase(configValue.toString()) && 
                !"false".equalsIgnoreCase(configValue.toString())) {
                result.addError("Boolean configuration values must be 'true' or 'false'");
            }
        } else if (configKey.contains("email")) {
            // Email configuration values
            if (configValue != null && !EMAIL_PATTERN.matcher(configValue.toString()).matches()) {
                result.addError("Email configuration values must be valid email addresses");
            }
        }

        return result;
    }

    /**
     * Set validation enabled state (for testing).
     */
    public void setValidationEnabled(boolean enabled) {
        this.validationEnabled = enabled;
    }

    /**
     * Set maximum file size (for testing).
     */
    public void setMaxFileSize(long maxSize) {
        this.maxFileSize = maxSize;
    }
}
