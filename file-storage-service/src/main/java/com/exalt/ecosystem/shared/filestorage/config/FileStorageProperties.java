package com.exalt.ecosystem.shared.filestorage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

/**
 * Configuration properties for file storage
 */
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageProperties {
    
    /**
     * Local storage configuration
     */
    private Local local = new Local();
    
    /**
     * AWS S3 configuration
     */
    private AwsS3 awsS3 = new AwsS3();
    
    /**
     * File validation configuration
     */
    private Validation validation = new Validation();
    
    /**
     * Image processing configuration
     */
    private Image image = new Image();
    
    @Data
    public static class Local {
        private String uploadDir = "./uploads";
        private String tempDir = "./temp";
        private boolean createDirectories = true;
    }
    
    @Data
    public static class AwsS3 {
        private String bucketName;
        private String region = "us-east-1";
        private String accessKey;
        private String secretKey;
        private String endpoint; // For S3-compatible services
        private boolean pathStyleAccess = false;
    }
    
    @Data
    public static class Validation {
        private long maxFileSize = 50 * 1024 * 1024; // 50MB
        private String[] allowedExtensions = {
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", 
            "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar"
        };
        private String[] blockedExtensions = {
            "exe", "bat", "cmd", "com", "scr", "vbs", "js"
        };
        private boolean virusScan = false;
        private boolean checksumValidation = true;
    }
    
    @Data
    public static class Image {
        private boolean generateThumbnails = true;
        private int thumbnailWidth = 200;
        private int thumbnailHeight = 200;
        private String thumbnailFormat = "jpg";
        private float thumbnailQuality = 0.8f;
    }
}
