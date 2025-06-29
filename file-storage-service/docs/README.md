# File Storage Service Documentation

## Overview

The File Storage Service is a comprehensive Java-based microservice that provides scalable, secure, and highly available file storage and management capabilities for the Social E-commerce Ecosystem. It supports multiple storage backends, implements advanced file processing, and ensures data integrity, security, and compliance across all file operations.

## Components

### Core Components
- **FileStorageManager**: Main service for file storage operations and lifecycle management
- **StorageProviderManager**: Multi-provider storage backend management and failover
- **FileProcessor**: File processing, transformation, and optimization services
- **MetadataService**: File metadata management and indexing
- **SecurityManager**: File access control, encryption, and security enforcement

### Storage Providers
- **LocalFileSystemProvider**: Local filesystem storage for development and caching
- **AmazonS3Provider**: Amazon S3 cloud storage integration
- **GoogleCloudStorageProvider**: Google Cloud Storage integration
- **AzureBlobStorageProvider**: Microsoft Azure Blob Storage integration
- **MinIOProvider**: Self-hosted S3-compatible storage

### File Processing
- **ImageProcessor**: Image resizing, format conversion, and optimization
- **VideoProcessor**: Video transcoding, thumbnail generation, and compression
- **DocumentProcessor**: Document preview generation and text extraction
- **ArchiveProcessor**: Archive extraction, compression, and management
- **VirusScanner**: File security scanning and malware detection

### Management Services
- **FileVersioningService**: File version control and history management
- **DuplicationDetectionService**: Duplicate file detection and deduplication
- **BackupService**: Automated backup and disaster recovery
- **AnalyticsService**: File usage analytics and reporting
- **QuotaManager**: Storage quota management and enforcement

## Getting Started

To use the File Storage Service, follow these steps:

1. Configure storage providers and credentials
2. Set up file processing pipelines
3. Configure security and access control
4. Set up metadata indexing and search
5. Enable monitoring and analytics

## Examples

### Basic File Storage Service Setup

