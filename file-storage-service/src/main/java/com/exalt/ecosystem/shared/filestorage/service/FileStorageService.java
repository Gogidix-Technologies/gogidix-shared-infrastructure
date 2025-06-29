package com.exalt.ecosystem.shared.filestorage.service;

import com.exalt.ecosystem.shared.filestorage.config.FileStorageProperties;
import com.exalt.ecosystem.shared.filestorage.dto.FileInfoResponse;
import com.exalt.ecosystem.shared.filestorage.dto.FileUploadRequest;
import com.exalt.ecosystem.shared.filestorage.dto.FileUploadResponse;
import com.exalt.ecosystem.shared.filestorage.exception.FileNotFoundException;
import com.exalt.ecosystem.shared.filestorage.exception.FileStorageException;
import com.exalt.ecosystem.shared.filestorage.exception.InvalidFileException;
import com.exalt.ecosystem.shared.filestorage.model.FileMetadata;
import com.exalt.ecosystem.shared.filestorage.model.FileStatus;
import com.exalt.ecosystem.shared.filestorage.model.StorageType;
import com.exalt.ecosystem.shared.filestorage.repository.FileMetadataRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main service for file storage operations
 */
@Service
@Slf4j
@Transactional
public class FileStorageService {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private FileStorageProperties storageProperties;

    @Autowired
    private LocalStorageService localStorageService;

    @Autowired
    private ImageProcessingService imageProcessingService;

    private final Tika tika = new Tika();

