package com.exalt.shared.ecommerce.admin.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a validation operation containing success status and error messages.
 */
public class ValidationResult {
    
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;

    /**
     * Default constructor creating a valid result.
     */
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    /**
     * Constructor with validity status.
     */
    public ValidationResult(boolean valid) {
        this();
        this.valid = valid;
    }

    /**
     * Static method to create a valid result.
     */
    public static ValidationResult valid() {
        return new ValidationResult(true);
    }

    /**
     * Static method to create an invalid result with error.
     */
    public static ValidationResult invalid(String error) {
        ValidationResult result = new ValidationResult(false);
        result.addError(error);
        return result;
    }

    /**
     * Add an error message and mark as invalid.
     */
    public ValidationResult addError(String error) {
        this.valid = false;
        this.errors.add(error);
        return this;
    }

    /**
     * Add a warning message (doesn't affect validity).
     */
    public ValidationResult addWarning(String warning) {
        this.warnings.add(warning);
        return this;
    }

    /**
     * Add multiple errors.
     */
    public ValidationResult addErrors(List<String> errors) {
        if (errors != null && !errors.isEmpty()) {
            this.valid = false;
            this.errors.addAll(errors);
        }
        return this;
    }

    /**
     * Combine this result with another validation result.
     */
    public ValidationResult combine(ValidationResult other) {
        if (other != null) {
            if (!other.isValid()) {
                this.valid = false;
            }
            this.errors.addAll(other.getErrors());
            this.warnings.addAll(other.getWarnings());
        }
        return this;
    }

    // Getters
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
    public List<String> getWarnings() { return warnings; }

    public boolean hasErrors() { return !errors.isEmpty(); }
    public boolean hasWarnings() { return !warnings.isEmpty(); }

    public String getFirstError() {
        return errors.isEmpty() ? null : errors.get(0);
    }

    public String getAllErrors() {
        return String.join("; ", errors);
    }

    public String getAllWarnings() {
        return String.join("; ", warnings);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValidationResult{valid=").append(valid);
        
        if (!errors.isEmpty()) {
            sb.append(", errors=").append(errors);
        }
        
        if (!warnings.isEmpty()) {
            sb.append(", warnings=").append(warnings);
        }
        
        sb.append("}");
        return sb.toString();
    }
}
