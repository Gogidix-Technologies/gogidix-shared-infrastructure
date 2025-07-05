package com.gogidix.ecosystem.shared.filestorage.controller;

import com.gogidix.ecosystem.shared.filestorage.dto.FileInfoResponse;
import com.gogidix.ecosystem.shared.filestorage.dto.FileUploadRequest;
import com.gogidix.ecosystem.shared.filestorage.dto.FileUploadResponse;
import com.gogidix.ecosystem.shared.filestorage.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for file storage operations
 */
@RestController
@RequestMapping("/api/files")
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class FileStorageController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload a single file
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            @RequestParam(value = "generateThumbnail", defaultValue = "false") boolean generateThumbnail,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", required = false) String category) {
        
        log.info("Received file upload request: {} (size: {} bytes) by user: {}", 
                 file.getOriginalFilename(), file.getSize(), uploadedBy);

        try {
            FileUploadRequest request = FileUploadRequest.builder()
                .file(file)
                .uploadedBy(uploadedBy)
                .isPublic(isPublic)
                .generateThumbnail(generateThumbnail)
                .description(description)
                .category(category)
                .validateChecksum(true)
                .build();

            FileUploadResponse response = fileStorageService.uploadFile(file, request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("File upload failed", e);
            
            FileUploadResponse errorResponse = FileUploadResponse.builder()
                .message("Upload failed: " + e.getMessage())
                .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Upload multiple files
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy,
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic) {
        
        log.info("Received multiple file upload request: {} files by user: {}", files.length, uploadedBy);

        try {
            List<FileUploadResponse> responses = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                try {
                    FileUploadRequest request = FileUploadRequest.builder()
                        .file(file)
                        .uploadedBy(uploadedBy)
                        .isPublic(isPublic)
                        .validateChecksum(true)
                        .build();

                    FileUploadResponse response = fileStorageService.uploadFile(file, request);
                    responses.add(response);
                    
                } catch (Exception e) {
                    log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                    
                    FileUploadResponse errorResponse = FileUploadResponse.builder()
                        .originalFilename(file.getOriginalFilename())
                        .message("Upload failed: " + e.getMessage())
                        .build();
                    responses.add(errorResponse);
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
            
        } catch (Exception e) {
            log.error("Multiple file upload failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download a file by ID
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        log.info("Processing download request for file: {}", fileId);

        try {
            // Get file info first to set proper headers
            FileInfoResponse fileInfo = fileStorageService.getFileInfo(fileId);
            Resource resource = fileStorageService.downloadFile(fileId);

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                .body(resource);
                
        } catch (Exception e) {
            log.error("File download failed for ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get file information by ID
     */
    @GetMapping("/{fileId}/info")
    public ResponseEntity<FileInfoResponse> getFileInfo(@PathVariable String fileId) {
        log.info("Getting file info for ID: {}", fileId);

        try {
            FileInfoResponse fileInfo = fileStorageService.getFileInfo(fileId);
            return ResponseEntity.ok(fileInfo);
            
        } catch (Exception e) {
            log.error("Failed to get file info for ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get files by user with pagination
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<FileInfoResponse>> getFilesByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting files for user: {} (page: {}, size: {})", userId, page, size);

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<FileInfoResponse> files = fileStorageService.getFilesByUser(userId, pageable);
            return ResponseEntity.ok(files);
            
        } catch (Exception e) {
            log.error("Failed to get files for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search files by filename
     */
    @GetMapping("/search")
    public ResponseEntity<Page<FileInfoResponse>> searchFiles(
            @RequestParam String filename,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching files with filename containing: {}", filename);

        try {
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "uploadTimestamp"));
            
            Page<FileInfoResponse> files = fileStorageService.searchFiles(filename, pageable);
            return ResponseEntity.ok(files);
            
        } catch (Exception e) {
            log.error("File search failed for query: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get public files
     */
    @GetMapping("/public")
    public ResponseEntity<Page<FileInfoResponse>> getPublicFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting public files (page: {}, size: {})", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, 
                Sort.by(Sort.Direction.DESC, "uploadTimestamp"));
            
            Page<FileInfoResponse> files = fileStorageService.getPublicFiles(pageable);
            return ResponseEntity.ok(files);
            
        } catch (Exception e) {
            log.error("Failed to get public files", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(
            @PathVariable String fileId,
            @RequestParam String userId) {
        
        log.info("Processing delete request for file: {} by user: {}", fileId, userId);

        try {
            fileStorageService.deleteFile(fileId, userId);
            return ResponseEntity.ok("File deleted successfully");
            
        } catch (Exception e) {
            log.error("File deletion failed for ID: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Delete failed: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("File storage service is running");
    }

    /**
     * Service info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<ServiceInfo> getServiceInfo() {
        ServiceInfo info = new ServiceInfo();
        info.setServiceName("file-storage-service");
        info.setVersion("1.0.0");
        info.setDescription("Multi-backend file storage service with metadata management");
        info.setSupportedStorageTypes(List.of("LOCAL", "AWS_S3", "AZURE_BLOB"));
        info.setSupportedFeatures(List.of("Upload", "Download", "Thumbnails", "Search", "Metadata"));
        
        return ResponseEntity.ok(info);
    }

    /**
     * Simple DTO for service information
     */
    public static class ServiceInfo {
        private String serviceName;
        private String version;
        private String description;
        private List<String> supportedStorageTypes;
        private List<String> supportedFeatures;

        // Getters and setters
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<String> getSupportedStorageTypes() { return supportedStorageTypes; }
        public void setSupportedStorageTypes(List<String> supportedStorageTypes) { this.supportedStorageTypes = supportedStorageTypes; }
        
        public List<String> getSupportedFeatures() { return supportedFeatures; }
        public void setSupportedFeatures(List<String> supportedFeatures) { this.supportedFeatures = supportedFeatures; }
    }
}
