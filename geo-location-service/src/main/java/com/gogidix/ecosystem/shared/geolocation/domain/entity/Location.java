package com.gogidix.ecosystem.shared.geolocation.domain.entity;

import java.io.Serializable;

/**
 * Interface representing a geographic location with latitude and longitude.
 */
public interface Location extends Serializable {
    /**
     * Gets the latitude coordinate.
     * @return the latitude in decimal degrees
     */
    double getLatitude();
    
    /**
     * Gets the longitude coordinate.
     * @return the longitude in decimal degrees
     */
    double getLongitude();
}
