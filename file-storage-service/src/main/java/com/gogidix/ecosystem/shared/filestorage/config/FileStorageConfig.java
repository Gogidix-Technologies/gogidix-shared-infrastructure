package com.gogidix.ecosystem.shared.filestorage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Configuration for file storage backends
 */
@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageConfig {
    
    @Autowired
    private FileStorageProperties properties;
    
    /**
     * Initialize local storage directories
     */
    @Bean
    public String initializeLocalStorage() {
        if (properties.getLocal().isCreateDirectories()) {
            createDirectoryIfNotExists(properties.getLocal().getUploadDir());
            createDirectoryIfNotExists(properties.getLocal().getTempDir());
        }
        return "LocalStorageInitialized";
    }
    
    /**
     * Create directory if it doesn't exist
     */
    private void createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created directory: " + path);
            } else {
                System.err.println("Failed to create directory: " + path);
            }
        }
    }
}