```java
import com.exalt.file.storage.FileStorageService;
import com.exalt.file.storage.config.StorageConfiguration;
import com.exalt.file.storage.providers.AmazonS3Provider;
import com.exalt.file.storage.providers.GoogleCloudStorageProvider;
import com.exalt.file.storage.security.SecurityManager;

@Configuration
@EnableFileStorage
public class FileStorageConfig {
    
    @Bean
    public FileStorageService fileStorageService() {
        return FileStorageService.builder()
            .storageProviders(getStorageProviders())
            .fileProcessor(fileProcessor())
            .securityManager(securityManager())
            .metadataService(metadataService())
            .quotaManager(quotaManager())
            .enableVersioning(true)
            .enableDeduplication(true)
            .enableVirusScanning(true)
            .build();
    }
    
    @Bean
    public List<StorageProvider> getStorageProviders() {
        return Arrays.asList(
            new AmazonS3Provider(S3ProviderConfig.builder()
                .accessKey(environment.getProperty("aws.s3.access-key"))
                .secretKey(environment.getProperty("aws.s3.secret-key"))
                .region(environment.getProperty("aws.s3.region", "us-east-1"))
                .bucketName(environment.getProperty("aws.s3.bucket"))
                .enableSSE(true)
                .storageClass("STANDARD_IA")
                .enableTransferAcceleration(true)
                .maxRetries(3)
                .timeout(Duration.ofSeconds(30))
                .build()),
                
            new GoogleCloudStorageProvider(GCSProviderConfig.builder()
                .projectId(environment.getProperty("gcp.project-id"))
                .credentialsPath(environment.getProperty("gcp.credentials-path"))
                .bucketName(environment.getProperty("gcp.storage.bucket"))
                .storageClass("NEARLINE")
                .enableEncryption(true)
                .retrySettings(RetrySettings.newBuilder()
                    .setMaxAttempts(3)
                    .setInitialRetryDelay(Duration.ofMillis(100))
                    .setMaxRetryDelay(Duration.ofSeconds(5))
                    .build())
                .build()),
                
            new AzureBlobStorageProvider(AzureProviderConfig.builder()
                .accountName(environment.getProperty("azure.storage.account-name"))
                .accountKey(environment.getProperty("azure.storage.account-key"))
                .containerName(environment.getProperty("azure.storage.container"))
                .tier("Cool")
                .enableHttps(true)
                .maxRetries(3)
                .timeout(Duration.ofSeconds(30))
                .build())
        );
    }
    
    @Bean
    public FileProcessor fileProcessor() {
        return FileProcessor.builder()
            .imageProcessor(imageProcessor())
            .videoProcessor(videoProcessor())
            .documentProcessor(documentProcessor())
            .virusScanner(virusScanner())
            .enableAsyncProcessing(true)
            .processingTimeout(Duration.ofMinutes(10))
            .tempDirectory("/tmp/file-processing")
            .build();
    }
    
    @Bean
    public ImageProcessor imageProcessor() {
        return ImageProcessor.builder()
            .supportedFormats(Arrays.asList("JPEG", "PNG", "WebP", "TIFF", "BMP"))
            .qualitySettings(ImageQualitySettings.builder()
                .jpegQuality(85)
                .pngCompression(6)
                .webpQuality(80)
                .enableProgressive(true)
                .build())
            .thumbnailSizes(Arrays.asList(
                new ImageSize(150, 150, "thumbnail"),
                new ImageSize(300, 300, "small"),
                new ImageSize(800, 600, "medium"),
                new ImageSize(1920, 1080, "large")
            ))
            .watermarkConfig(WatermarkConfig.builder()
                .enabled(true)
                .watermarkPath("/assets/watermark.png")
                .position(WatermarkPosition.BOTTOM_RIGHT)
                .opacity(0.7f)
                .build())
            .build();
    }
    
    @Bean
    public SecurityManager securityManager() {
        return SecurityManager.builder()
            .encryptionService(encryptionService())
            .accessControlService(accessControlService())
            .auditService(auditService())
            .enableFileEncryption(true)
            .enableAccessLogging(true)
            .enableIntegrityChecks(true)
            .quarantineDirectory("/quarantine")
            .build();
    }
}
```

### File Upload and Storage

```java
// Upload a file with metadata
@RestController
@RequestMapping("/api/v1/files")
public class FileStorageController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category,
            @RequestParam(value = "visibility", defaultValue = "private") String visibility,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request) {
        
        try {
            // Validate file
            FileValidationResult validation = fileStorageService.validateFile(file);
            if (!validation.isValid()) {
                return ResponseEntity.badRequest()
                    .body(FileUploadResponse.error(validation.getErrorMessages()));
            }
            
            // Create file metadata
            FileMetadata metadata = FileMetadata.builder()
                .originalName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .category(category)
                .visibility(FileVisibility.valueOf(visibility.toUpperCase()))
                .description(description)
                .uploadedBy(getCurrentUserId(request))
                .uploadedAt(Instant.now())
                .tags(extractTagsFromRequest(request))
                .customAttributes(extractCustomAttributes(request))
                .build();
            
            // Upload file with processing
            FileUploadOptions options = FileUploadOptions.builder()
                .enableVirusScanning(true)
                .enableDeduplication(true)
                .generateThumbnails(isImageFile(file.getContentType()))
                .extractMetadata(true)
                .enableVersioning(true)
                .storageProvider("primary") // Use primary storage provider
                .build();
            
            FileStorageResult result = fileStorageService.uploadFile(
                file.getInputStream(),
                metadata,
                options
            );
            
            return ResponseEntity.ok(FileUploadResponse.success(result));
            
        } catch (FileProcessingException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(FileUploadResponse.error("File processing failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(FileUploadResponse.error("Upload failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileId,
            @RequestParam(value = "version", required = false) String version,
            HttpServletRequest request) {
        
        try {
            // Check access permissions
            String userId = getCurrentUserId(request);
            if (!fileStorageService.hasAccessPermission(fileId, userId, FilePermission.READ)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // Get file with version
            FileDownloadOptions options = FileDownloadOptions.builder()
                .version(version)
                .enableAccessLogging(true)
                .includeMetadata(true)
                .build();
            
            FileDownloadResult downloadResult = fileStorageService.downloadFile(fileId, options);
            
            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(downloadResult.getContentType()));
            headers.setContentLength(downloadResult.getSize());
            headers.setContentDispositionFormData("attachment", downloadResult.getOriginalName());
            headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(downloadResult.getInputStream()));
                
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

### File Processing and Transformation

```java
// Image processing service
@Service
public class ImageProcessingService {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ImageProcessor imageProcessor;
    
