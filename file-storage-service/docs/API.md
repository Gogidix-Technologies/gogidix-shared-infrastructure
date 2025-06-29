# File Storage Service API Documentation

## Core Storage Management API

### FileStorageService
- `FileStorageService(config)`: Initialize file storage service with configuration
- `FileStorageService(providers, processor, security)`: Initialize with specific components
- `uploadFile(inputStream, metadata, options)`: Upload file with metadata and processing options
- `uploadFile(multipartFile, metadata)`: Upload file from HTTP multipart request
- `uploadFileAsync(inputStream, metadata, options)`: Upload file asynchronously
- `downloadFile(fileId, options)`: Download file with specified options
- `downloadFile(fileId, version, options)`: Download specific version of file
- `downloadFileStream(fileId, range)`: Download file with byte range support
- `getFileMetadata(fileId)`: Get complete file metadata and properties
- `getFileMetadata(fileId, includeVersions)`: Get metadata with version history
- `updateFileMetadata(fileId, metadata)`: Update file metadata properties
- `deleteFile(fileId, options)`: Delete file with safety options
- `restoreFile(fileId, version)`: Restore file from backup or version
- `moveFile(fileId, newPath, options)`: Move file to different location or provider
- `copyFile(fileId, destinationPath, options)`: Copy file to new location
- `validateFile(fileData, validationRules)`: Validate file against rules
- `getFileUrl(fileId, urlOptions)`: Get temporary or permanent URL for file access
- `bulkUpload(files, options)`: Upload multiple files in batch operation
- `bulkDelete(fileIds, options)`: Delete multiple files in batch operation

### StorageProviderManager
- `StorageProviderManager(providers)`: Initialize with list of storage providers
- `StorageProviderManager(providers, config)`: Initialize with providers and configuration
- `addProvider(provider, priority)`: Add new storage provider with priority
- `removeProvider(providerId)`: Remove storage provider
- `getProvider(providerId)`: Get specific storage provider
- `getPrimaryProvider()`: Get primary storage provider
- `getProviderForFile(fileId)`: Get provider storing specific file
- `switchProvider(fileId, targetProviderId)`: Move file to different provider
- `getProviderHealth(providerId)`: Get health status of storage provider
- `getProviderMetrics(providerId)`: Get performance metrics for provider
- `setProviderPriority(providerId, priority)`: Set provider priority
- `enableProvider(providerId)`: Enable storage provider
- `disableProvider(providerId)`: Disable storage provider
- `testProvider(providerId)`: Test provider connectivity and functionality
- `syncProviders(fileId)`: Synchronize file across multiple providers
- `getProviderCapacity(providerId)`: Get storage capacity information
- `optimizeProviderUsage()`: Optimize file placement across providers

### FileProcessor
- `FileProcessor(processingEngines)`: Initialize file processor with engines
- `processFile(fileId, processingTasks)`: Process file with specified tasks
- `processFileAsync(fileId, processingTasks, callback)`: Process file asynchronously
- `generateThumbnails(fileId, sizes)`: Generate image thumbnails
- `convertFormat(fileId, targetFormat, options)`: Convert file format
- `compressFile(fileId, compressionOptions)`: Compress file to reduce size
- `extractMetadata(fileId)`: Extract metadata from file
- `extractText(fileId, options)`: Extract text content from file
- `generatePreview(fileId, previewOptions)`: Generate file preview
- `scanForViruses(fileId, scannerOptions)`: Scan file for malware
- `validateIntegrity(fileId, checksumType)`: Validate file integrity
- `optimizeFile(fileId, optimizationOptions)`: Optimize file for web delivery
- `getProcessingStatus(taskId)`: Get status of processing task
- `cancelProcessing(taskId)`: Cancel ongoing processing task
- `retryProcessing(taskId, retryOptions)`: Retry failed processing task
- `getProcessingHistory(fileId)`: Get processing history for file
- `batchProcess(fileIds, processingTasks)`: Process multiple files in batch

