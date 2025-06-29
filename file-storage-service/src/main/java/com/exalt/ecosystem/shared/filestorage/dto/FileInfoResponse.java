package com.exalt.ecosystem.shared.filestorage.dto;

import com.exalt.ecosystem.shared.filestorage.model.FileStatus;
import com.exalt.ecosystem.shared.filestorage.model.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for file information responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfoResponse {
    
    private String fileId;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String checksum;
    private StorageType storageType;
    private FileStatus fileStatus;
    private String uploadedBy;
    private LocalDateTime uploadTimestamp;
    private LocalDateTime lastAccessed;
    private Long accessCount;
    private Long downloadCount;
    private LocalDateTime expiryDate;
    private boolean isPublic;
    private String downloadUrl;
    private String thumbnailUrl;
    private String metadataJson;
    
    /**
     * Helper method to get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        long bytes = fileSize;
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    /**
     * Check if file is expired
     */
    public boolean isExpired() {
        return expiryDate != null && LocalDateTime.now().isAfter(expiryDate);
    }
}
