package com.gogidix.ecosystem.shared.geolocation.api.controller;

import com.gogidix.ecosystem.shared.geolocation.api.dto.GeocodeRequest;
import com.gogidix.ecosystem.shared.geolocation.api.dto.GeocodeResponse;
import com.gogidix.ecosystem.shared.geolocation.api.dto.ReverseGeocodeRequest;
import com.gogidix.ecosystem.shared.geolocation.api.dto.SearchRequest;
import com.gogidix.ecosystem.shared.geolocation.domain.entity.Location;
import com.gogidix.ecosystem.shared.geolocation.exception.GeoLocationException;
import com.gogidix.ecosystem.shared.geolocation.service.GeoLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for geo-location operations.
 */
@RestController
@RequestMapping("/api/v1/geo")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Geo-Location", description = "API for geo-location operations")
public class GeoLocationController {
    private final GeoLocationService geoLocationService;
    
    /**
     * Get the current provider name.
     * @return The name of the active provider
     */
    @GetMapping("/provider")
    @Operation(summary = "Get current provider", description = "Returns the name of the active geo-location provider")
    public ResponseEntity<String> getProvider() {
        return ResponseEntity.ok(geoLocationService.getCurrentProviderName());
    }
    
    /**
     * Convert an address to coordinates.
     * @param request The geocode request with address
     * @return The coordinates
     */
    @PostMapping("/geocode")
    @Operation(summary = "Geocode address", description = "Convert an address to geographic coordinates")
    public ResponseEntity<GeocodeResponse> geocodeAddress(@Valid @RequestBody GeocodeRequest request) {
        try {
            Optional<Location> location = geoLocationService.geocodeAddress(request.getAddress());
            
            if (location.isPresent()) {
                GeocodeResponse response = new GeocodeResponse(
                        location.get().getLatitude(),
                        location.get().getLongitude(),
                        request.getAddress());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (GeoLocationException e) {
            log.error("Error geocoding address: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Convert coordinates to an address.
     * @param request The reverse geocode request with coordinates
     * @return The formatted address
     */
    @PostMapping("/reverse-geocode")
    @Operation(summary = "Reverse geocode", description = "Convert geographic coordinates to an address")
    public ResponseEntity<String> reverseGeocode(@Valid @RequestBody ReverseGeocodeRequest request) {
        try {
            Optional<String> address = geoLocationService.reverseGeocode(
                    request.getLatitude(), request.getLongitude());
            
            if (address.isPresent()) {
                return ResponseEntity.ok(address.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (GeoLocationException e) {
            log.error("Error reverse geocoding: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Calculate distance between two points.
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return The distance in meters
     */
    @GetMapping("/distance")
    @Operation(summary = "Calculate distance", description = "Calculate the distance between two points in meters")
    public ResponseEntity<Double> calculateDistance(
            @RequestParam @NotNull @Min(-90) @Max(90) Double startLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double startLng,
            @RequestParam @NotNull @Min(-90) @Max(90) Double endLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double endLng) {
        
        double distance = geoLocationService.calculateDistance(
                startLat, startLng, endLat, endLng);
        
        return ResponseEntity.ok(distance);
    }
    
    /**
     * Estimate travel time between two points.
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @param mode The travel mode (e.g., "driving", "walking", "bicycling", "transit")
     * @return The estimated travel time in seconds
     */
    @GetMapping("/travel-time")
    @Operation(summary = "Estimate travel time", 
               description = "Estimate the travel time between two points in seconds using specified travel mode")
    public ResponseEntity<Long> estimateTravelTime(
            @RequestParam @NotNull @Min(-90) @Max(90) Double startLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double startLng,
            @RequestParam @NotNull @Min(-90) @Max(90) Double endLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double endLng,
            @RequestParam @NotBlank String mode) {
        
        try {
            long travelTime = geoLocationService.estimateTravelTime(
                    startLat, startLng, endLat, endLng, mode);
            
            return ResponseEntity.ok(travelTime);
        } catch (GeoLocationException e) {
            log.error("Error estimating travel time: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check if a point is within a radius of a center point.
     * @param centerLat Center latitude
     * @param centerLng Center longitude
     * @param pointLat Point latitude
     * @param pointLng Point longitude
     * @param radiusMeters Radius in meters
     * @return true if point is within radius, false otherwise
     */
    @GetMapping("/is-within-radius")
    @Operation(summary = "Check if within radius", 
               description = "Check if a point is within a specified radius of a center point")
    public ResponseEntity<Boolean> isWithinRadius(
            @RequestParam @NotNull @Min(-90) @Max(90) Double centerLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double centerLng,
            @RequestParam @NotNull @Min(-90) @Max(90) Double pointLat,
            @RequestParam @NotNull @Min(-180) @Max(180) Double pointLng,
            @RequestParam @NotNull @Min(0) Double radiusMeters) {
        
        boolean isWithin = geoLocationService.isWithinRadius(
                centerLat, centerLng, pointLat, pointLng, radiusMeters);
        
        return ResponseEntity.ok(isWithin);
    }
    
    /**
     * Search for locations based on a query.
     * @param request The search request
     * @return List of matching locations
     */
    @PostMapping("/search")
    @Operation(summary = "Search locations", 
               description = "Search for locations based on a query, optionally biased toward a specific location")
    public ResponseEntity<List<GeocodeResponse>> searchLocations(@Valid @RequestBody SearchRequest request) {
        try {
            List<Location> locations = geoLocationService.searchLocations(
                    request.getQuery(),
                    request.getBiasLat(),
                    request.getBiasLng(),
                    request.getRadiusMeters());
            
            List<GeocodeResponse> responses = locations.stream()
                    .map(loc -> new GeocodeResponse(
                            loc.getLatitude(),
                            loc.getLongitude(),
                            null)) // We don't have the formatted address here
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (GeoLocationException e) {
            log.error("Error searching locations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
