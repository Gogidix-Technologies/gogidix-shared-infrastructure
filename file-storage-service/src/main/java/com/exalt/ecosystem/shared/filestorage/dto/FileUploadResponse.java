package com.exalt.ecosystem.shared.filestorage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for file upload responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadResponse {
    
    private String fileId;
    private String originalFilename;
    private String downloadUrl;
    private String contentType;
    private Long fileSize;
    private String checksum;
    private LocalDateTime uploadTimestamp;
    private boolean isPublic;
    private String message;
    
    // Convenience constructor for successful uploads
    public FileUploadResponse(String fileId, String originalFilename, String downloadUrl, 
                            String contentType, Long fileSize, String message) {
        this.fileId = fileId;
        this.originalFilename = originalFilename;
        this.downloadUrl = downloadUrl;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.message = message;
        this.uploadTimestamp = LocalDateTime.now();
        this.isPublic = false;
    }
}
