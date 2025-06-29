package com.exalt.ecosystem.shared.caching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Spring Boot application class for the Caching Service.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CachingServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CachingServiceApplication.class, args);
    }
}