### MetadataService
- `MetadataService(indexer, storage)`: Initialize metadata service
- `indexFile(fileId, metadata)`: Index file metadata for search
- `updateIndex(fileId, metadata)`: Update indexed metadata
- `removeFromIndex(fileId)`: Remove file from search index
- `searchFiles(query, filters)`: Search files by metadata
- `getFilesByCategory(category, pagination)`: Get files by category
- `getFilesByContentType(contentType, pagination)`: Get files by content type
- `getFilesByUser(userId, filters)`: Get files uploaded by user
- `getFilesByDateRange(startDate, endDate, filters)`: Get files by date range
- `extractMetadata(fileContent, contentType)`: Extract metadata from file content
- `validateMetadata(metadata, schema)`: Validate metadata against schema
- `enrichMetadata(metadata, enrichmentSources)`: Enrich metadata with external data
- `normalizeMetadata(metadata)`: Normalize metadata format
- `getMetadataSchema(contentType)`: Get metadata schema for content type
- `createCustomMetadataField(fieldDefinition)`: Create custom metadata field

## File Processing API

### ImageProcessor
- `ImageProcessor(config)`: Initialize image processor with configuration
- `resizeImage(inputStream, width, height, options)`: Resize image to dimensions
- `cropImage(inputStream, cropArea, options)`: Crop image to specified area
- `rotateImage(inputStream, angle, options)`: Rotate image by angle
- `convertFormat(inputStream, targetFormat, quality)`: Convert image format
- `generateThumbnail(inputStream, size, options)`: Generate thumbnail
- `optimizeImage(inputStream, optimizationLevel)`: Optimize image for web
- `addWatermark(inputStream, watermarkOptions)`: Add watermark to image
- `applyFilter(inputStream, filterType, parameters)`: Apply image filter
- `detectFaces(inputStream, detectionOptions)`: Detect faces in image
- `extractColors(inputStream, colorCount)`: Extract dominant colors
- `getImageInfo(inputStream)`: Get image dimensions and properties
- `validateImage(inputStream, validationRules)`: Validate image properties
- `batchResize(images, sizes)`: Resize multiple images to different sizes
- `createImageMontage(images, layout)`: Create image montage/collage

### VideoProcessor
- `VideoProcessor(config)`: Initialize video processor with configuration
- `transcodeVideo(inputStream, targetFormat, quality)`: Transcode video format
- `generateThumbnail(inputStream, timeOffset, options)`: Generate video thumbnail
- `extractAudio(inputStream, audioFormat)`: Extract audio from video
- `compressVideo(inputStream, compressionOptions)`: Compress video file
- `addSubtitles(inputStream, subtitleFile, options)`: Add subtitles to video
- `trimVideo(inputStream, startTime, endTime)`: Trim video to time range
- `mergeVideos(videoStreams, mergeOptions)`: Merge multiple videos
- `addWatermark(inputStream, watermarkOptions)`: Add watermark to video
- `getVideoInfo(inputStream)`: Get video properties and metadata
- `validateVideo(inputStream, validationRules)`: Validate video properties
- `createVideoPreview(inputStream, previewOptions)`: Create video preview
- `extractFrames(inputStream, frameOptions)`: Extract frames from video
- `adjustVideoQuality(inputStream, qualitySettings)`: Adjust video quality
- `stabilizeVideo(inputStream, stabilizationOptions)`: Stabilize shaky video

### DocumentProcessor
- `DocumentProcessor(config)`: Initialize document processor
- `convertToPDF(inputStream, sourceFormat, options)`: Convert document to PDF
- `extractText(inputStream, options)`: Extract text from document
- `generateThumbnail(inputStream, pageNumber, options)`: Generate document thumbnail
- `splitDocument(inputStream, splitOptions)`: Split document into parts
- `mergeDocuments(documentStreams, mergeOptions)`: Merge multiple documents
- `addPassword(inputStream, password, encryptionLevel)`: Password protect document
- `removePassword(inputStream, currentPassword)`: Remove password protection
- `addWatermark(inputStream, watermarkOptions)`: Add watermark to document
- `compressDocument(inputStream, compressionLevel)`: Compress document
- `getDocumentInfo(inputStream)`: Get document properties and metadata
- `validateDocument(inputStream, validationRules)`: Validate document
- `ocrDocument(inputStream, ocrOptions)`: Perform OCR on document
- `annotateDocument(inputStream, annotations)`: Add annotations to document
- `redactDocument(inputStream, redactionAreas)`: Redact sensitive content

