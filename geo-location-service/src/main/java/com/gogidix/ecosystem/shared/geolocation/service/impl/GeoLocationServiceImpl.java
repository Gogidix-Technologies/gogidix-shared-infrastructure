package com.gogidix.ecosystem.shared.geolocation.service.impl;

import com.gogidix.ecosystem.shared.geolocation.domain.entity.GeoRequest;
import com.gogidix.ecosystem.shared.geolocation.domain.entity.Location;
import com.gogidix.ecosystem.shared.geolocation.domain.entity.SimpleLocation;
import com.gogidix.ecosystem.shared.geolocation.domain.repository.GeoRequestRepository;
import com.gogidix.ecosystem.shared.geolocation.exception.GeoLocationException;
import com.gogidix.ecosystem.shared.geolocation.provider.GeoLocationProvider;
import com.gogidix.ecosystem.shared.geolocation.service.GeoLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of the GeoLocationService.
 */
@Service
@Slf4j
public class GeoLocationServiceImpl implements GeoLocationService {
    private final Map<String, GeoLocationProvider> providers;
    private final GeoRequestRepository geoRequestRepository;
    private final String defaultProviderType;
    private GeoLocationProvider activeProvider;
    
    @Autowired
    public GeoLocationServiceImpl(
            List<GeoLocationProvider> providerList,
            GeoRequestRepository geoRequestRepository,
            @Value("${geo-location.provider.type:local}") String defaultProviderType) {
        
        this.providers = providerList.stream()
                .collect(Collectors.toMap(GeoLocationProvider::getProviderName, Function.identity()));
        
        this.geoRequestRepository = geoRequestRepository;
        this.defaultProviderType = defaultProviderType;
        
        // Set the active provider
        setActiveProvider(defaultProviderType);
        
        log.info("GeoLocationService initialized with providers: {} (active: {})",
                providers.keySet(), activeProvider.getProviderName());
    }
    
    /**
     * Set the active provider by name.
     * @param providerName The provider name
     * @throws IllegalArgumentException if the provider does not exist
     */
    public void setActiveProvider(String providerName) {
        if (!providers.containsKey(providerName)) {
            throw new IllegalArgumentException("Unknown provider: " + providerName);
        }
        
        this.activeProvider = providers.get(providerName);
        log.info("Active geo-location provider set to: {}", providerName);
    }
    