    @Async
    public CompletableFuture<Void> processImageAsync(String fileId) {
        try {
            FileDownloadResult originalFile = fileStorageService.downloadFile(fileId, 
                FileDownloadOptions.builder().includeMetadata(true).build());
            
            // Generate thumbnails
            List<ImageProcessingTask> thumbnailTasks = Arrays.asList(
                ImageProcessingTask.builder()
                    .taskType(ImageTaskType.RESIZE)
                    .outputName("thumbnail_small")
                    .width(150)
                    .height(150)
                    .maintainAspectRatio(true)
                    .quality(85)
                    .format("JPEG")
                    .build(),
                    
                ImageProcessingTask.builder()
                    .taskType(ImageTaskType.RESIZE)
                    .outputName("thumbnail_medium")
                    .width(300)
                    .height(300)
                    .maintainAspectRatio(true)
                    .quality(85)
                    .format("JPEG")
                    .build(),
                    
                ImageProcessingTask.builder()
                    .taskType(ImageTaskType.OPTIMIZE)
                    .outputName("optimized")
                    .quality(80)
                    .enableProgressive(true)
                    .stripMetadata(true)
                    .build()
            );
            
            // Process images
            ImageProcessingResult result = imageProcessor.processImage(
                originalFile.getInputStream(),
                thumbnailTasks
            );
            
            // Store processed images
            for (ProcessedImage processedImage : result.getProcessedImages()) {
                FileMetadata derivedMetadata = FileMetadata.builder()
                    .parentFileId(fileId)
                    .originalName(processedImage.getName())
                    .contentType(processedImage.getContentType())
                    .size(processedImage.getSize())
                    .category("image_derivative")
                    .visibility(originalFile.getMetadata().getVisibility())
                    .processingInfo(ProcessingInfo.builder()
                        .processedAt(Instant.now())
                        .processingType("image_resize")
                        .processingParameters(processedImage.getProcessingParameters())
                        .build())
                    .build();
                
                fileStorageService.uploadFile(
                    processedImage.getInputStream(),
                    derivedMetadata,
                    FileUploadOptions.builder()
                        .enableVirusScanning(false) // Skip for derivatives
                        .enableDeduplication(false)
                        .generateThumbnails(false)
                        .build()
                );
            }
            
            // Update original file with processing status
            fileStorageService.updateFileMetadata(fileId, 
                originalFile.getMetadata().toBuilder()
                    .processingStatus(ProcessingStatus.COMPLETED)
                    .lastProcessedAt(Instant.now())
                    .build());
            
        } catch (Exception e) {
            // Update processing status on failure
            fileStorageService.updateProcessingStatus(fileId, ProcessingStatus.FAILED, e.getMessage());
            throw new ImageProcessingException("Failed to process image: " + fileId, e);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @EventListener
    public void handleFileUploadedEvent(FileUploadedEvent event) {
        if (isImageFile(event.getFileMetadata().getContentType())) {
            processImageAsync(event.getFileId());
        }
    }
}
```

### File Versioning and History

```java
// File versioning service
@Service
public class FileVersioningService {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public FileVersion createNewVersion(String fileId, InputStream newContent, String changeDescription) {
        try {
            // Get current file
            FileMetadata currentMetadata = fileStorageService.getFileMetadata(fileId);
            
            // Create version metadata
            FileVersion newVersion = FileVersion.builder()
                .fileId(fileId)
                .versionNumber(currentMetadata.getCurrentVersion() + 1)
                .changeDescription(changeDescription)
                .createdBy(getCurrentUserId())
                .createdAt(Instant.now())
                .size(calculateContentSize(newContent))
                .checksumSHA256(calculateChecksum(newContent, "SHA-256"))
                .build();
            
            // Store new version
            String versionedFileId = fileStorageService.storeFileVersion(
                fileId, 
                newContent, 
                newVersion
            );
            
            // Update current metadata
            fileStorageService.updateFileMetadata(fileId,
                currentMetadata.toBuilder()
                    .currentVersion(newVersion.getVersionNumber())
                    .lastModifiedAt(Instant.now())
                    .lastModifiedBy(getCurrentUserId())
                    .build());
            
            return newVersion.toBuilder().versionedFileId(versionedFileId).build();
            
        } catch (Exception e) {
            throw new VersioningException("Failed to create new version for file: " + fileId, e);
        }
    }
    
    public List<FileVersion> getFileVersionHistory(String fileId, int limit, int offset) {
        return fileStorageService.getFileVersions(fileId, 
            VersionQueryOptions.builder()
                .limit(limit)
                .offset(offset)
                .includeMetadata(true)
                .sortOrder(SortOrder.DESCENDING)
                .sortBy("createdAt")
                .build());
    }
    
    public void restoreVersion(String fileId, int versionNumber) {
        try {
            FileVersion targetVersion = fileStorageService.getFileVersion(fileId, versionNumber);
            if (targetVersion == null) {
                throw new VersionNotFoundException("Version " + versionNumber + " not found for file: " + fileId);
            }
            
            // Download version content
            FileDownloadResult versionContent = fileStorageService.downloadFileVersion(
                fileId, 
                versionNumber
            );
            
            // Create restore version
            createNewVersion(
                fileId,
                versionContent.getInputStream(),
                "Restored from version " + versionNumber
            );
            
        } catch (Exception e) {
            throw new VersioningException("Failed to restore version " + versionNumber + " for file: " + fileId, e);
        }
    }
    
    public void pruneOldVersions(String fileId, VersionRetentionPolicy policy) {
        List<FileVersion> versions = getFileVersionHistory(fileId, Integer.MAX_VALUE, 0);
        List<FileVersion> versionsToDelete = policy.selectVersionsForDeletion(versions);
        
        for (FileVersion version : versionsToDelete) {
            fileStorageService.deleteFileVersion(fileId, version.getVersionNumber());
        }
    }
}
```

### File Search and Metadata Management

```java
// File search and indexing service
@Service
public class FileSearchService {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    
    public FileSearchResults searchFiles(FileSearchQuery query) {
        try {
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            // Add text search
            if (StringUtils.hasText(query.getSearchText())) {
                boolQuery.must(QueryBuilders.multiMatchQuery(query.getSearchText())
                    .field("originalName", 2.0f) // Boost filename matches
                    .field("description", 1.5f)
                    .field("extractedText")
                    .field("tags")
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS));
            }
            
            // Add category filter
            if (query.getCategories() != null && !query.getCategories().isEmpty()) {
                boolQuery.filter(QueryBuilders.termsQuery("category", query.getCategories()));
            }
            
            // Add content type filter
            if (query.getContentTypes() != null && !query.getContentTypes().isEmpty()) {
                boolQuery.filter(QueryBuilders.termsQuery("contentType", query.getContentTypes()));
            }
            
            // Add size range filter
            if (query.getMinSize() != null || query.getMaxSize() != null) {
                RangeQueryBuilder sizeRange = QueryBuilders.rangeQuery("size");
                if (query.getMinSize() != null) {
                    sizeRange.gte(query.getMinSize());
                }
                if (query.getMaxSize() != null) {
                    sizeRange.lte(query.getMaxSize());
                }
                boolQuery.filter(sizeRange);
            }
            
            // Add date range filter
            if (query.getUploadedAfter() != null || query.getUploadedBefore() != null) {
                RangeQueryBuilder dateRange = QueryBuilders.rangeQuery("uploadedAt");
                if (query.getUploadedAfter() != null) {
                    dateRange.gte(query.getUploadedAfter());
                }
                if (query.getUploadedBefore() != null) {
                    dateRange.lte(query.getUploadedBefore());
                }
                boolQuery.filter(dateRange);
            }
            
            // Add user access filter
            String currentUserId = getCurrentUserId();
            boolQuery.filter(QueryBuilders.boolQuery()
                .should(QueryBuilders.termQuery("uploadedBy", currentUserId))
                .should(QueryBuilders.termQuery("visibility", "public"))
                .should(QueryBuilders.nestedQuery("permissions",
                    QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("permissions.userId", currentUserId))
                        .must(QueryBuilders.termQuery("permissions.permission", "READ")),
                    ScoreMode.None)));
            
            // Build search request
            SearchRequest searchRequest = new SearchRequest("files")
                .source(new SearchSourceBuilder()
                    .query(boolQuery)
                    .from(query.getOffset())
                    .size(query.getLimit())
                    .sort(query.getSortBy(), SortOrder.fromString(query.getSortOrder()))
                    .aggregation(AggregationBuilders.terms("categories").field("category"))
                    .aggregation(AggregationBuilders.terms("contentTypes").field("contentType"))
                    .aggregation(AggregationBuilders.dateHistogram("uploadTrend")
                        .field("uploadedAt")
                        .calendarInterval(DateHistogramInterval.DAY)));
            
            SearchResponse response = elasticsearchTemplate.search(searchRequest, RequestOptions.DEFAULT);
            
            // Convert results
            List<FileSearchResult> results = Arrays.stream(response.getHits().getHits())
                .map(this::convertToFileSearchResult)
                .collect(Collectors.toList());
            
            // Extract aggregations
            Map<String, Object> aggregations = extractAggregations(response.getAggregations());
            
            return FileSearchResults.builder()
                .results(results)
                .totalHits(response.getHits().getTotalHits().value)
                .aggregations(aggregations)
                .took(response.getTook().getMillis())
                .build();
                
        } catch (Exception e) {
            throw new FileSearchException("Failed to search files", e);
        }
    }
    
