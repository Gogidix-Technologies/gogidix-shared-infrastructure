package com.exalt.ecosystem.shared.filestorage.service;

import com.exalt.ecosystem.shared.filestorage.config.FileStorageProperties;
import com.exalt.ecosystem.shared.filestorage.exception.FileStorageException;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for image processing operations like thumbnail generation
 */
@Service
@Slf4j
public class ImageProcessingService {

    @Autowired
    private FileStorageProperties storageProperties;

    /**
     * Generate thumbnail for an image file
     */
    public String generateThumbnail(String originalFilePath, String fileId) {
        if (!storageProperties.getImage().isGenerateThumbnails()) {
            return null;
        }

        try {
            log.info("Generating thumbnail for file: {}", fileId);

            // Read the original image
            BufferedImage originalImage = ImageIO.read(new File(originalFilePath));
            if (originalImage == null) {
                log.warn("Could not read image file: {}", originalFilePath);
                return null;
            }

            // Get thumbnail dimensions
            int thumbnailWidth = storageProperties.getImage().getThumbnailWidth();
            int thumbnailHeight = storageProperties.getImage().getThumbnailHeight();

            // Generate thumbnail
            BufferedImage thumbnail = Scalr.resize(
                originalImage, 
                Scalr.Method.BALANCED,
                Scalr.Mode.FIT_TO_WIDTH,
                thumbnailWidth, 
                thumbnailHeight
            );

            // Create thumbnail filename and path
            String thumbnailFilename = "thumb_" + fileId + "." + storageProperties.getImage().getThumbnailFormat();
            Path originalPath = Paths.get(originalFilePath);
            Path thumbnailPath = originalPath.getParent().resolve(thumbnailFilename);

            // Save thumbnail
            ImageIO.write(thumbnail, storageProperties.getImage().getThumbnailFormat(), thumbnailPath.toFile());

            String thumbnailPathStr = thumbnailPath.toString();
            log.info("Thumbnail generated successfully: {} -> {}", fileId, thumbnailPathStr);
            
            return thumbnailPathStr;

        } catch (IOException e) {
            log.error("Failed to generate thumbnail for file: {}", fileId, e);
            throw new FileStorageException("Thumbnail generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Resize image to specific dimensions
     */
    public String resizeImage(String originalFilePath, String fileId, int width, int height) {
        try {
            log.info("Resizing image: {} to {}x{}", fileId, width, height);

            BufferedImage originalImage = ImageIO.read(new File(originalFilePath));
            if (originalImage == null) {
                throw new FileStorageException("Could not read image file: " + originalFilePath);
            }

            BufferedImage resizedImage = Scalr.resize(
                originalImage, 
                Scalr.Method.BALANCED,
                width, 
                height
            );

            // Create resized filename and path
            String resizedFilename = "resized_" + width + "x" + height + "_" + fileId + ".jpg";
            Path originalPath = Paths.get(originalFilePath);
            Path resizedPath = originalPath.getParent().resolve(resizedFilename);

            // Save resized image
            ImageIO.write(resizedImage, "jpg", resizedPath.toFile());

            String resizedPathStr = resizedPath.toString();
            log.info("Image resized successfully: {} -> {}", fileId, resizedPathStr);
            
            return resizedPathStr;

        } catch (IOException e) {
            log.error("Failed to resize image: {}", fileId, e);
            throw new FileStorageException("Image resize failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get image dimensions
     */
    public int[] getImageDimensions(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            if (image == null) {
                return null;
            }
            
            return new int[]{image.getWidth(), image.getHeight()};
            
        } catch (IOException e) {
            log.error("Failed to get image dimensions: {}", filePath, e);
            return null;
        }
    }

    /**
     * Check if file is a supported image format
     */
    public boolean isSupportedImageFormat(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.startsWith("image/") && 
               (contentType.contains("jpeg") || 
                contentType.contains("jpg") || 
                contentType.contains("png") || 
                contentType.contains("gif") || 
                contentType.contains("bmp") || 
                contentType.contains("webp"));
    }

    /**
     * Convert image to different format
     */
    public String convertImageFormat(String originalFilePath, String fileId, String targetFormat) {
        try {
            log.info("Converting image: {} to format: {}", fileId, targetFormat);

            BufferedImage originalImage = ImageIO.read(new File(originalFilePath));
            if (originalImage == null) {
                throw new FileStorageException("Could not read image file: " + originalFilePath);
            }

            // Create converted filename and path
            String convertedFilename = "converted_" + fileId + "." + targetFormat;
            Path originalPath = Paths.get(originalFilePath);
            Path convertedPath = originalPath.getParent().resolve(convertedFilename);

            // Handle transparency for formats that don't support it
            if ("jpg".equalsIgnoreCase(targetFormat) || "jpeg".equalsIgnoreCase(targetFormat)) {
                BufferedImage jpgImage = new BufferedImage(
                    originalImage.getWidth(), 
                    originalImage.getHeight(), 
                    BufferedImage.TYPE_INT_RGB
                );
                jpgImage.createGraphics().drawImage(originalImage, 0, 0, java.awt.Color.WHITE, null);
                originalImage = jpgImage;
            }

            // Save converted image
            ImageIO.write(originalImage, targetFormat, convertedPath.toFile());

            String convertedPathStr = convertedPath.toString();
            log.info("Image converted successfully: {} -> {} ({})", fileId, convertedPathStr, targetFormat);
            
            return convertedPathStr;

        } catch (IOException e) {
            log.error("Failed to convert image format: {}", fileId, e);
            throw new FileStorageException("Image format conversion failed: " + e.getMessage(), e);
        }
    }
}
