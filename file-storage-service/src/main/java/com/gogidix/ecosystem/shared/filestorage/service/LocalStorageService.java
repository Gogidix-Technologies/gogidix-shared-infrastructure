package com.gogidix.ecosystem.shared.filestorage.service;

import com.gogidix.ecosystem.shared.filestorage.config.FileStorageProperties;
import com.gogidix.ecosystem.shared.filestorage.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for local file system storage operations
 */
@Service
@Slf4j
public class LocalStorageService {

    @Autowired
    private FileStorageProperties storageProperties;

    /**
     * Store file in local filesystem with organized directory structure
     */
    public String storeFile(MultipartFile file, String filename) {
        try {
            // Create date-based directory structure (yyyy/MM/dd)
            String dateBasedPath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadPath = Paths.get(storageProperties.getLocal().getUploadDir(), dateBasedPath);

            // Create directories if they don't exist
            Files.createDirectories(uploadPath);

            // Resolve the file path
            Path filePath = uploadPath.resolve(filename);

            // Ensure we don't overwrite existing files
            int counter = 1;
            Path originalPath = filePath;
            while (Files.exists(filePath)) {
                String name = filename.substring(0, filename.lastIndexOf('.'));
                String extension = filename.substring(filename.lastIndexOf('.'));
                String newFilename = name + "_" + counter + extension;
                filePath = uploadPath.resolve(newFilename);
                counter++;
            }

            // Copy file to the target location
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String storedPath = filePath.toString();
            log.info("File stored locally: {} -> {}", filename, storedPath);
            
            return storedPath;

        } catch (IOException e) {
            log.error("Failed to store file locally: {}", filename, e);
            throw new FileStorageException("Failed to store file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete file from local filesystem
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            
            if (deleted) {
                log.info("File deleted from local storage: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
            
            return deleted;

        } catch (IOException e) {
            log.error("Failed to delete file from local storage: {}", filePath, e);
            return false;
        }
    }

    /**
     * Check if file exists in local storage
     */
    public boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Get file size
     */
    public long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filePath, e);
            return 0;
        }
    }

    /**
     * Create temporary file
     */
    public Path createTempFile(String prefix, String suffix) {
        try {
            Path tempDir = Paths.get(storageProperties.getLocal().getTempDir());
            Files.createDirectories(tempDir);
            
            return Files.createTempFile(tempDir, prefix, suffix);
            
        } catch (IOException e) {
            log.error("Failed to create temporary file", e);
            throw new FileStorageException("Failed to create temporary file: " + e.getMessage(), e);
        }
    }

    /**
     * Clean up temporary files older than specified days
     */
    public void cleanupTempFiles(int daysOld) {
        try {
            Path tempDir = Paths.get(storageProperties.getLocal().getTempDir());
            
            if (!Files.exists(tempDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60L * 60L * 1000L);

            Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted old temp file: {}", path);
                    } catch (IOException e) {
                        log.warn("Failed to delete temp file: {}", path, e);
                    }
                });

        } catch (IOException e) {
            log.error("Failed to cleanup temp files", e);
        }
    }
}
