package com.gogidix.ecosystem.shared.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Configuration Server Application for Micro-Social-Ecommerce-Ecosystems.
 * 
 * This server provides centralized configuration management for all microservices
 * in the ecosystem. It integrates with the service registry for discovery and
 * provides secure configuration distribution.
 * 
 * Key Features:
 * - Centralized configuration management
 * - Git-based configuration repository
 * - Eureka service discovery integration
 * - Secure configuration access
 * - Environment-specific configurations
 * 
 * @author Ecosystem Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
