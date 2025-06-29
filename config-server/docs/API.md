# Config Server API Documentation

## Core Configuration API

### ConfigServerController
- `ConfigServerController()`: Default constructor for configuration server controller
- `ConfigServerController(EnvironmentRepository repository)`: Initialize with custom environment repository
- `Environment getConfiguration(String application, String profile, String label)`: Retrieve complete configuration environment for application
- `Environment getConfiguration(String application, String profile, String label, boolean resolvePlaceholders)`: Get configuration with placeholder resolution option
- `PropertySource getPropertySource(String application, String profile, String label, String path)`: Get specific property source by path
- `Properties getProperties(String application, String profile, String label)`: Get configuration as properties
- `String getProperty(String application, String profile, String label, String key)`: Get single property value
- `String getProperty(String application, String profile, String label, String key, String defaultValue)`: Get property with default value
- `void refreshConfiguration(String application)`: Trigger configuration refresh for application
- `void refreshConfiguration(String application, String profile)`: Refresh specific application profile
- `ConfigStatus getConfigStatus(String application)`: Get current configuration status and metadata
- `ConfigHealth getHealth()`: Get configuration server health status
- `List<String> getApplications()`: Get list of all registered applications
- `List<String> getProfiles(String application)`: Get available profiles for specific application
- `List<String> getLabels(String application, String profile)`: Get available labels for application/profile combination
- `Map<String, Object> getAllProperties(String application, String profile, String label)`: Get all properties as map
- `ConfigurationMetadata getMetadata(String application, String profile, String label)`: Get configuration metadata

### EnvironmentRepository
- `EnvironmentRepository()`: Default constructor
- `Environment findOne(String application, String profile, String label)`: Find and return environment configuration
- `Environment findOne(String application, String profile, String label, boolean includeOrigin)`: Find environment with origin information
- `List<Environment> findAll(String application, String profile)`: Find all environments for application/profile
- `void save(String application, String profile, String label, Properties properties)`: Save configuration properties
- `void save(String application, String profile, String label, String key, String value)`: Save single property
- `void delete(String application, String profile, String label)`: Delete entire configuration
- `void delete(String application, String profile, String label, String key)`: Delete specific property
- `boolean exists(String application, String profile, String label)`: Check if configuration exists
- `boolean exists(String application, String profile, String label, String key)`: Check if specific property exists
- `List<String> getApplicationNames()`: Get all registered application names
- `List<String> getProfileNames(String application)`: Get profile names for application
- `long getLastModified(String application, String profile, String label)`: Get last modification timestamp
- `String getVersion(String application, String profile, String label)`: Get configuration version identifier

### PropertySourceLocator
- `PropertySourceLocator()`: Default constructor
- `PropertySource<?> locate(Environment environment)`: Locate property source for environment
- `PropertySource<?> locate(String name, Environment environment)`: Locate named property source
- `Collection<PropertySource<?>> locateCollection(Environment environment)`: Locate multiple property sources
- `PropertySource<?> locateWithFallback(Environment environment, PropertySource<?> fallback)`: Locate with fallback option
- `boolean supports(Environment environment)`: Check if environment is supported by locator
- `int getOrder()`: Get execution order for this locator
- `void setOrder(int order)`: Set execution order
- `String getName()`: Get locator name
- `void refresh()`: Refresh property source locations

### ConfigurationManager
- `ConfigurationManager()`: Default constructor
- `ConfigurationManager(EnvironmentRepository repository, ConfigValidator validator)`: Initialize with repository and validator
- `void updateConfiguration(String application, String profile, Map<String, Object> properties)`: Update configuration properties
- `void updateConfiguration(String application, String profile, String key, Object value)`: Update single configuration property
- `void mergeConfiguration(String application, String profile, Map<String, Object> properties)`: Merge properties with existing configuration
- `void deleteProperty(String application, String profile, String key)`: Delete specific configuration property
- `void deleteConfiguration(String application, String profile)`: Delete entire configuration
- `void copyConfiguration(String sourceApp, String sourceProfile, String targetApp, String targetProfile)`: Copy configuration between applications/profiles
- `void backupConfiguration(String application, String profile)`: Create configuration backup
- `void backupConfiguration(String application, String profile, String backupId)`: Create named backup
- `void restoreConfiguration(String application, String profile, String backupId)`: Restore configuration from backup
- `List<ConfigBackup> getBackups(String application, String profile)`: Get available backups
- `void deleteBackup(String application, String profile, String backupId)`: Delete specific backup
- `ConfigurationHistory getHistory(String application, String profile)`: Get configuration change history
- `ConfigurationDiff compareConfigurations(String app1, String profile1, String app2, String profile2)`: Compare configurations
- `ValidationResult validateConfiguration(String application, String profile)`: Validate configuration
- `void publishConfigurationChange(String application, String profile, ConfigChangeEvent event)`: Publish change event