### ArchiveProcessor
- `ArchiveProcessor(config)`: Initialize archive processor
- `createArchive(files, archiveFormat, options)`: Create archive from files
- `extractArchive(archiveStream, extractionOptions)`: Extract files from archive
- `listArchiveContents(archiveStream)`: List contents of archive
- `addToArchive(archiveStream, newFiles, options)`: Add files to existing archive
- `removeFromArchive(archiveStream, filePaths)`: Remove files from archive
- `validateArchive(archiveStream, validationRules)`: Validate archive integrity
- `compressArchive(archiveStream, compressionLevel)`: Compress archive
- `encryptArchive(archiveStream, encryptionOptions)`: Encrypt archive
- `getArchiveInfo(archiveStream)`: Get archive properties and metadata
- `testArchive(archiveStream)`: Test archive for corruption
- `repairArchive(archiveStream, repairOptions)`: Repair corrupted archive
- `convertArchiveFormat(archiveStream, targetFormat)`: Convert archive format
- `splitArchive(archiveStream, splitSize)`: Split large archive into parts

## Security and Access Control API

### SecurityManager
- `SecurityManager(encryptionService, accessControl)`: Initialize security manager
- `encryptFile(fileId, encryptionOptions)`: Encrypt file with specified options
- `decryptFile(fileId, decryptionCredentials)`: Decrypt file with credentials
- `setFilePermissions(fileId, permissions)`: Set access permissions for file
- `getFilePermissions(fileId)`: Get current file permissions
- `checkAccess(fileId, userId, permission)`: Check user access to file
- `grantAccess(fileId, userId, permissions)`: Grant access to user
- `revokeAccess(fileId, userId, permissions)`: Revoke user access
- `createAccessToken(fileId, tokenOptions)`: Create temporary access token
- `validateAccessToken(token, fileId)`: Validate access token
- `auditFileAccess(fileId, userId, action)`: Log file access for audit
- `scanForMalware(fileId, scannerOptions)`: Scan file for malware
- `quarantineFile(fileId, reason)`: Quarantine suspicious file
- `releaseFromQuarantine(fileId, authorization)`: Release file from quarantine
- `generateFileHash(fileId, hashAlgorithm)`: Generate file integrity hash
- `verifyFileIntegrity(fileId, expectedHash)`: Verify file integrity

### EncryptionService
- `EncryptionService(keyManager, config)`: Initialize encryption service
- `generateKey(algorithm, keySize)`: Generate encryption key
- `encryptStream(inputStream, key, options)`: Encrypt data stream
- `decryptStream(encryptedStream, key, options)`: Decrypt data stream
- `encryptMetadata(metadata, key)`: Encrypt file metadata
- `decryptMetadata(encryptedMetadata, key)`: Decrypt file metadata
- `rotateKey(oldKey, newKey, fileIds)`: Rotate encryption keys for files
- `deriveKey(password, salt, iterations)`: Derive key from password
- `storeKey(key, keyStorageOptions)`: Store encryption key securely
- `retrieveKey(keyId, credentials)`: Retrieve stored encryption key
- `destroyKey(keyId, authorization)`: Securely destroy encryption key
- `getKeyInfo(keyId)`: Get encryption key information
- `validateKey(key, keyValidationRules)`: Validate encryption key
- `backupKeys(keyIds, backupOptions)`: Backup encryption keys
- `restoreKeys(backupData, restoreOptions)`: Restore encryption keys from backup

### AccessControlService
- `AccessControlService(policyEngine)`: Initialize access control service
- `createAccessPolicy(policyDefinition)`: Create new access policy
- `updateAccessPolicy(policyId, updates)`: Update existing access policy
- `deleteAccessPolicy(policyId)`: Delete access policy
- `evaluateAccess(resourceId, subject, action, context)`: Evaluate access request
- `grantPermission(resourceId, subject, permission)`: Grant specific permission
- `revokePermission(resourceId, subject, permission)`: Revoke specific permission
- `listPermissions(resourceId)`: List all permissions for resource
- `getUserPermissions(userId, resourceType)`: Get user permissions by resource type
- `createRole(roleDefinition)`: Create access role
- `assignRole(userId, roleId)`: Assign role to user
- `removeRole(userId, roleId)`: Remove role from user
- `getEffectivePermissions(userId, resourceId)`: Get effective permissions for user
- `auditAccessRequest(requestDetails)`: Audit access request
- `createAccessGroup(groupDefinition)`: Create access group
- `addUserToGroup(userId, groupId)`: Add user to access group
- `removeUserFromGroup(userId, groupId)`: Remove user from access group

## File Versioning and History API

