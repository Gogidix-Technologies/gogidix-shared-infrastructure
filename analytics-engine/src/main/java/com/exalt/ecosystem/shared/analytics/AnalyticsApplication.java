package com.exalt.ecosystem.shared.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.client.RestTemplate;

/**
 * Main application class for the Analytics Engine microservice.
 * This service provides data processing and analytics capabilities
 * for the microservices ecosystem.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaRepositories
@EnableScheduling
@EnableAsync
@EnableCaching
public class AnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApplication.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 