## Environment and Profile Management API

### ProfileManager
- `ProfileManager()`: Default constructor
- `ProfileManager(EnvironmentRepository repository)`: Initialize with environment repository
- `void createProfile(String application, String profile)`: Create new profile
- `void createProfile(String application, String profile, String baseProfile)`: Create profile based on existing profile
- `void deleteProfile(String application, String profile)`: Delete profile
- `void copyProfile(String application, String sourceProfile, String targetProfile)`: Copy profile within application
- `void moveProfile(String application, String oldProfile, String newProfile)`: Rename profile
- `List<String> getProfiles(String application)`: Get all profiles for application
- `ProfileMetadata getProfileMetadata(String application, String profile)`: Get profile metadata and statistics
- `void setDefaultProfile(String application, String profile)`: Set default profile for application
- `String getDefaultProfile(String application)`: Get current default profile
- `void activateProfile(String application, String profile)`: Activate profile
- `void deactivateProfile(String application, String profile)`: Deactivate profile
- `List<String> getActiveProfiles(String application)`: Get currently active profiles
- `ProfileStatus getProfileStatus(String application, String profile)`: Get profile status information
- `void setProfileProperty(String application, String profile, String key, String value)`: Set profile-specific property
- `Map<String, String> getProfileProperties(String application, String profile)`: Get profile-specific properties

### LabelManager
- `LabelManager()`: Default constructor
- `LabelManager(EnvironmentRepository repository)`: Initialize with repository
- `void createLabel(String application, String profile, String label)`: Create new label
- `void deleteLabel(String application, String profile, String label)`: Delete label
- `void tagLabel(String application, String profile, String label, String tag)`: Tag label with metadata
- `List<String> getLabels(String application, String profile)`: Get available labels
- `List<String> getTags(String application, String profile, String label)`: Get tags for label
- `LabelMetadata getLabelMetadata(String application, String profile, String label)`: Get label metadata
- `void setCurrentLabel(String application, String profile, String label)`: Set current active label
- `String getCurrentLabel(String application, String profile)`: Get current active label
- `void promoteLabel(String application, String profile, String fromLabel, String toLabel)`: Promote label to another environment

## Encryption and Security API

### EncryptionController
- `EncryptionController()`: Default constructor
- `EncryptionController(TextEncryptor encryptor)`: Initialize with text encryptor
- `String encrypt(String plaintext)`: Encrypt plain text value
- `String decrypt(String ciphertext)`: Decrypt encrypted value
- `Map<String, String> encryptProperties(Map<String, String> properties)`: Encrypt multiple properties
- `Map<String, String> decryptProperties(Map<String, String> encryptedProperties)`: Decrypt multiple properties
- `boolean canDecrypt(String ciphertext)`: Check if value can be decrypted
- `boolean isEncrypted(String value)`: Check if value is encrypted
- `EncryptionStatus getEncryptionStatus()`: Get encryption configuration status
- `void rotateEncryptionKey()`: Rotate encryption key
- `void rotateEncryptionKey(String keyId)`: Rotate to specific key
- `List<String> getAvailableKeys()`: Get available encryption keys
- `String getCurrentKeyId()`: Get current encryption key ID
- `void setEncryptionAlgorithm(String algorithm)`: Set encryption algorithm
- `String getEncryptionAlgorithm()`: Get current encryption algorithm

