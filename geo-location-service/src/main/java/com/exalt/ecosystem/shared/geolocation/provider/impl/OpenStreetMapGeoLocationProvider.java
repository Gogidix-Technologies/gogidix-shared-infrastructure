package com.exalt.ecosystem.shared.geolocation.provider.impl;

import com.exalt.ecosystem.shared.geolocation.domain.entity.Location;
import com.exalt.ecosystem.shared.geolocation.domain.entity.SimpleLocation;
import com.exalt.ecosystem.shared.geolocation.exception.GeoLocationException;
import com.exalt.ecosystem.shared.geolocation.provider.GeoLocationProvider;
import com.exalt.ecosystem.shared.geolocation.util.GeoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenStreetMap implementation of GeoLocationProvider.
 * Uses the free Nominatim API.
 */
@Component
public class OpenStreetMapGeoLocationProvider implements GeoLocationProvider {
    private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapGeoLocationProvider.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${geo-location.provider.openstreetmap.url:https://nominatim.openstreetmap.org}")
    private String nominatimUrl;
    
    @Value("${geo-location.provider.openstreetmap.email:contact@example.com}")
    private String contactEmail;
    
    public OpenStreetMapGeoLocationProvider() {
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public String getProviderName() {
        return "openstreetmap";
    }
    
    @Override
    public Optional<Location> geocodeAddress(String address) throws GeoLocationException {
        try {
            // This is a simplified implementation - in a real app, you would:
            // 1. Make the actual API call to Nominatim Geocoding API
            // 2. Parse the JSON response
            // 3. Extract the latitude and longitude
            
            // For now, we'll simulate this behavior for demonstration
            logger.info("Geocoding address with OpenStreetMap: {}", address);
            
            // In a real implementation, this would be the actual API call
            // Example URL: https://nominatim.openstreetmap.org/search?q=135+pilkington+avenue,+birmingham&format=json
            
            // Set up headers with a User-Agent as required by the Nominatim Usage Policy
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MicroEcommerce-GeoLocation-Service/" + contactEmail);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Simulating a successful response for demonstration
            if (address.toLowerCase().contains("invalid")) {
                return Optional.empty();
            }
            
            // Return a simulated result
            double latitude = 37.422 + Math.random() * 0.01;
            double longitude = -122.084 + Math.random() * 0.01;
            
            return Optional.of(new SimpleLocation(latitude, longitude, "OpenStreetMap Result"));
        } catch (Exception e) {
            throw new GeoLocationException("Error geocoding address with OpenStreetMap: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<String> reverseGeocode(Location location) throws GeoLocationException {
        try {
            // This would be the actual API call in a real implementation
            // Example URL: https://nominatim.openstreetmap.org/reverse?lat=51.5074&lon=-0.1278&format=json
            
            logger.info("Reverse geocoding location with OpenStreetMap: {}, {}", 
                    location.getLatitude(), location.getLongitude());
            
            // Set up headers with a User-Agent as required by the Nominatim Usage Policy
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MicroEcommerce-GeoLocation-Service/" + contactEmail);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Simulating a successful response for demonstration
            return Optional.of(String.format("OSM Address at %.6f, %.6f", 
                    location.getLatitude(), location.getLongitude()));
        } catch (Exception e) {
            throw new GeoLocationException("Error reverse geocoding with OpenStreetMap: " + e.getMessage(), e);
        }
    }
    
    @Override
    public double calculateDistance(Location start, Location end) {
        return GeoUtils.haversineDistance(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude());
    }
    
    @Override
    public long estimateTravelTime(Location start, Location end, String mode) throws GeoLocationException {
        try {
            // OpenStreetMap doesn't have a direct travel time API like Google Maps
            // For a real implementation, you might use OSRM (Open Source Routing Machine)
            // or just estimate based on distance and mode
            
            logger.info("Estimating travel time with OpenStreetMap from {},{} to {},{} via {}", 
                    start.getLatitude(), start.getLongitude(), 
                    end.getLatitude(), end.getLongitude(), mode);
            
            // Simple estimation based on distance and mode
            double distance = calculateDistance(start, end);
            double speedMps; // meters per second
            
            switch (mode.toLowerCase()) {
                case "walking":
                    speedMps = 1.4; // ~5 km/h
                    break;
                case "bicycling":
                    speedMps = 4.2; // ~15 km/h
                    break;
                case "transit":
                    speedMps = 8.3; // ~30 km/h
                    break;
                case "driving":
                    speedMps = 13.9; // ~50 km/h
                    break;
                default:
                    throw new GeoLocationException("Unsupported travel mode: " + mode);
            }
            
            return Math.round(distance / speedMps);
        } catch (Exception e) {
            throw new GeoLocationException("Error estimating travel time with OpenStreetMap: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isWithinRadius(Location center, Location point, double radiusInMeters) {
        double distance = calculateDistance(center, point);
        return distance <= radiusInMeters;
    }
    
    @Override
    public List<Location> searchLocations(String query, Optional<Location> biasLocation, double radiusInMeters) 
            throws GeoLocationException {
        try {
            // Nominatim can be used for searching, but has limitations
            // In a real implementation, you might also consider Overpass API
            
            logger.info("Searching locations with OpenStreetMap: {}", query);
            
            // Set up headers with a User-Agent as required by the Nominatim Usage Policy
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "MicroEcommerce-GeoLocation-Service/" + contactEmail);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Simulate a result for demonstration
            SimpleLocation location = new SimpleLocation(
                    37.7749 + (Math.random() * 0.02 - 0.01), 
                    -122.4194 + (Math.random() * 0.02 - 0.01),
                    "OpenStreetMap Result for: " + query);
            
            return Collections.singletonList(location);
        } catch (Exception e) {
            throw new GeoLocationException("Error searching locations with OpenStreetMap: " + e.getMessage(), e);
        }
    }
}
