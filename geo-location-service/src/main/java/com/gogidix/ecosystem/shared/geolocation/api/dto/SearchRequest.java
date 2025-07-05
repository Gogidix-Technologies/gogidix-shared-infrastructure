package com.gogidix.ecosystem.shared.geolocation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO for location search requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @NotBlank(message = "Query cannot be blank")
    @Size(min = 2, max = 500, message = "Query must be between 2 and 500 characters")
    private String query;
    
    @Min(value = -90, message = "Bias latitude must be greater than or equal to -90")
    @Max(value = 90, message = "Bias latitude must be less than or equal to 90")
    private Double biasLat;
    
    @Min(value = -180, message = "Bias longitude must be greater than or equal to -180")
    @Max(value = 180, message = "Bias longitude must be less than or equal to 180")
    private Double biasLng;
    
    @Min(value = 1, message = "Radius must be positive")
    @Max(value = 50000, message = "Radius must be less than or equal to 50,000 meters")
    private double radiusMeters = 5000; // Default radius of 5km
}
