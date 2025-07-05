package com.gogidix.ecosystem.shared.geolocation.provider;

import com.gogidix.ecosystem.shared.geolocation.domain.entity.Location;
import com.gogidix.ecosystem.shared.geolocation.exception.GeoLocationException;

import java.util.List;
import java.util.Optional;

/**
 * Interface for geographic location services.
 */
public interface GeoLocationProvider {
    /**
     * Get provider name for identification.
     * @return The name of the provider
     */
    String getProviderName();
    
    /**
     * Convert an address to geographic coordinates.
     * @param address The address to geocode
     * @return Optional containing location if found, empty if not found
     * @throws GeoLocationException if there's an error during geocoding
     */
    Optional<Location> geocodeAddress(String address) throws GeoLocationException;
    
    /**
     * Convert coordinates to an address (reverse geocoding).
     * @param location The location coordinates
     * @return The formatted address or empty if no address found
     * @throws GeoLocationException if there's an error during reverse geocoding
     */
    Optional<String> reverseGeocode(Location location) throws GeoLocationException;
    
    /**
     * Calculate distance between two locations in meters.
     * @param start The starting location
     * @param end The ending location
     * @return The distance in meters
     */
    double calculateDistance(Location start, Location end);
    
    /**
     * Estimate travel time between two locations in seconds.
     * @param start The starting location
     * @param end The ending location
     * @param mode The travel mode (e.g., "driving", "walking", "bicycling", "transit")
     * @return Estimated travel time in seconds or -1 if calculation not possible
     * @throws GeoLocationException if there's an error calculating travel time
     */
    long estimateTravelTime(Location start, Location end, String mode) throws GeoLocationException;
    
    /**
     * Check if a location is within a specified radius of a center point.
     * @param center The center location
     * @param point The point to check
     * @param radiusInMeters The radius in meters
     * @return true if the point is within the radius, false otherwise
     */
    boolean isWithinRadius(Location center, Location point, double radiusInMeters);
    
    /**
     * Search for locations based on a query and optional location bias.
     * @param query The search query
     * @param biasLocation Optional location to bias results toward
     * @param radiusInMeters Search radius in meters when bias location is provided
     * @return List of locations matching the query
     * @throws GeoLocationException if there's an error during search
     */
    List<Location> searchLocations(String query, Optional<Location> biasLocation, double radiusInMeters) throws GeoLocationException;
}