### VersioningService
- `VersioningService(versionStore, config)`: Initialize versioning service
- `createVersion(fileId, content, changeDescription)`: Create new file version
- `getVersion(fileId, versionNumber)`: Get specific file version
- `getAllVersions(fileId, options)`: Get all versions of file
- `getVersionHistory(fileId, pagination)`: Get paginated version history
- `compareVersions(fileId, version1, version2)`: Compare two file versions
- `restoreVersion(fileId, versionNumber)`: Restore file to specific version
- `deleteVersion(fileId, versionNumber)`: Delete specific version
- `getVersionMetadata(fileId, versionNumber)`: Get version metadata
- `setVersionRetentionPolicy(fileId, policy)`: Set version retention policy
- `pruneOldVersions(fileId, retentionCriteria)`: Remove old versions based on criteria
- `lockVersion(fileId, versionNumber, reason)`: Lock version against deletion
- `unlockVersion(fileId, versionNumber, authorization)`: Unlock version
- `getVersionDiff(fileId, version1, version2)`: Get differences between versions
- `mergeVersions(fileId, versions, mergeStrategy)`: Merge multiple versions

### BackupService
- `BackupService(backupProviders, scheduler)`: Initialize backup service
- `createBackup(fileIds, backupOptions)`: Create backup of files
- `scheduleBackup(fileIds, schedule, options)`: Schedule automatic backup
- `restoreBackup(backupId, restoreOptions)`: Restore files from backup
- `listBackups(filters)`: List available backups
- `getBackupStatus(backupId)`: Get backup operation status
- `deleteBackup(backupId, confirmation)`: Delete backup
- `validateBackup(backupId)`: Validate backup integrity
- `testRestore(backupId, testOptions)`: Test backup restore process
- `getBackupMetadata(backupId)`: Get backup metadata and contents
- `setBackupRetentionPolicy(policy)`: Set backup retention policy
- `compressBackup(backupId, compressionOptions)`: Compress existing backup
- `encryptBackup(backupId, encryptionOptions)`: Encrypt backup
- `replicateBackup(backupId, targetLocation)`: Replicate backup to another location
- `getBackupStatistics(dateRange)`: Get backup operation statistics

## Search and Discovery API

### FileSearchService
- `FileSearchService(searchEngine, indexer)`: Initialize file search service
- `searchFiles(query, filters, pagination)`: Search files with query and filters
- `searchByContent(contentQuery, options)`: Search files by content
- `searchByMetadata(metadataQuery, options)`: Search files by metadata
- `searchByTags(tags, matchType, options)`: Search files by tags
- `getSearchSuggestions(partialQuery, limit)`: Get search suggestions
- `indexFile(fileId, indexingOptions)`: Index file for search
- `reindexFile(fileId, indexingOptions)`: Reindex existing file
- `removeFromIndex(fileId)`: Remove file from search index
- `getIndexStatus(fileId)`: Get file indexing status
- `buildIndex(fileIds, indexingOptions)`: Build search index for files
- `optimizeIndex()`: Optimize search index performance
- `getSearchStatistics()`: Get search usage statistics
- `createSavedSearch(searchDefinition)`: Create saved search query
- `executeSavedSearch(savedSearchId, parameters)`: Execute saved search
- `deleteSavedSearch(savedSearchId)`: Delete saved search

### ContentAnalyzer
- `ContentAnalyzer(analysisEngines)`: Initialize content analyzer
- `analyzeContent(fileId, analysisTypes)`: Analyze file content
- `extractText(fileId, extractionOptions)`: Extract text from file
- `detectLanguage(fileId)`: Detect language of text content
- `analyzeImageContent(fileId, analysisOptions)`: Analyze image content
- `detectObjects(fileId, detectionOptions)`: Detect objects in images
- `recognizeText(fileId, ocrOptions)`: Perform OCR on image/document
- `analyzeSentiment(fileId)`: Analyze sentiment of text content
- `extractEntities(fileId, entityTypes)`: Extract named entities from text
- `classifyContent(fileId, classificationModel)`: Classify file content
- `generateTags(fileId, tagGenerationOptions)`: Auto-generate tags for file
- `detectDuplicates(fileId, comparisonOptions)`: Detect duplicate content
- `analyzeMedia(fileId, mediaAnalysisOptions)`: Analyze audio/video content
- `getAnalysisResults(analysisId)`: Get content analysis results
- `scheduleAnalysis(fileIds, analysisTypes, schedule)`: Schedule content analysis

## Analytics and Reporting API