### ConfigSecurityManager
- `ConfigSecurityManager()`: Default constructor
- `ConfigSecurityManager(ConfigSecurityProperties properties)`: Initialize with security properties
- `boolean authenticate(String username, String password)`: Authenticate user credentials
- `boolean authenticate(String token)`: Authenticate with JWT token
- `boolean authorize(String user, String application, String operation)`: Authorize user operation
- `boolean authorize(String user, String application, String profile, String operation)`: Authorize profile-specific operation
- `void grantAccess(String user, String application, List<String> permissions)`: Grant user access permissions
- `void revokeAccess(String user, String application)`: Revoke all user access
- `void revokeAccess(String user, String application, String permission)`: Revoke specific permission
- `List<String> getUserPermissions(String user, String application)`: Get user permissions
- `List<String> getAuthorizedUsers(String application)`: Get users with access to application
- `void auditAccess(String user, String application, String operation, boolean success)`: Record access audit
- `List<AccessLog> getAccessLogs(String application, Duration period)`: Get access logs for period
- `List<AccessLog> getUserAccessLogs(String user, Duration period)`: Get user-specific access logs
- `AccessStatistics getAccessStatistics(String application)`: Get access statistics
- `void createRole(String roleName, List<String> permissions)`: Create security role
- `void assignRole(String user, String roleName)`: Assign role to user
- `void removeRole(String user, String roleName)`: Remove role from user
- `List<String> getUserRoles(String user)`: Get user roles

### AuditService
- `AuditService()`: Default constructor
- `AuditService(AuditEventRepository repository)`: Initialize with audit repository
- `void recordEvent(AuditEvent event)`: Record audit event
- `void recordConfigurationAccess(String user, String application, String profile, String operation)`: Record configuration access
- `void recordConfigurationChange(String user, String application, String profile, String key, String oldValue, String newValue)`: Record configuration change
- `void recordAuthenticationEvent(String user, String operation, boolean success)`: Record authentication event
- `List<AuditEvent> getEvents(AuditQuery query)`: Get events matching query
- `List<AuditEvent> getEvents(String application, Duration period)`: Get events for application in period
- `List<AuditEvent> getUserEvents(String user, Duration period)`: Get user events in period
- `AuditReport generateReport(AuditReportRequest request)`: Generate audit report
- `void exportAuditLog(String application, LocalDate date, String format)`: Export audit log
- `void purgeOldEvents(Duration retentionPeriod)`: Purge old audit events
- `AuditStatistics getStatistics(String application)`: Get audit statistics

## Client Integration API

### ConfigServicePropertySourceLocator
- `ConfigServicePropertySourceLocator()`: Default constructor
- `ConfigServicePropertySourceLocator(ConfigClientProperties properties)`: Initialize with client properties
- `PropertySource<?> locate(Environment environment)`: Locate config server property source
- `PropertySource<?> locateWithRetry(Environment environment)`: Locate with retry logic
- `void setConfigServerInstanceProvider(ConfigServerInstanceProvider provider)`: Set server instance provider
- `void setRequestInterceptors(List<RequestInterceptor> interceptors)`: Set HTTP request interceptors
- `void setErrorHandler(ConfigServerErrorHandler errorHandler)`: Set error handler
- `PropertySource<?> getRemotePropertySource(String name, String profile, String label)`: Get remote property source
- `void invalidateCache()`: Invalidate cached property sources
- `boolean isFailFast()`: Check if fail-fast mode is enabled
- `void setFailFast(boolean failFast)`: Enable/disable fail-fast mode
- `Duration getTimeout()`: Get request timeout
- `void setTimeout(Duration timeout)`: Set request timeout

### RefreshEndpoint
- `RefreshEndpoint()`: Default constructor
- `RefreshEndpoint(ContextRefresher contextRefresher)`: Initialize with context refresher
- `Collection<String> refresh()`: Refresh all configuration
- `Collection<String> refresh(String application)`: Refresh specific application configuration
- `Collection<String> refreshScope(String scope)`: Refresh specific scope
- `RefreshResult refreshWithResult()`: Refresh and return detailed results
- `boolean isRefreshEnabled()`: Check if refresh is enabled
- `void setRefreshEnabled(boolean enabled)`: Enable/disable refresh capability
- `RefreshStatus getRefreshStatus()`: Get current refresh status
- `RefreshStatus getRefreshStatus(String application)`: Get application-specific refresh status
- `void addRefreshListener(RefreshListener listener)`: Add refresh event listener
- `void removeRefreshListener(RefreshListener listener)`: Remove refresh event listener
- `Duration getRefreshInterval()`: Get automatic refresh interval
- `void setRefreshInterval(Duration interval)`: Set automatic refresh interval

