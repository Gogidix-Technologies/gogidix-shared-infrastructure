package com.exalt.ecosystem.shared.documentverification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Spring Boot application class for the Document Verification Service.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class DocumentVerificationApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DocumentVerificationApplication.class, args);
    }
}