package com.exalt.ecosystem.shared.geolocation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for geocode address requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeRequest {
    @NotBlank(message = "Address cannot be blank")
    @Size(min = 3, max = 500, message = "Address must be between 3 and 500 characters")
    private String address;
}