### ConfigWatchService
- `ConfigWatchService()`: Default constructor
- `ConfigWatchService(ConfigClientProperties properties)`: Initialize with client properties
- `void startWatching()`: Start configuration change watching
- `void startWatching(String application)`: Start watching specific application
- `void stopWatching()`: Stop all configuration watching
- `void stopWatching(String application)`: Stop watching specific application
- `boolean isWatching()`: Check if currently watching for changes
- `boolean isWatching(String application)`: Check if watching specific application
- `void addChangeListener(ConfigChangeListener listener)`: Add configuration change listener
- `void removeChangeListener(ConfigChangeListener listener)`: Remove configuration change listener
- `List<ConfigChangeListener> getChangeListeners()`: Get all registered change listeners
- `Duration getWatchInterval()`: Get watch polling interval
- `void setWatchInterval(Duration interval)`: Set watch polling interval
- `ConfigWatchStatus getWatchStatus()`: Get watching status information
- `void forceRefresh()`: Force immediate configuration refresh

## Repository Implementation API

### GitEnvironmentRepository
- `GitEnvironmentRepository()`: Default constructor
- `GitEnvironmentRepository(Environment environment)`: Initialize with environment configuration
- `Environment findOne(String application, String profile, String label)`: Find configuration in Git repository
- `void refresh()`: Refresh Git repository (pull latest changes)
- `void refresh(boolean force)`: Refresh with option to force update
- `String getUri()`: Get Git repository URI
- `void setUri(String uri)`: Set Git repository URI
- `String[] getSearchPaths()`: Get search paths for configuration files
- `void setSearchPaths(String[] searchPaths)`: Set search paths
- `String getUsername()`: Get Git username
- `void setUsername(String username)`: Set Git username
- `String getPassword()`: Get Git password (masked)
- `void setPassword(String password)`: Set Git password
- `boolean isCloneOnStart()`: Check if repository is cloned on startup
- `void setCloneOnStart(boolean cloneOnStart)`: Enable/disable clone on startup
- `String getBranch()`: Get current Git branch
- `void setBranch(String branch)`: Set Git branch
- `File getBasedir()`: Get local repository base directory
- `void setBasedir(File basedir)`: Set local repository base directory
- `GitRepositoryState getRepositoryState()`: Get current repository state

### VaultEnvironmentRepository
- `VaultEnvironmentRepository()`: Default constructor
- `VaultEnvironmentRepository(VaultTemplate vaultTemplate)`: Initialize with Vault template
- `Environment findOne(String application, String profile, String label)`: Find configuration in Vault
- `String getBackend()`: Get Vault backend path
- `void setBackend(String backend)`: Set Vault backend path
- `String getDefaultKey()`: Get default key for generic secrets
- `void setDefaultKey(String defaultKey)`: Set default key
- `String getProfileSeparator()`: Get profile separator character
- `void setProfileSeparator(String profileSeparator)`: Set profile separator
- `Map<String, String> findProperties(String path)`: Find properties at Vault path
- `void writeProperties(String path, Map<String, String> properties)`: Write properties to Vault
- `void deleteProperties(String path)`: Delete properties from Vault
- `boolean exists(String path)`: Check if path exists in Vault
- `List<String> list(String path)`: List available paths
- `VaultHealth getVaultHealth()`: Get Vault backend health status

### DatabaseEnvironmentRepository
- `DatabaseEnvironmentRepository()`: Default constructor
- `DatabaseEnvironmentRepository(JdbcTemplate jdbcTemplate)`: Initialize with JDBC template
- `Environment findOne(String application, String profile, String label)`: Find configuration in database
- `void save(String application, String profile, String label, Properties properties)`: Save configuration to database
- `void delete(String application, String profile, String label)`: Delete configuration from database
- `boolean exists(String application, String profile, String label)`: Check if configuration exists in database
- `List<ConfigEntity> findAll()`: Get all configuration entities
- `List<ConfigEntity> findByApplication(String application)`: Get configurations for application
- `void createTable()`: Create configuration table if not exists
- `void dropTable()`: Drop configuration table
- `String getTableName()`: Get configuration table name
- `void setTableName(String tableName)`: Set configuration table name
- `DatabaseMetadata getDatabaseMetadata()`: Get database metadata