### AnalyticsService
- `AnalyticsService(metricsCollector, reporter)`: Initialize analytics service
- `trackFileEvent(eventType, fileId, eventData)`: Track file-related event
- `getFileUsageStats(fileId, dateRange)`: Get usage statistics for file
- `getUserUsageStats(userId, dateRange)`: Get usage statistics for user
- `getSystemUsageStats(dateRange)`: Get system-wide usage statistics
- `generateUsageReport(reportConfig)`: Generate custom usage report
- `getStorageMetrics(groupBy, dateRange)`: Get storage utilization metrics
- `getBandwidthMetrics(groupBy, dateRange)`: Get bandwidth usage metrics
- `getPerformanceMetrics(serviceComponent, dateRange)`: Get performance metrics
- `getTopFiles(metric, limit, dateRange)`: Get top files by specified metric
- `getTopUsers(metric, limit, dateRange)`: Get top users by specified metric
- `predictStorageGrowth(predictionPeriod)`: Predict future storage requirements
- `generateCostReport(costModel, dateRange)`: Generate storage cost report
- `scheduleReport(reportConfig, schedule)`: Schedule automatic report generation
- `exportReport(reportId, format)`: Export report in specified format
- `createDashboard(dashboardConfig)`: Create analytics dashboard

### MetricsCollector
- `MetricsCollector(metricsStore)`: Initialize metrics collector
- `collectStorageMetrics()`: Collect storage utilization metrics
- `collectPerformanceMetrics()`: Collect system performance metrics
- `collectUsageMetrics()`: Collect file usage metrics
- `collectErrorMetrics()`: Collect error and failure metrics
- `recordFileUpload(fileMetadata, uploadMetrics)`: Record file upload metrics
- `recordFileDownload(fileId, downloadMetrics)`: Record file download metrics
- `recordFileProcessing(taskId, processingMetrics)`: Record processing metrics
- `recordStorageOperation(operation, operationMetrics)`: Record storage operation
- `aggregateMetrics(rawMetrics, aggregationType)`: Aggregate raw metrics
- `exportMetrics(query, format)`: Export metrics data
- `getMetricDefinitions()`: Get available metric definitions
- `setMetricRetentionPolicy(metricType, retentionPolicy)`: Set metrics retention
- `archiveMetrics(archiveCriteria)`: Archive old metrics data
- `validateMetrics(metricsData)`: Validate metrics data integrity

## REST API Endpoints

### File Management Endpoints
- `POST /api/v1/files/upload`: Upload single file
- `POST /api/v1/files/upload/multi`: Upload multiple files
- `GET /api/v1/files/{fileId}`: Download file
- `GET /api/v1/files/{fileId}/metadata`: Get file metadata
- `PUT /api/v1/files/{fileId}/metadata`: Update file metadata
- `DELETE /api/v1/files/{fileId}`: Delete file
- `GET /api/v1/files/{fileId}/url`: Get temporary download URL
- `POST /api/v1/files/{fileId}/copy`: Copy file
- `POST /api/v1/files/{fileId}/move`: Move file
- `GET /api/v1/files`: List files with filtering

### File Processing Endpoints
- `POST /api/v1/files/{fileId}/process`: Start file processing
- `GET /api/v1/files/{fileId}/process/{taskId}/status`: Get processing status
- `POST /api/v1/files/{fileId}/process/{taskId}/cancel`: Cancel processing
- `GET /api/v1/files/{fileId}/thumbnails`: Get file thumbnails
- `POST /api/v1/files/{fileId}/convert`: Convert file format
- `POST /api/v1/files/{fileId}/compress`: Compress file
- `POST /api/v1/files/{fileId}/extract-text`: Extract text from file
- `POST /api/v1/files/{fileId}/scan`: Scan file for viruses

### File Versioning Endpoints
- `GET /api/v1/files/{fileId}/versions`: Get file version history
- `POST /api/v1/files/{fileId}/versions`: Create new version
- `GET /api/v1/files/{fileId}/versions/{version}`: Get specific version
- `POST /api/v1/files/{fileId}/versions/{version}/restore`: Restore version
- `DELETE /api/v1/files/{fileId}/versions/{version}`: Delete version
- `GET /api/v1/files/{fileId}/versions/compare`: Compare versions