    /**
     * Upload a file with comprehensive validation and processing
     */
    public FileUploadResponse uploadFile(MultipartFile file, FileUploadRequest request) {
        log.info("Processing file upload: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());

        try {
            // Validate the file
            validateFile(file);

            // Generate unique file ID
            String fileId = UUID.randomUUID().toString();

            // Detect content type
            String contentType = detectContentType(file);

            // Generate safe filename
            String safeFilename = generateSafeFilename(file.getOriginalFilename(), fileId);

            // Calculate checksum if required
            String checksum = null;
            if (request.isValidateChecksum()) {
                checksum = calculateChecksum(file.getBytes());
            }

            // Check for duplicates if checksum is available
            if (checksum != null) {
                List<FileMetadata> duplicates = fileMetadataRepository.findByChecksum(checksum);
                if (!duplicates.isEmpty() && request.isValidateChecksum()) {
                    log.warn("Duplicate file detected: {} (checksum: {})", file.getOriginalFilename(), checksum);
                    // Return existing file info instead of uploading duplicate
                    FileMetadata existing = duplicates.get(0);
                    return buildUploadResponse(existing, "File already exists (duplicate detected)");
                }
            }

            // Store the file
            String storedPath = storeFile(file, safeFilename, request);

            // Create metadata record
            FileMetadata metadata = createFileMetadata(fileId, file, safeFilename, storedPath, 
                                                     contentType, checksum, request);

            // Save metadata to database
            metadata = fileMetadataRepository.save(metadata);

            // Generate thumbnail for images
            if (isImageFile(contentType) && request.isGenerateThumbnail()) {
                try {
                    String thumbnailPath = imageProcessingService.generateThumbnail(storedPath, fileId);
                    metadata.setThumbnailPath(thumbnailPath);
                    fileMetadataRepository.save(metadata);
                } catch (Exception e) {
                    log.warn("Failed to generate thumbnail for file: {}", fileId, e);
                }
            }

            // Update status to uploaded
            metadata.setFileStatus(FileStatus.UPLOADED);
            fileMetadataRepository.save(metadata);

            log.info("File uploaded successfully: {} -> {}", file.getOriginalFilename(), fileId);
            return buildUploadResponse(metadata, "File uploaded successfully");

        } catch (Exception e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new FileStorageException("File upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Download a file by ID
     */
    public Resource downloadFile(String fileId) {
        log.info("Processing file download: {}", fileId);

        FileMetadata metadata = getFileMetadata(fileId);

        // Update access statistics
        updateAccessStatistics(fileId);

        try {
            Path filePath = getFilePath(metadata);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.info("File downloaded successfully: {}", fileId);
                return resource;
            } else {
                throw new FileNotFoundException("File not found or not readable: " + fileId);
            }

        } catch (Exception e) {
            log.error("Failed to download file: {}", fileId, e);
            throw new FileStorageException("File download failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get file information by ID
     */
    public FileInfoResponse getFileInfo(String fileId) {
        log.info("Getting file info: {}", fileId);

        FileMetadata metadata = getFileMetadata(fileId);
        return buildFileInfoResponse(metadata);
    }

    /**
     * Get files by user with pagination
     */
    public Page<FileInfoResponse> getFilesByUser(String userId, Pageable pageable) {
        log.info("Getting files for user: {} (page: {}, size: {})", userId, pageable.getPageNumber(), pageable.getPageSize());

        Page<FileMetadata> metadataPage = fileMetadataRepository.findByUploadedBy(userId, pageable);
        return metadataPage.map(this::buildFileInfoResponse);
    }

    /**
     * Search files by filename
     */
    public Page<FileInfoResponse> searchFiles(String filename, Pageable pageable) {
        log.info("Searching files with filename containing: {}", filename);

        Page<FileMetadata> metadataPage = fileMetadataRepository
            .findByOriginalFilenameContainingIgnoreCase(filename, pageable);
        return metadataPage.map(this::buildFileInfoResponse);
    }

    /**
     * Delete a file
     */
    public void deleteFile(String fileId, String userId) {
        log.info("Deleting file: {} by user: {}", fileId, userId);

        FileMetadata metadata = getFileMetadata(fileId);

        // Check if user has permission to delete
        if (!metadata.getUploadedBy().equals(userId) && !metadata.isPublic()) {
            throw new FileStorageException("Insufficient permissions to delete file: " + fileId);
        }

        try {
            // Mark as deleted in database
            metadata.setFileStatus(FileStatus.DELETED);
            fileMetadataRepository.save(metadata);

            // Optionally delete physical file immediately or schedule for cleanup
            // For now, we'll keep the file for potential recovery
            log.info("File marked as deleted: {}", fileId);

        } catch (Exception e) {
            log.error("Failed to delete file: {}", fileId, e);
            throw new FileStorageException("File deletion failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get public files
     */
    public Page<FileInfoResponse> getPublicFiles(Pageable pageable) {
        Page<FileMetadata> metadataPage = fileMetadataRepository.findByIsPublicTrue(pageable);
        return metadataPage.map(this::buildFileInfoResponse);
    }

    /**
     * Update file access statistics
     */
    private void updateAccessStatistics(String fileId) {
        try {
            fileMetadataRepository.updateAccessInfo(fileId, LocalDateTime.now());
            fileMetadataRepository.incrementDownloadCount(fileId);
        } catch (Exception e) {
            log.warn("Failed to update access statistics for file: {}", fileId, e);
        }
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("File is empty");
        }

        if (file.getSize() > storageProperties.getValidation().getMaxFileSize()) {
            throw new InvalidFileException("File size exceeds maximum allowed size");
        }

        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new InvalidFileException("Filename is required");
        }

        String extension = FilenameUtils.getExtension(filename).toLowerCase();

        // Check blocked extensions
        if (Arrays.asList(storageProperties.getValidation().getBlockedExtensions()).contains(extension)) {
            throw new InvalidFileException("File type not allowed: " + extension);
        }

        // Check allowed extensions if specified
        String[] allowedExtensions = storageProperties.getValidation().getAllowedExtensions();
        if (allowedExtensions.length > 0 && !Arrays.asList(allowedExtensions).contains(extension)) {
            throw new InvalidFileException("File type not supported: " + extension);
        }
    }

    /**
     * Detect file content type
     */
    private String detectContentType(MultipartFile file) {
        try {
            // First try the multipart file's content type
            String contentType = file.getContentType();
            if (StringUtils.hasText(contentType) && !contentType.equals("application/octet-stream")) {
                return contentType;
            }

            // Fall back to Tika detection
            return tika.detect(file.getBytes());

        } catch (Exception e) {
            log.warn("Failed to detect content type for file: {}", file.getOriginalFilename(), e);
            return "application/octet-stream";
        }
    }

    /**
     * Generate safe filename
     */
    private String generateSafeFilename(String originalFilename, String fileId) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String baseName = FilenameUtils.getBaseName(originalFilename);
        
        // Sanitize filename
        baseName = baseName.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        return fileId + "_" + baseName + "." + extension;
    }

    /**
     * Store file using appropriate storage backend
     */
    private String storeFile(MultipartFile file, String filename, FileUploadRequest request) {
        // For now, use local storage. Can be extended to support other backends
        return localStorageService.storeFile(file, filename);
    }

    /**
     * Create file metadata entity
     */
    private FileMetadata createFileMetadata(String fileId, MultipartFile file, String storedFilename, 
                                          String storedPath, String contentType, String checksum, 
                                          FileUploadRequest request) {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileId(fileId);
        metadata.setOriginalFilename(file.getOriginalFilename());
        metadata.setStoredFilename(storedFilename);
        metadata.setFilePath(storedPath);
        metadata.setContentType(contentType);
        metadata.setFileSize(file.getSize());
        metadata.setChecksum(checksum);
        metadata.setStorageType(StorageType.LOCAL);
        metadata.setFileStatus(FileStatus.UPLOADING);
        metadata.setUploadedBy(request.getUploadedBy());
        metadata.setUploadTimestamp(LocalDateTime.now());
        metadata.setPublic(request.isPublic());
        metadata.setExpiryDate(request.getExpiryDate());
        
        return metadata;
    }

    /**
     * Calculate file checksum (MD5)
     */
    private String calculateChecksum(byte[] fileBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to calculate checksum", e);
            return null;
        }
    }

    /**
     * Check if file is an image
     */
    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Get file metadata by ID
     */
    private FileMetadata getFileMetadata(String fileId) {
        return fileMetadataRepository.findByFileId(fileId)
            .orElseThrow(() -> new FileNotFoundException(fileId));
    }

    /**
     * Get file path from metadata
     */
    private Path getFilePath(FileMetadata metadata) {
        return Paths.get(metadata.getFilePath());
    }

    /**
     * Build upload response from metadata
     */
    private FileUploadResponse buildUploadResponse(FileMetadata metadata, String message) {
        return FileUploadResponse.builder()
            .fileId(metadata.getFileId())
            .originalFilename(metadata.getOriginalFilename())
            .downloadUrl("/api/files/" + metadata.getFileId() + "/download")
            .contentType(metadata.getContentType())
            .fileSize(metadata.getFileSize())
            .checksum(metadata.getChecksum())
            .uploadTimestamp(metadata.getUploadTimestamp())
            .isPublic(metadata.isPublic())
            .message(message)
            .build();
    }

    /**
     * Build file info response from metadata
     */
    private FileInfoResponse buildFileInfoResponse(FileMetadata metadata) {
        return FileInfoResponse.builder()
            .fileId(metadata.getFileId())
            .originalFilename(metadata.getOriginalFilename())
            .contentType(metadata.getContentType())
            .fileSize(metadata.getFileSize())
            .checksum(metadata.getChecksum())
            .storageType(metadata.getStorageType())
            .fileStatus(metadata.getFileStatus())
            .uploadedBy(metadata.getUploadedBy())
            .uploadTimestamp(metadata.getUploadTimestamp())
            .lastAccessed(metadata.getLastAccessed())
            .accessCount(metadata.getAccessCount())
            .downloadCount(metadata.getDownloadCount())
            .expiryDate(metadata.getExpiryDate())
            .isPublic(metadata.isPublic())
            .downloadUrl("/api/files/" + metadata.getFileId() + "/download")
            .thumbnailUrl(metadata.getThumbnailPath() != null ? 
                         "/api/files/" + metadata.getFileId() + "/thumbnail" : null)
            .metadataJson(metadata.getMetadataJson())
            .build();
    }
}
