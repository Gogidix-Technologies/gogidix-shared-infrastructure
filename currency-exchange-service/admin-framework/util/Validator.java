package com.exalt.shared.ecommerce.admin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Validator utility for validating objects.
 * This class provides validation functionality for objects.
 * 
 * @param <T> The type of object to validate
 */
public class Validator<T> {
    
    private final T object;
    private final List<ValidationError> errors;
    
    /**
     * Constructor with object
     * 
     * @param object The object to validate
     */
    public Validator(T object) {
        this.object = object;
        this.errors = new ArrayList<>();
    }
    
    /**
     * Validate a field
     * 
     * @param fieldName The name of the field
     * @param getter The getter function for the field
     * @param predicate The predicate to validate the field
     * @param errorMessage The error message if validation fails
     * @return This validator for chaining
     */
    public Validator<T> validate(String fieldName, FieldGetter<T> getter, 
                                Predicate<Object> predicate, String errorMessage) {
        Object fieldValue = getter.get(object);
        if (!predicate.test(fieldValue)) {
            errors.add(new ValidationError(fieldName, errorMessage));
        }
        return this;
    }
    
    /**
     * Check if validation has errors
     * 
     * @return true if validation has errors, false otherwise
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Get validation errors
     * 
     * @return The list of validation errors
     */
    public List<ValidationError> getErrors() {
        return errors;
    }
    
    /**
     * Get the validated object
     * 
     * @return The validated object
     */
    public T getObject() {
        return object;
    }
    
    /**
     * Functional interface for getting a field value
     * 
     * @param <T> The type of object
     */
    @FunctionalInterface
    public interface FieldGetter<T> {
        Object get(T object);
    }
    
    /**
     * Validation error
     */
    public static class ValidationError {
        private final String fieldName;
        private final String message;
        
        /**
         * Constructor with field name and message
         * 
         * @param fieldName The name of the field
         * @param message The error message
         */
        public ValidationError(String fieldName, String message) {
            this.fieldName = fieldName;
            this.message = message;
        }
        
        /**
         * Get the field name
         * 
         * @return The field name
         */
        public String getFieldName() {
            return fieldName;
        }
        
        /**
         * Get the error message
         * 
         * @return The error message
         */
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return fieldName + ": " + message;
        }
    }
}
