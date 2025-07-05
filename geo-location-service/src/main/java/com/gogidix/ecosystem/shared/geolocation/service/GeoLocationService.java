package com.gogidix.ecosystem.shared.geolocation.service;

import com.gogidix.ecosystem.shared.geolocation.domain.entity.Location;
import com.gogidix.ecosystem.shared.geolocation.exception.GeoLocationException;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for geographic location operations.
 */
public interface GeoLocationService {
    /**
     * Convert an address to geographic coordinates.
     * @param address The address to geocode
     * @return Optional containing location if found, empty if not found
     * @throws GeoLocationException if there's an error during geocoding
     */
    Optional<Location> geocodeAddress(String address) throws GeoLocationException;
    
    /**
     * Convert coordinates to an address (reverse geocoding).
     * @param latitude The latitude coordinate
     * @param longitude The longitude coordinate
     * @return The formatted address or empty if no address found
     * @throws GeoLocationException if there's an error during reverse geocoding
     */
    Optional<String> reverseGeocode(double latitude, double longitude) throws GeoLocationException;
    
    /**
     * Calculate distance between two locations in meters.
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return The distance in meters
     */
    double calculateDistance(double startLat, double startLng, double endLat, double endLng);
    
    /**
     * Estimate travel time between two locations in seconds.
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @param mode The travel mode (e.g., "driving", "walking", "bicycling", "transit")
     * @return Estimated travel time in seconds or -1 if calculation not possible
     * @throws GeoLocationException if there's an error calculating travel time
     */
    long estimateTravelTime(double startLat, double startLng, double endLat, double endLng, String mode) throws GeoLocationException;
    
    /**
     * Check if a location is within a specified radius of a center point.
     * @param centerLat Center latitude
     * @param centerLng Center longitude
     * @param pointLat Point latitude to check
     * @param pointLng Point longitude to check
     * @param radiusInMeters The radius in meters
     * @return true if the point is within the radius, false otherwise
     */
    boolean isWithinRadius(double centerLat, double centerLng, double pointLat, double pointLng, double radiusInMeters);
    
    /**
     * Search for locations based on a query and optional location bias.
     * @param query The search query
     * @param biasLat Optional latitude to bias results toward (can be null)
     * @param biasLng Optional longitude to bias results toward (can be null)
     * @param radiusInMeters Search radius in meters when bias location is provided
     * @return List of locations matching the query
     * @throws GeoLocationException if there's an error during search
     */
    List<Location> searchLocations(String query, Double biasLat, Double biasLng, double radiusInMeters) throws GeoLocationException;
    
    /**
     * Get the current provider name being used.
     * @return The name of the active provider
     */
    String getCurrentProviderName();
}