### Search and Discovery Endpoints
- `GET /api/v1/search/files`: Search files
- `GET /api/v1/search/suggestions`: Get search suggestions
- `POST /api/v1/search/saved`: Create saved search
- `GET /api/v1/search/saved`: List saved searches
- `POST /api/v1/search/saved/{searchId}/execute`: Execute saved search
- `GET /api/v1/files/categories`: Get file categories
- `GET /api/v1/files/tags`: Get available tags

### Security and Access Control Endpoints
- `GET /api/v1/files/{fileId}/permissions`: Get file permissions
- `PUT /api/v1/files/{fileId}/permissions`: Set file permissions
- `POST /api/v1/files/{fileId}/encrypt`: Encrypt file
- `POST /api/v1/files/{fileId}/decrypt`: Decrypt file
- `POST /api/v1/files/{fileId}/tokens`: Create access token
- `POST /api/v1/files/{fileId}/quarantine`: Quarantine file
- `POST /api/v1/files/{fileId}/release`: Release from quarantine

### Analytics and Reporting Endpoints
- `GET /api/v1/analytics/usage`: Get usage analytics
- `GET /api/v1/analytics/storage`: Get storage analytics
- `GET /api/v1/analytics/performance`: Get performance analytics
- `POST /api/v1/reports/generate`: Generate custom report
- `GET /api/v1/reports`: List available reports
- `GET /api/v1/reports/{reportId}`: Get report details
- `GET /api/v1/reports/{reportId}/download`: Download report

### Administrative Endpoints
- `GET /api/v1/admin/providers`: Get storage providers
- `GET /api/v1/admin/providers/{providerId}/status`: Get provider status
- `POST /api/v1/admin/providers/{providerId}/test`: Test provider
- `GET /api/v1/admin/system/status`: Get system status
- `GET /api/v1/admin/system/metrics`: Get system metrics
- `POST /api/v1/admin/system/cleanup`: Trigger cleanup operations
- `GET /api/v1/admin/quotas`: Get storage quotas
- `PUT /api/v1/admin/quotas/{userId}`: Update user quota

### Health and Monitoring Endpoints
- `GET /health`: Service health status
- `GET /health/live`: Liveness probe for Kubernetes
- `GET /health/ready`: Readiness probe for Kubernetes
- `GET /metrics`: Prometheus metrics endpoint
- `GET /api/v1/status`: Service status and uptime
- `GET /api/v1/info`: Service information and version

## Error Handling

### FileStorageError
- `FileStorageError(message)`: Create generic file storage error
- `FileStorageError(message, code)`: Create error with specific code
- `getErrorCode()`: Get error code
- `getErrorDetails()`: Get detailed error information

### FileNotFoundException
- `FileNotFoundException(fileId)`: Create file not found error
- `getFileId()`: Get file ID from error

### FileUploadError
- `FileUploadError(filename, message)`: Create file upload error
- `getFilename()`: Get filename from error
- `getUploadDetails()`: Get upload error details

### FileProcessingError
- `FileProcessingError(fileId, taskType, message)`: Create processing error
- `getFileId()`: Get file ID that failed processing
- `getTaskType()`: Get processing task type
- `getProcessingDetails()`: Get processing error details

### StorageProviderError
- `StorageProviderError(providerId, message)`: Create storage provider error
- `getProviderId()`: Get provider ID from error
- `getProviderDetails()`: Get provider error details

### AccessDeniedError
- `AccessDeniedError(fileId, userId, action)`: Create access denied error
- `getFileId()`: Get file ID from error
- `getUserId()`: Get user ID from error
- `getAttemptedAction()`: Get attempted action

### QuotaExceededError
- `QuotaExceededError(userId, currentUsage, quotaLimit)`: Create quota exceeded error
- `getUserId()`: Get user ID from error
- `getCurrentUsage()`: Get current storage usage
- `getQuotaLimit()`: Get quota limit

### VirusDetectedError
- `VirusDetectedError(fileId, virusName, scannerName)`: Create virus detection error
- `getFileId()`: Get infected file ID
- `getVirusName()`: Get detected virus name
- `getScannerName()`: Get scanner that detected virus

### EncryptionError
- `EncryptionError(operation, fileId, message)`: Create encryption error
- `getOperation()`: Get failed encryption operation
- `getFileId()`: Get file ID from error
- `getEncryptionDetails()`: Get encryption error details

### ValidationError
- `ValidationError(field, value, rule, message)`: Create validation error
- `getField()`: Get field that failed validation
- `getValue()`: Get invalid value
- `getValidationRule()`: Get violated validation rule