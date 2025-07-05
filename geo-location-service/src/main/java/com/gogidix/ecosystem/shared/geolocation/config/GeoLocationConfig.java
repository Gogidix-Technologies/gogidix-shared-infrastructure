package com.gogidix.ecosystem.shared.geolocation.config;

import com.gogidix.ecosystem.shared.geolocation.provider.GeoLocationProvider;
import com.gogidix.ecosystem.shared.geolocation.service.impl.GeoLocationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Configuration for the Geo-Location Service.
 */
@Configuration
public class GeoLocationConfig {
    private final GeoLocationServiceImpl geoLocationService;
    private final String providerType;
    
    @Autowired
    public GeoLocationConfig(
            GeoLocationServiceImpl geoLocationService,
            @Value("${geo-location.provider.type:local}") String providerType) {
        this.geoLocationService = geoLocationService;
        this.providerType = providerType;
    }
    
    /**
     * Set up the geo-location service with the configured provider.
     */
    @PostConstruct
    public void setupGeoLocationService() {
        geoLocationService.setActiveProvider(providerType);
    }
}
