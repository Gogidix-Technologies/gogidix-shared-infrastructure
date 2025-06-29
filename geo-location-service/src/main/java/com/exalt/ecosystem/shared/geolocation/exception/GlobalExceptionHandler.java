package com.exalt.ecosystem.shared.geolocation.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle GeoLocationException.
     * @param ex The exception
     * @return Error response
     */
    @ExceptionHandler(GeoLocationException.class)
    public ResponseEntity<ApiError> handleGeoLocationException(GeoLocationException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle validation exceptions.
     * @param ex The exception
     * @return Error response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ValidationError validationError = new ValidationError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                errors,
                LocalDateTime.now()
        );
        
        return new ResponseEntity<>(validationError, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle constraint violation exceptions.
     * @param ex The exception
     * @return Error response
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle generic exceptions.
     * @param ex The exception
     * @return Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * DTO for API errors.
     */
    @Data
    @AllArgsConstructor
    public static class ApiError {
        private int status;
        private String message;
        private LocalDateTime timestamp;
    }
    
    /**
     * DTO for validation errors.
     */
    @Data
    @AllArgsConstructor
    public static class ValidationError {
        private int status;
        private String message;
        private Map<String, String> errors;
        private LocalDateTime timestamp;
    }
}
