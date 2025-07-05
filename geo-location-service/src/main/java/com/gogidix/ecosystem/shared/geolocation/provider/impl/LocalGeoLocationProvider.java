package com.gogidix.ecosystem.shared.geolocation.provider.impl;

import com.gogidix.ecosystem.shared.geolocation.domain.entity.Location;
import com.gogidix.ecosystem.shared.geolocation.domain.entity.SimpleLocation;
import com.gogidix.ecosystem.shared.geolocation.exception.GeoLocationException;
import com.gogidix.ecosystem.shared.geolocation.provider.GeoLocationProvider;
import com.gogidix.ecosystem.shared.geolocation.util.GeoUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * A simple local implementation of GeoLocationProvider for development and testing.
 * Uses a small in-memory database of locations.
 */
@Component
public class LocalGeoLocationProvider implements GeoLocationProvider {
    // In-memory database of locations
    private final Map<String, SimpleLocation> locationDatabase;
    
    public LocalGeoLocationProvider() {
        locationDatabase = new HashMap<>();
        // Populate with some sample locations
        initializeLocationDatabase();
    }
    
    @Override
    public String getProviderName() {
        return "local";
    }
    
    @Override
    public Optional<Location> geocodeAddress(String address) throws GeoLocationException {
        // Simple case-insensitive partial match
        String searchTerm = address.toLowerCase().trim();
        
        return locationDatabase.entrySet().stream()
                .filter(entry -> entry.getKey().toLowerCase().contains(searchTerm))
                .findFirst()
                .map(Map.Entry::getValue);
    }
    
    @Override
    public Optional<String> reverseGeocode(Location location) throws GeoLocationException {
        // Find the closest location in our database
        double minDistance = Double.MAX_VALUE;
        String closestAddress = null;
        
        for (Map.Entry<String, SimpleLocation> entry : locationDatabase.entrySet()) {
            SimpleLocation loc = entry.getValue();
            double distance = GeoUtils.haversineDistance(
                    location.getLatitude(), location.getLongitude(),
                    loc.getLatitude(), loc.getLongitude());
            
            if (distance < minDistance) {
                minDistance = distance;
                closestAddress = entry.getKey();
            }
        }
        
        // Only return if within 2km (arbitrary threshold for this mock provider)
        if (minDistance <= 2000) {
            return Optional.ofNullable(closestAddress);
        }
        
        return Optional.empty();
    }
    
    @Override
    public double calculateDistance(Location start, Location end) {
        return GeoUtils.haversineDistance(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude());
    }
    
    @Override
    public long estimateTravelTime(Location start, Location end, String mode) throws GeoLocationException {
        double distance = calculateDistance(start, end);
        
        // Very simplified model just for testing
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
    }
    
    @Override
    public boolean isWithinRadius(Location center, Location point, double radiusInMeters) {
        double distance = calculateDistance(center, point);
        return distance <= radiusInMeters;
    }
    
    @Override
    public List<Location> searchLocations(String query, Optional<Location> biasLocation, double radiusInMeters) 
            throws GeoLocationException {
        String searchTerm = query.toLowerCase().trim();
        List<Location> results = new ArrayList<>();
        
        for (Map.Entry<String, SimpleLocation> entry : locationDatabase.entrySet()) {
            if (entry.getKey().toLowerCase().contains(searchTerm)) {
                // If we have a bias location, only include results within the radius
                if (biasLocation.isPresent()) {
                    if (isWithinRadius(biasLocation.get(), entry.getValue(), radiusInMeters)) {
                        results.add(entry.getValue());
                    }
                } else {
                    results.add(entry.getValue());
                }
            }
        }
        
        return results;
    }
    
    private void initializeLocationDatabase() {
        // Add some major cities
        locationDatabase.put("New York, NY, USA", new SimpleLocation(40.7128, -74.0060, "New York"));
        locationDatabase.put("Los Angeles, CA, USA", new SimpleLocation(34.0522, -118.2437, "Los Angeles"));
        locationDatabase.put("Chicago, IL, USA", new SimpleLocation(41.8781, -87.6298, "Chicago"));
        locationDatabase.put("Houston, TX, USA", new SimpleLocation(29.7604, -95.3698, "Houston"));
        locationDatabase.put("Phoenix, AZ, USA", new SimpleLocation(33.4484, -112.0740, "Phoenix"));
        locationDatabase.put("Philadelphia, PA, USA", new SimpleLocation(39.9526, -75.1652, "Philadelphia"));
        locationDatabase.put("San Antonio, TX, USA", new SimpleLocation(29.4241, -98.4936, "San Antonio"));
        locationDatabase.put("San Diego, CA, USA", new SimpleLocation(32.7157, -117.1611, "San Diego"));
        locationDatabase.put("Dallas, TX, USA", new SimpleLocation(32.7767, -96.7970, "Dallas"));
        locationDatabase.put("San Jose, CA, USA", new SimpleLocation(37.3382, -121.8863, "San Jose"));
        
        // International cities
        locationDatabase.put("London, UK", new SimpleLocation(51.5074, -0.1278, "London"));
        locationDatabase.put("Paris, France", new SimpleLocation(48.8566, 2.3522, "Paris"));
        locationDatabase.put("Tokyo, Japan", new SimpleLocation(35.6762, 139.6503, "Tokyo"));
        locationDatabase.put("Sydney, Australia", new SimpleLocation(33.8688, 151.2093, "Sydney"));
        locationDatabase.put("Rio de Janeiro, Brazil", new SimpleLocation(-22.9068, -43.1729, "Rio de Janeiro"));
        locationDatabase.put("Cape Town, South Africa", new SimpleLocation(-33.9249, 18.4241, "Cape Town"));
        locationDatabase.put("Mexico City, Mexico", new SimpleLocation(19.4326, -99.1332, "Mexico City"));
        locationDatabase.put("Beijing, China", new SimpleLocation(39.9042, 116.4074, "Beijing"));
        locationDatabase.put("Moscow, Russia", new SimpleLocation(55.7558, 37.6173, "Moscow"));
        locationDatabase.put("Berlin, Germany", new SimpleLocation(52.5200, 13.4050, "Berlin"));
        
        // Add landmarks
        locationDatabase.put("Eiffel Tower, Paris, France", new SimpleLocation(48.8584, 2.2945, "Eiffel Tower"));
        locationDatabase.put("Statue of Liberty, NY, USA", new SimpleLocation(40.6892, -74.0445, "Statue of Liberty"));
        locationDatabase.put("Golden Gate Bridge, SF, USA", new SimpleLocation(37.8199, -122.4783, "Golden Gate Bridge"));
        locationDatabase.put("Sydney Opera House, Australia", new SimpleLocation(-33.8568, 151.2153, "Sydney Opera House"));
        locationDatabase.put("Great Wall of China, China", new SimpleLocation(40.4319, 116.5704, "Great Wall of China"));
        locationDatabase.put("Taj Mahal, Agra, India", new SimpleLocation(27.1751, 78.0421, "Taj Mahal"));
        locationDatabase.put("Colosseum, Rome, Italy", new SimpleLocation(41.8902, 12.4922, "Colosseum"));
        locationDatabase.put("Machu Picchu, Peru", new SimpleLocation(-13.1631, -72.5450, "Machu Picchu"));
        locationDatabase.put("Burj Khalifa, Dubai, UAE", new SimpleLocation(25.1972, 55.2744, "Burj Khalifa"));
        locationDatabase.put("Stonehenge, UK", new SimpleLocation(51.1789, -1.8262, "Stonehenge"));
    }
}