    @EventListener
    public void indexFileMetadata(FileUploadedEvent event) {
        try {
            FileIndexDocument document = FileIndexDocument.builder()
                .fileId(event.getFileId())
                .originalName(event.getFileMetadata().getOriginalName())
                .contentType(event.getFileMetadata().getContentType())
                .size(event.getFileMetadata().getSize())
                .category(event.getFileMetadata().getCategory())
                .description(event.getFileMetadata().getDescription())
                .tags(event.getFileMetadata().getTags())
                .uploadedBy(event.getFileMetadata().getUploadedBy())
                .uploadedAt(event.getFileMetadata().getUploadedAt())
                .visibility(event.getFileMetadata().getVisibility())
                .extractedText(extractTextIfAvailable(event.getFileId()))
                .build();
            
            IndexRequest indexRequest = new IndexRequest("files")
                .id(event.getFileId())
                .source(objectMapper.writeValueAsString(document), XContentType.JSON);
            
            elasticsearchTemplate.index(indexRequest, RequestOptions.DEFAULT);
            
        } catch (Exception e) {
            log.error("Failed to index file metadata for file: " + event.getFileId(), e);
        }
    }
}
```

## Security and Access Control

### File Encryption and Security

```java
// File security and encryption service
@Service
public class FileSecurityService {
    
