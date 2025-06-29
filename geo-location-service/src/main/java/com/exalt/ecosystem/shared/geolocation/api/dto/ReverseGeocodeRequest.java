package com.exalt.ecosystem.shared.geolocation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for reverse geocode requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReverseGeocodeRequest {
    @NotNull(message = "Latitude cannot be null")
    @Min(value = -90, message = "Latitude must be greater than or equal to -90")
    @Max(value = 90, message = "Latitude must be less than or equal to 90")
    private Double latitude;
    
    @NotNull(message = "Longitude cannot be null")
    @Min(value = -180, message = "Longitude must be greater than or equal to -180")
    @Max(value = 180, message = "Longitude must be less than or equal to 180")
    private Double longitude;
}