    @Override
    public Optional<Location> geocodeAddress(String address) throws GeoLocationException {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check cache first (if address was geocoded in the last 24 hours)
            Optional<GeoRequest> cachedRequest = geoRequestRepository.findFirstByQueryAndRequestTypeAndRequestTimeGreaterThanAndResultIsNotNullOrderByRequestTimeDesc(
                    address, "geocode", LocalDateTime.now().minusHours(24));
            
            if (cachedRequest.isPresent() && cachedRequest.get().getLatitude() != null && cachedRequest.get().getLongitude() != null) {
                log.debug("Cache hit for geocoding address: {}", address);
                return Optional.of(new SimpleLocation(
                        cachedRequest.get().getLatitude(),
                        cachedRequest.get().getLongitude()));
            }
            
            log.debug("Geocoding address with provider {}: {}", activeProvider.getProviderName(), address);
            Optional<Location> location = activeProvider.geocodeAddress(address);
            
            // Save request for caching and auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("geocode")
                    .query(address)
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .build();
            
            if (location.isPresent()) {
                request.setLatitude(location.get().getLatitude());
                request.setLongitude(location.get().getLongitude());
                request.setResult("SUCCESS");
            } else {
                request.setResult("NOT_FOUND");
            }
            
            request.setResponseTimeMs((int) (System.currentTimeMillis() - startTime));
            geoRequestRepository.save(request);
            
            return location;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Save failed request for auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("geocode")
                    .query(address)
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .result("ERROR: " + e.getMessage())
                    .responseTimeMs((int) duration)
                    .build();
            
            geoRequestRepository.save(request);
            
            if (e instanceof GeoLocationException) {
                throw (GeoLocationException) e;
            }
            throw new GeoLocationException("Error geocoding address", e);
        }
    }
    
    @Override
    public Optional<String> reverseGeocode(double latitude, double longitude) throws GeoLocationException {
        long startTime = System.currentTimeMillis();
        
        try {
            // Check cache first (if coordinates were reverse geocoded in the last 24 hours)
            List<GeoRequest> cachedRequests = geoRequestRepository.findByLatitudeAndLongitudeAndRequestTypeAndRequestTimeGreaterThanOrderByRequestTimeDesc(
                    latitude, longitude, "reverse_geocode", LocalDateTime.now().minusHours(24));
            
            if (!cachedRequests.isEmpty() && cachedRequests.get(0).getResult() != null) {
                log.debug("Cache hit for reverse geocoding: {}, {}", latitude, longitude);
                return Optional.of(cachedRequests.get(0).getResult());
            }
            
            log.debug("Reverse geocoding with provider {}: {}, {}", 
                    activeProvider.getProviderName(), latitude, longitude);
            
            SimpleLocation location = new SimpleLocation(latitude, longitude);
            Optional<String> address = activeProvider.reverseGeocode(location);
            
            // Save request for caching and auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("reverse_geocode")
                    .latitude(latitude)
                    .longitude(longitude)
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .build();
            
            if (address.isPresent()) {
                request.setResult(address.get());
            } else {
                request.setResult("NOT_FOUND");
            }
            
            request.setResponseTimeMs((int) (System.currentTimeMillis() - startTime));
            geoRequestRepository.save(request);
            
            return address;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Save failed request for auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("reverse_geocode")
                    .latitude(latitude)
                    .longitude(longitude)
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .result("ERROR: " + e.getMessage())
                    .responseTimeMs((int) duration)
                    .build();
            
            geoRequestRepository.save(request);
            
            if (e instanceof GeoLocationException) {
                throw (GeoLocationException) e;
            }
            throw new GeoLocationException("Error reverse geocoding", e);
        }
    }
    
    @Override
    public double calculateDistance(double startLat, double startLng, double endLat, double endLng) {
        SimpleLocation start = new SimpleLocation(startLat, startLng);
        SimpleLocation end = new SimpleLocation(endLat, endLng);
        
        return activeProvider.calculateDistance(start, end);
    }
    
    @Override
    public long estimateTravelTime(double startLat, double startLng, double endLat, double endLng, String mode) 
            throws GeoLocationException {
        long startTime = System.currentTimeMillis();
        
        try {
            SimpleLocation start = new SimpleLocation(startLat, startLng);
            SimpleLocation end = new SimpleLocation(endLat, endLng);
            
            log.debug("Estimating travel time with provider {}: from {},{} to {},{} via {}",
                    activeProvider.getProviderName(), startLat, startLng, endLat, endLng, mode);
            
            long travelTimeSeconds = activeProvider.estimateTravelTime(start, end, mode);
            
            // Save request for auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("travel_time")
                    .query(String.format("from:%f,%f to:%f,%f mode:%s", 
                            startLat, startLng, endLat, endLng, mode))
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .result(String.valueOf(travelTimeSeconds))
                    .responseTimeMs((int) (System.currentTimeMillis() - startTime))
                    .build();
            
            geoRequestRepository.save(request);
            
            return travelTimeSeconds;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Save failed request for auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("travel_time")
                    .query(String.format("from:%f,%f to:%f,%f mode:%s", 
                            startLat, startLng, endLat, endLng, mode))
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .result("ERROR: " + e.getMessage())
                    .responseTimeMs((int) duration)
                    .build();
            
            geoRequestRepository.save(request);
            
            if (e instanceof GeoLocationException) {
                throw (GeoLocationException) e;
            }
            throw new GeoLocationException("Error estimating travel time", e);
        }
    }
    
    @Override
    public boolean isWithinRadius(double centerLat, double centerLng, double pointLat, double pointLng, double radiusInMeters) {
        SimpleLocation center = new SimpleLocation(centerLat, centerLng);
        SimpleLocation point = new SimpleLocation(pointLat, pointLng);
        
        return activeProvider.isWithinRadius(center, point, radiusInMeters);
    }
    
    @Override
    public List<Location> searchLocations(String query, Double biasLat, Double biasLng, double radiusInMeters) 
            throws GeoLocationException {
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<Location> biasLocation = Optional.empty();
            if (biasLat != null && biasLng != null) {
                biasLocation = Optional.of(new SimpleLocation(biasLat, biasLng));
            }
            
            log.debug("Searching locations with provider {}: {} (bias: {})",
                    activeProvider.getProviderName(), query, 
                    biasLocation.isPresent() ? String.format("%f,%f", biasLat, biasLng) : "none");
            
            List<Location> locations = activeProvider.searchLocations(query, biasLocation, radiusInMeters);
            
            // Save request for auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("search")
                    .query(query)
                    .latitude(biasLat)
                    .longitude(biasLng)
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .result(String.format("Found %d results", locations.size()))
                    .responseTimeMs((int) (System.currentTimeMillis() - startTime))
                    .build();
            
            geoRequestRepository.save(request);
            
            return locations;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Save failed request for auditing
            GeoRequest request = GeoRequest.builder()
                    .requestType("search")
                    .query(query)
                    .latitude(biasLat)
                    .longitude(biasLng)
                    .provider(activeProvider.getProviderName())
                    .requestTime(LocalDateTime.now())
                    .result("ERROR: " + e.getMessage())
                    .responseTimeMs((int) duration)
                    .build();
            
            geoRequestRepository.save(request);
            
            if (e instanceof GeoLocationException) {
                throw (GeoLocationException) e;
            }
            throw new GeoLocationException("Error searching locations", e);
        }
    }
    
    @Override
    public String getCurrentProviderName() {
        return activeProvider.getProviderName();
    }
}