    @Autowired
    private EncryptionService encryptionService;
    
    @Autowired
    private AccessControlService accessControlService;
    
    @Autowired
    private AuditService auditService;
    
    public EncryptedFileResult encryptFile(String fileId, EncryptionOptions options) {
        try {
            // Download original file
            FileDownloadResult originalFile = fileStorageService.downloadFile(fileId,
                FileDownloadOptions.builder().includeMetadata(true).build());
            
            // Generate encryption key
            EncryptionKey encryptionKey = encryptionService.generateKey(options.getAlgorithm());
            
            // Encrypt file content
            EncryptedStream encryptedStream = encryptionService.encrypt(
                originalFile.getInputStream(),
                encryptionKey,
                options
            );
            
            // Update file metadata with encryption info
            FileMetadata updatedMetadata = originalFile.getMetadata().toBuilder()
                .encrypted(true)
                .encryptionAlgorithm(options.getAlgorithm())
                .encryptionKeyId(encryptionKey.getId())
                .encryptedAt(Instant.now())
                .build();
            
            // Replace original file with encrypted version
            fileStorageService.replaceFileContent(fileId, encryptedStream, updatedMetadata);
            
            // Store encryption key securely
            encryptionService.storeKey(encryptionKey, 
                KeyStorageOptions.builder()
                    .fileId(fileId)
                    .keyDerivation(options.getKeyDerivation())
                    .accessPolicy(options.getAccessPolicy())
                    .build());
            
            // Audit encryption
            auditService.logFileEncryption(fileId, getCurrentUserId(), options.getAlgorithm());
            
            return EncryptedFileResult.builder()
                .fileId(fileId)
                .encryptionKeyId(encryptionKey.getId())
                .algorithm(options.getAlgorithm())
                .encryptedAt(Instant.now())
                .build();
                
        } catch (Exception e) {
            throw new FileEncryptionException("Failed to encrypt file: " + fileId, e);
        }
    }
    
