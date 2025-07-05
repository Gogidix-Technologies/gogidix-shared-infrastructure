package com.gogidix.ecosystem.shared.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for the File Storage Service
 * Handles file uploads, downloads, storage management and metadata
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FileStorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStorageServiceApplication.class, args);
    }
}