## Monitoring and Management API

### ConfigMetricsCollector
- `ConfigMetricsCollector()`: Default constructor
- `ConfigMetricsCollector(MeterRegistry meterRegistry)`: Initialize with metrics registry
- `void recordConfigurationRequest(String application, String profile, Duration duration)`: Record configuration request metrics
- `void recordConfigurationError(String application, String error)`: Record configuration error
- `void recordRefreshEvent(String application, int propertiesChanged)`: Record refresh event metrics
- `void recordCacheHit(String application)`: Record cache hit
- `void recordCacheMiss(String application)`: Record cache miss
- `ConfigMetrics getMetrics()`: Get current configuration metrics
- `ConfigMetrics getMetrics(String application)`: Get application-specific metrics
- `ConfigMetrics getMetrics(Duration period)`: Get metrics for specific period
- `void resetMetrics()`: Reset all metrics counters
- `void resetMetrics(String application)`: Reset application-specific metrics
- `boolean isMetricsEnabled()`: Check if metrics collection is enabled
- `void setMetricsEnabled(boolean enabled)`: Enable/disable metrics collection

### ConfigHealthIndicator
- `ConfigHealthIndicator()`: Default constructor
- `ConfigHealthIndicator(EnvironmentRepository repository)`: Initialize with repository
- `Health health()`: Get configuration server health status
- `Health health(String application)`: Get application-specific health
- `ComponentHealth getComponentHealth(String component)`: Get specific component health
- `List<String> getUnhealthyComponents()`: Get list of unhealthy components
- `void addHealthContributor(String name, HealthContributor contributor)`: Add health contributor
- `void removeHealthContributor(String name)`: Remove health contributor
- `Map<String, HealthContributor> getHealthContributors()`: Get all health contributors
- `boolean isHealthy()`: Check overall health status
- `HealthSummary getHealthSummary()`: Get health summary

### ConfigAdminController
- `ConfigAdminController()`: Default constructor
- `ConfigAdminController(ConfigurationManager configManager, SecurityManager securityManager)`: Initialize with managers
- `List<ApplicationSummary> getApplications()`: Get summary of all applications
- `ApplicationDetails getApplicationDetails(String application)`: Get detailed application information
- `void createApplication(String application, ApplicationConfig config)`: Create new application
- `void deleteApplication(String application)`: Delete application and all its configurations
- `void purgeApplication(String application)`: Purge all data for application
- `SystemStatus getSystemStatus()`: Get overall system status
- `ConfigurationSummary getConfigurationSummary()`: Get configuration summary statistics
- `void maintenanceMode(boolean enabled)`: Enable/disable maintenance mode
- `boolean isMaintenanceMode()`: Check if maintenance mode is active
- `void flushCaches()`: Flush all caches
- `void reloadConfiguration()`: Reload server configuration
- `ServerInfo getServerInfo()`: Get server information
- `void shutdown()`: Gracefully shutdown configuration server

## Error Handling

### ConfigServerException
- `ConfigServerException(String message)`: Create exception with message
- `ConfigServerException(String message, Throwable cause)`: Create exception with message and cause
- `String getApplication()`: Get application name where error occurred
- `String getProfile()`: Get profile where error occurred
- `String getLabel()`: Get label where error occurred
- `ErrorCode getErrorCode()`: Get specific error code
- `Map<String, Object> getErrorDetails()`: Get additional error details

### ConfigurationNotFoundException
- `ConfigurationNotFoundException(String application, String profile)`: Create for missing configuration
- `ConfigurationNotFoundException(String application, String profile, String label)`: Create with label
- `String getApplication()`: Get application name
- `String getProfile()`: Get profile name
- `String getLabel()`: Get label name

### EncryptionException
- `EncryptionException(String message)`: Create encryption exception
- `EncryptionException(String message, Throwable cause)`: Create with cause
- `String getOperation()`: Get operation that failed (encrypt/decrypt)
- `String getValue()`: Get value that failed to process

### AuthorizationException
- `AuthorizationException(String user, String application, String operation)`: Create authorization exception
- `String getUser()`: Get user who was denied access
- `String getApplication()`: Get application access was denied for
- `String getOperation()`: Get operation that was denied
- `List<String> getRequiredPermissions()`: Get permissions required for operation