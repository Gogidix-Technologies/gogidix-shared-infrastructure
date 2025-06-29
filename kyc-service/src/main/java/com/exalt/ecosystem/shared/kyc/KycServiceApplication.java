package com.exalt.ecosystem.shared.kyc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Spring Boot application class for the KYC Service.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class KycServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(KycServiceApplication.class, args);
    }
}