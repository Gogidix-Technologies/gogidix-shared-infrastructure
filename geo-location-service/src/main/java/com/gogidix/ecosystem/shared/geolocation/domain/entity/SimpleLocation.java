package com.gogidix.ecosystem.shared.geolocation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serializable;

/**
 * Simple implementation of the Location interface.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SimpleLocation implements Location, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private double latitude;
    private double longitude;
    private String name;
    private String formattedAddress;
    
    /**
     * Constructor with only coordinates.
     * @param latitude The latitude
     * @param longitude The longitude
     */
    public SimpleLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    /**
     * Constructor with coordinates and name.
     * @param latitude The latitude
     * @param longitude The longitude
     * @param name The location name
     */
    public SimpleLocation(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }
}