    public DecryptedFileStream decryptFile(String fileId, DecryptionCredentials credentials) {
        try {
            // Check access permissions
            if (!accessControlService.hasPermission(fileId, getCurrentUserId(), FilePermission.READ)) {
                throw new AccessDeniedException("User does not have read permission for file: " + fileId);
            }
            
            // Get file metadata
            FileMetadata metadata = fileStorageService.getFileMetadata(fileId);
            if (!metadata.isEncrypted()) {
                throw new FileNotEncryptedException("File is not encrypted: " + fileId);
            }
            
            // Retrieve encryption key
            EncryptionKey encryptionKey = encryptionService.retrieveKey(
                metadata.getEncryptionKeyId(),
                credentials
            );
            
            // Download encrypted file
            FileDownloadResult encryptedFile = fileStorageService.downloadFile(fileId,
                FileDownloadOptions.builder().includeMetadata(false).build());
            
            // Decrypt file stream
            DecryptedStream decryptedStream = encryptionService.decrypt(
                encryptedFile.getInputStream(),
                encryptionKey,
                DecryptionOptions.builder()
                    .algorithm(metadata.getEncryptionAlgorithm())
                    .verifyIntegrity(true)
                    .build()
            );
            
            // Audit decryption
            auditService.logFileDecryption(fileId, getCurrentUserId());
            
            return DecryptedFileStream.builder()
                .fileId(fileId)
                .inputStream(decryptedStream)
                .originalSize(metadata.getSize())
                .contentType(metadata.getContentType())
                .decryptedAt(Instant.now())
                .build();
                
        } catch (Exception e) {
            auditService.logFileDecryptionFailure(fileId, getCurrentUserId(), e.getMessage());
            throw new FileDecryptionException("Failed to decrypt file: " + fileId, e);
        }
    }
}
```

## Monitoring and Analytics

### File Usage Analytics

```java
// File analytics and reporting service
@Service
public class FileAnalyticsService {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private MetricsRegistry metricsRegistry;
    
