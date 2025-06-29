package com.exalt.ecosystem.shared.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for file upload requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadRequest {
    
    @NotNull(message = "File is required")
    private org.springframework.web.multipart.MultipartFile file;
    
    private String uploadedBy;
    private boolean isPublic = false;
    private LocalDateTime expiryDate;
    private Map<String, Object> metadata;
    private boolean generateThumbnail = false;
    private String description;
    private String category;
    
    // File validation settings
    @Builder.Default
    private boolean validateChecksum = true;
    
    @Builder.Default
    private boolean scanForVirus = false;
    
    // Storage preferences
    private String preferredStorageType; // LOCAL, AWS_S3, etc.
    private String folder; // Optional folder/directory structure
}
