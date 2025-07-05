package com.gogidix.ecosystem.shared.geolocation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for geocode address responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodeResponse {
    private double latitude;
    private double longitude;
    private String formattedAddress;
}