    public FileUsageReport generateUsageReport(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            // Get user's files
            List<FileMetadata> userFiles = fileStorageService.getUserFiles(userId,
                FileQueryOptions.builder()
                    .uploadedAfter(startDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                    .uploadedBefore(endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC))
                    .includeDeleted(false)
                    .build());
            
            // Calculate storage usage
            long totalStorageUsed = userFiles.stream()
                .mapToLong(FileMetadata::getSize)
                .sum();
            
            // Calculate by category
            Map<String, Long> storageByCategory = userFiles.stream()
                .collect(Collectors.groupingBy(
                    FileMetadata::getCategory,
                    Collectors.summingLong(FileMetadata::getSize)
                ));
            
            // Calculate by content type
            Map<String, Long> storageByContentType = userFiles.stream()
                .collect(Collectors.groupingBy(
                    FileMetadata::getContentType,
                    Collectors.summingLong(FileMetadata::getSize)
                ));
            
            // Get download statistics
            FileDownloadStats downloadStats = getDownloadStatistics(userId, startDate, endDate);
            
            // Get bandwidth usage
            BandwidthUsage bandwidthUsage = getBandwidthUsage(userId, startDate, endDate);
            
            return FileUsageReport.builder()
                .userId(userId)
                .reportPeriod(DateRange.of(startDate, endDate))
                .totalFiles(userFiles.size())
                .totalStorageUsed(totalStorageUsed)
                .storageByCategory(storageByCategory)
                .storageByContentType(storageByContentType)
                .downloadStats(downloadStats)
                .bandwidthUsage(bandwidthUsage)
                .generatedAt(Instant.now())
                .build();
                
        } catch (Exception e) {
            throw new AnalyticsException("Failed to generate usage report for user: " + userId, e);
        }
    }
    
    public SystemStorageReport generateSystemReport(LocalDate reportDate) {
        try {
            // Get system-wide statistics
            long totalFiles = fileStorageService.getTotalFileCount();
            long totalStorageUsed = fileStorageService.getTotalStorageUsed();
            long activeUsers = fileStorageService.getActiveUserCount(reportDate);
            
            // Get growth metrics
            StorageGrowthMetrics growthMetrics = calculateStorageGrowth(reportDate);
            
            // Get provider statistics
            Map<String, ProviderStats> providerStats = getProviderStatistics();
            
            // Get top categories and content types
            List<CategoryUsage> topCategories = getTopCategories(10);
            List<ContentTypeUsage> topContentTypes = getTopContentTypes(10);
            
            // Get performance metrics
            PerformanceMetrics performanceMetrics = getPerformanceMetrics(reportDate);
            
            return SystemStorageReport.builder()
                .reportDate(reportDate)
                .totalFiles(totalFiles)
                .totalStorageUsed(totalStorageUsed)
                .activeUsers(activeUsers)
                .growthMetrics(growthMetrics)
                .providerStats(providerStats)
                .topCategories(topCategories)
                .topContentTypes(topContentTypes)
                .performanceMetrics(performanceMetrics)
                .generatedAt(Instant.now())
                .build();
                
        } catch (Exception e) {
            throw new AnalyticsException("Failed to generate system storage report", e);
        }
    }
}
```

## Best Practices

### Storage Optimization
- Implement intelligent tiering based on access patterns
- Use compression for appropriate file types
- Enable deduplication to reduce storage costs
- Regular cleanup of unused and expired files

### Security Best Practices
- Encrypt sensitive files at rest and in transit
- Implement proper access controls and audit logging
- Regular security scanning and vulnerability assessment
- Use secure file upload validation and virus scanning

### Performance Optimization
- Implement CDN for frequently accessed files
- Use appropriate caching strategies
- Optimize file processing workflows
- Monitor and tune storage provider performance

### Disaster Recovery
- Implement cross-region replication
- Regular backup verification and restore testing
- Document recovery procedures and RTO/RPO targets
- Maintain detailed disaster recovery runbooks