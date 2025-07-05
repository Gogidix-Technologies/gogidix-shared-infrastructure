package com.gogidix.ecosystem.shared.filestorage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing file metadata in the database
 */
@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "file_id", unique = true, nullable = false)
    private String fileId; // UUID for file identification
    
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    
    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;
    
    @Column(name = "file_path")
    private String filePath;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "checksum")
    private String checksum; // MD5 or SHA-256 hash
    
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    private StorageType storageType = StorageType.LOCAL;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "file_status", nullable = false)
    private FileStatus fileStatus = FileStatus.UPLOADED;
    
    @Column(name = "uploaded_by")
    private String uploadedBy; // User ID who uploaded the file
    
    @Column(name = "upload_timestamp", nullable = false)
    private LocalDateTime uploadTimestamp;
    
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;
    
    @Column(name = "access_count")
    private Long accessCount = 0L;
    
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    @Column(name = "is_public")
    private boolean isPublic = false;
    
    @Column(name = "download_count")
    private Long downloadCount = 0L;
    
    @Column(name = "thumbnail_path")
    private String thumbnailPath; // For images
    
    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson; // Additional metadata as JSON
    
    @PrePersist
    protected void onCreate() {
        if (uploadTimestamp == null) {
            uploadTimestamp = LocalDateTime.now();
        }
        if (fileStatus == null) {
            fileStatus = FileStatus.UPLOADED;
        }
        if (storageType == null) {
            storageType = StorageType.LOCAL;
        }
        if (accessCount == null) {
            accessCount = 0L;
        }
        if (downloadCount == null) {
            downloadCount = 0L;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastAccessed = LocalDateTime.now();
    }
}
