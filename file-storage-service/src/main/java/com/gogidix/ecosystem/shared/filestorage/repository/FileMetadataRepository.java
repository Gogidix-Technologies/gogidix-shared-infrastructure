package com.gogidix.ecosystem.shared.filestorage.repository;

import com.gogidix.ecosystem.shared.filestorage.model.FileMetadata;
import com.gogidix.ecosystem.shared.filestorage.model.FileStatus;
import com.gogidix.ecosystem.shared.filestorage.model.StorageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FileMetadata entity
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    
    /**
     * Find file metadata by file ID
     */
    Optional<FileMetadata> findByFileId(String fileId);
    
    /**
     * Find files by uploaded user
     */
    List<FileMetadata> findByUploadedBy(String uploadedBy);
    
    /**
     * Find files by uploaded user with pagination
     */
    Page<FileMetadata> findByUploadedBy(String uploadedBy, Pageable pageable);
    
    /**
     * Find files by status
     */
    List<FileMetadata> findByFileStatus(FileStatus fileStatus);
    
    /**
     * Find files by storage type
     */
    List<FileMetadata> findByStorageType(StorageType storageType);
    
    /**
     * Find public files
     */
    List<FileMetadata> findByIsPublicTrue();
    
    /**
     * Find public files with pagination
     */
    Page<FileMetadata> findByIsPublicTrue(Pageable pageable);
    
    /**
     * Find files by content type
     */
    List<FileMetadata> findByContentTypeContainingIgnoreCase(String contentType);
    
    /**
     * Find files uploaded within a date range
     */
    List<FileMetadata> findByUploadTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find expired files
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.expiryDate IS NOT NULL AND f.expiryDate < :currentTime")
    List<FileMetadata> findExpiredFiles(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find files by original filename containing (search)
     */
    Page<FileMetadata> findByOriginalFilenameContainingIgnoreCase(String filename, Pageable pageable);
    
    /**
     * Find files larger than specified size
     */
    List<FileMetadata> findByFileSizeGreaterThan(Long fileSize);
    
    /**
     * Find files by checksum (duplicate detection)
     */
    List<FileMetadata> findByChecksum(String checksum);
    
    /**
     * Update access count and last accessed time
     */
    @Modifying
    @Query("UPDATE FileMetadata f SET f.accessCount = f.accessCount + 1, f.lastAccessed = :accessTime WHERE f.fileId = :fileId")
    int updateAccessInfo(@Param("fileId") String fileId, @Param("accessTime") LocalDateTime accessTime);
    
    /**
     * Update download count
     */
    @Modifying
    @Query("UPDATE FileMetadata f SET f.downloadCount = f.downloadCount + 1 WHERE f.fileId = :fileId")
    int incrementDownloadCount(@Param("fileId") String fileId);
    
    /**
     * Update file status
     */
    @Modifying
    @Query("UPDATE FileMetadata f SET f.fileStatus = :status WHERE f.fileId = :fileId")
    int updateFileStatus(@Param("fileId") String fileId, @Param("status") FileStatus status);
    
    /**
     * Count files by user
     */
    long countByUploadedBy(String uploadedBy);
    
    /**
     * Count files by status
     */
    long countByFileStatus(FileStatus fileStatus);
    
    /**
     * Calculate total size by user
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileMetadata f WHERE f.uploadedBy = :uploadedBy")
    Long getTotalFileSizeByUser(@Param("uploadedBy") String uploadedBy);
    
    /**
     * Get files that need cleanup (deleted status and old)
     */
    @Query("SELECT f FROM FileMetadata f WHERE f.fileStatus = 'DELETED' AND f.uploadTimestamp < :cutoffDate")
    List<FileMetadata> findFilesForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Check if file ID exists
     */
    boolean existsByFileId(String fileId);
}
