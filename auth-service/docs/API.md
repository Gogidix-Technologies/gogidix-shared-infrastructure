# Authentication Service API Documentation

## Core Authentication API

### AuthenticationManager
- `AuthenticationManager()`: Default constructor with secure defaults
- `AuthenticationManager(SecurityConfig config)`: Constructor with custom security configuration
- `AuthenticationResult authenticate(String email, String password)`: Authenticate user credentials
- `AuthenticationResult authenticateWithMFA(String email, String password, String mfaCode)`: Authenticate with multi-factor authentication
- `AuthenticationResult authenticateWithToken(String token)`: Authenticate using JWT token
- `boolean isAccountLocked(String email)`: Check if user account is locked
- `void unlockAccount(String email)`: Manually unlock user account
- `void lockAccount(String email, String reason)`: Lock user account with reason
- `void recordLoginAttempt(String email, boolean success, String ipAddress)`: Record login attempt
- `LoginAttemptInfo getLoginAttempts(String email)`: Get login attempt history
- `void resetFailedLoginAttempts(String email)`: Reset failed login attempt counter
- `boolean validatePassword(String rawPassword, String encodedPassword)`: Validate password against encoded version
- `SecurityConfig getConfig()`: Get current security configuration
- `void setConfig(SecurityConfig config)`: Update security configuration

### AuthenticationResult
- `AuthenticationResult(boolean success, User user)`: Constructor with success status and user
- `AuthenticationResult(boolean success, String errorMessage)`: Constructor with failure information
- `AuthenticationResult(boolean success, User user, boolean mfaRequired)`: Constructor with MFA requirement
- `boolean isSuccess()`: Check if authentication succeeded
- `User getUser()`: Get authenticated user details
- `String getErrorMessage()`: Get error message if authentication failed
- `boolean isMfaRequired()`: Check if multi-factor authentication is required
- `List<String> getRoles()`: Get user roles from authentication
- `Map<String, Object> getAttributes()`: Get additional user attributes
- `Instant getAuthenticationTime()`: Get authentication timestamp
- `Duration getValidityDuration()`: Get authentication validity duration
- `String getSessionId()`: Get session ID if session created

### UserManager
- `UserManager()`: Default constructor
- `UserManager(UserRepository repository, PasswordEncoder encoder)`: Constructor with dependencies
- `User createUser(UserRegistrationRequest request)`: Create new user account
- `User findById(UUID userId)`: Find user by unique ID
- `User findByEmail(String email)`: Find user by email address
- `User findByUsername(String username)`: Find user by username
- `List<User> findByRole(String roleName)`: Find users with specific role
- `boolean updateUser(UUID userId, UserUpdateRequest request)`: Update user information
- `boolean changePassword(UUID userId, String oldPassword, String newPassword)`: Change user password
- `boolean resetPassword(UUID userId, String newPassword)`: Reset user password (admin function)
- `boolean deleteUser(UUID userId)`: Delete user account
- `boolean enableUser(UUID userId)`: Enable user account
- `boolean disableUser(UUID userId)`: Disable user account
- `void assignRole(UUID userId, String roleName)`: Assign role to user
- `void removeRole(UUID userId, String roleName)`: Remove role from user
- `List<Role> getUserRoles(UUID userId)`: Get all roles for user
- `List<Permission> getUserPermissions(UUID userId)`: Get all permissions for user
- `void sendWelcomeEmail(User user)`: Send welcome email to new user
- `void sendPasswordResetEmail(String email)`: Send password reset email
- `boolean validateRegistrationData(UserRegistrationRequest request)`: Validate user registration data

### TokenManager
- `TokenManager()`: Default constructor with secure defaults
- `TokenManager(SecurityConfig config)`: Constructor with security configuration
- `String generateAccessToken(User user)`: Generate JWT access token
- `String generateRefreshToken(User user)`: Generate JWT refresh token
- `String generatePasswordResetToken(User user)`: Generate password reset token
- `String generateEmailVerificationToken(User user)`: Generate email verification token
- `TokenValidationResult validateToken(String token)`: Validate JWT token
- `TokenValidationResult validateToken(String token, TokenType type)`: Validate specific token type
- `Claims extractClaims(String token)`: Extract claims from JWT token
- `String extractUserId(String token)`: Extract user ID from token
- `List<String> extractRoles(String token)`: Extract roles from token
- `boolean isTokenExpired(String token)`: Check if token is expired
- `boolean isTokenRevoked(String token)`: Check if token is revoked
- `void revokeToken(String token)`: Revoke specific token
- `void revokeAllUserTokens(UUID userId)`: Revoke all tokens for user
- `String refreshAccessToken(String refreshToken)`: Create new access token from refresh token
- `Duration getAccessTokenExpiration()`: Get access token expiration time
- `Duration getRefreshTokenExpiration()`: Get refresh token expiration time
- `TokenStatistics getTokenStatistics()`: Get token usage statistics

## Role and Permission Management API

### RoleManager
- `RoleManager()`: Default constructor
- `RoleManager(RoleRepository repository)`: Constructor with repository dependency
- `Role createRole(String name, String description)`: Create new role
- `Role createRole(CreateRoleRequest request)`: Create role from request object
- `boolean deleteRole(UUID roleId)`: Delete existing role
- `Role findRoleById(UUID roleId)`: Find role by unique ID
- `Role findRoleByName(String name)`: Find role by name
- `List<Role> getAllRoles()`: Get all available roles
- `List<Role> getRolesByCategory(String category)`: Get roles by category
- `boolean updateRole(UUID roleId, UpdateRoleRequest request)`: Update role information
- `void assignPermission(UUID roleId, UUID permissionId)`: Assign permission to role
- `void removePermission(UUID roleId, UUID permissionId)`: Remove permission from role
- `List<Permission> getRolePermissions(UUID roleId)`: Get all permissions for role
- `void setRoleHierarchy(UUID parentRoleId, List<UUID> childRoleIds)`: Set role hierarchy
- `List<Role> getChildRoles(UUID roleId)`: Get child roles in hierarchy
- `List<Role> getParentRoles(UUID roleId)`: Get parent roles in hierarchy
- `boolean hasPermission(UUID roleId, String permissionName)`: Check if role has permission
- `RoleStatistics getRoleStatistics()`: Get role usage statistics

### PermissionManager
- `PermissionManager()`: Default constructor
- `PermissionManager(PermissionRepository repository)`: Constructor with repository dependency
- `Permission createPermission(String name, String description)`: Create new permission
- `Permission createPermission(CreatePermissionRequest request)`: Create permission from request
- `boolean deletePermission(UUID permissionId)`: Delete existing permission
- `Permission findPermissionById(UUID permissionId)`: Find permission by unique ID
- `Permission findPermissionByName(String name)`: Find permission by name
- `List<Permission> getAllPermissions()`: Get all available permissions
- `List<Permission> getPermissionsByResource(String resource)`: Get permissions for resource
- `boolean updatePermission(UUID permissionId, UpdatePermissionRequest request)`: Update permission
- `boolean checkUserPermission(UUID userId, String permissionName)`: Check if user has permission
- `boolean checkUserPermission(UUID userId, String resource, String action)`: Check resource-action permission
- `void grantPermission(UUID userId, UUID permissionId)`: Grant permission directly to user
- `void revokePermission(UUID userId, UUID permissionId)`: Revoke permission from user
- `List<Permission> getUserDirectPermissions(UUID userId)`: Get permissions assigned directly to user
- `List<Permission> getUserEffectivePermissions(UUID userId)`: Get all effective permissions (role + direct)
- `PermissionStatistics getPermissionStatistics()`: Get permission usage statistics

### Role
- `Role()`: Default constructor
- `Role(String name, String description)`: Constructor with name and description
- `UUID getId()`: Get role unique ID
- `String getName()`: Get role name
- `void setName(String name)`: Set role name
- `String getDescription()`: Get role description
- `void setDescription(String description)`: Set role description
- `String getCategory()`: Get role category
- `void setCategory(String category)`: Set role category
- `List<Permission> getPermissions()`: Get role permissions
- `void addPermission(Permission permission)`: Add permission to role
- `boolean removePermission(UUID permissionId)`: Remove permission from role
- `boolean hasPermission(String permissionName)`: Check if role has permission
- `List<Role> getChildRoles()`: Get child roles
- `List<Role> getParentRoles()`: Get parent roles
- `boolean isEnabled()`: Check if role is enabled
- `void setEnabled(boolean enabled)`: Set role enabled status
- `LocalDateTime getCreatedAt()`: Get creation timestamp
- `LocalDateTime getUpdatedAt()`: Get last update timestamp

### Permission
- `Permission()`: Default constructor
- `Permission(String name, String description, String resource, String action)`: Constructor with details
- `UUID getId()`: Get permission unique ID
- `String getName()`: Get permission name
- `void setName(String name)`: Set permission name
- `String getDescription()`: Get permission description
- `void setDescription(String description)`: Set permission description
- `String getResource()`: Get resource this permission applies to
- `void setResource(String resource)`: Set resource
- `String getAction()`: Get action this permission allows
- `void setAction(String action)`: Set action
- `String getCategory()`: Get permission category
- `void setCategory(String category)`: Set permission category
- `boolean isEnabled()`: Check if permission is enabled
- `void setEnabled(boolean enabled)`: Set permission enabled status
- `LocalDateTime getCreatedAt()`: Get creation timestamp
- `LocalDateTime getUpdatedAt()`: Get last update timestamp

## Multi-Factor Authentication API

### MFAManager
- `MFAManager()`: Default constructor
- `MFAManager(MFAConfig config)`: Constructor with MFA configuration
- `MFASetupResult setupTOTP(UUID userId)`: Setup Time-based One-Time Password for user
- `MFASetupResult setupSMS(UUID userId, String phoneNumber)`: Setup SMS-based MFA
- `MFASetupResult setupEmail(UUID userId)`: Setup email-based MFA
- `boolean verifyMFACode(UUID userId, String code, MFAMethod method)`: Verify MFA code
- `boolean verifyTOTPCode(UUID userId, String code)`: Verify TOTP code specifically
- `boolean verifySMSCode(UUID userId, String code)`: Verify SMS code specifically
- `boolean verifyEmailCode(UUID userId, String code)`: Verify email code specifically
- `List<String> generateBackupCodes(UUID userId)`: Generate backup codes for user
- `boolean verifyBackupCode(UUID userId, String code)`: Verify backup code
- `void regenerateBackupCodes(UUID userId)`: Regenerate backup codes
- `boolean disableMFA(UUID userId)`: Disable MFA for user
- `boolean isMFAEnabled(UUID userId)`: Check if MFA is enabled for user
- `List<MFAMethod> getEnabledMFAMethods(UUID userId)`: Get enabled MFA methods for user
- `void sendMFACode(UUID userId, MFAMethod method)`: Send MFA code via specified method
- `MFAStatistics getMFAStatistics()`: Get MFA usage statistics

### TOTPGenerator
- `TOTPGenerator()`: Default constructor with standard settings
- `TOTPGenerator(TOTPConfig config)`: Constructor with custom configuration
- `String generateSecretKey()`: Generate new TOTP secret key
- `String generateQRCodeUrl(String email, String issuer, String secretKey)`: Generate QR code URL for authenticator apps
- `String generateCode(String secretKey)`: Generate current TOTP code
- `String generateCode(String secretKey, long timestamp)`: Generate TOTP code for specific timestamp
- `boolean verifyCode(String secretKey, String code)`: Verify TOTP code
- `boolean verifyCode(String secretKey, String code, int validWindows)`: Verify with time window tolerance
- `List<String> generateRecoveryCodes(int count)`: Generate recovery codes
- `int getTimeStep()`: Get time step in seconds (default 30)
- `void setTimeStep(int timeStep)`: Set time step in seconds
- `int getCodeDigits()`: Get number of digits in code (default 6)
- `void setCodeDigits(int digits)`: Set number of digits in code
- `String getAlgorithm()`: Get hash algorithm (default SHA1)
- `void setAlgorithm(String algorithm)`: Set hash algorithm

### MFASetupResult
- `MFASetupResult(boolean success, MFAMethod method, String secretKey)`: Constructor for successful setup
- `MFASetupResult(boolean success, String errorMessage)`: Constructor for failed setup
- `boolean isSuccess()`: Check if setup was successful
- `MFAMethod getMethod()`: Get MFA method that was set up
- `String getSecretKey()`: Get secret key (for TOTP)
- `String getQrCodeUrl()`: Get QR code URL (for TOTP)
- `List<String> getBackupCodes()`: Get generated backup codes
- `String getErrorMessage()`: Get error message if setup failed
- `Map<String, Object> getSetupData()`: Get additional setup data

### MFAMethod
- `TOTP`: Time-based One-Time Password (Google Authenticator, Authy)
- `SMS`: SMS-based verification codes
- `EMAIL`: Email-based verification codes
- `BACKUP_CODE`: Backup recovery codes
- `HARDWARE_TOKEN`: Hardware security tokens
- `PUSH_NOTIFICATION`: Push notification to mobile app

## Session Management API

### SessionManager
- `SessionManager()`: Default constructor
- `SessionManager(SessionConfig config)`: Constructor with session configuration
- `Session createSession(User user, DeviceInfo deviceInfo)`: Create new user session
- `Session createSession(User user, DeviceInfo deviceInfo, Map<String, Object> attributes)`: Create session with attributes
- `boolean validateSession(String sessionId)`: Validate if session is active
- `Session getSession(String sessionId)`: Get session details
- `void extendSession(String sessionId)`: Extend session expiration
- `void extendSession(String sessionId, Duration extension)`: Extend session by specific duration
- `void terminateSession(String sessionId)`: Terminate specific session
- `void terminateAllUserSessions(UUID userId)`: Terminate all sessions for user
- `void terminateAllUserSessionsExcept(UUID userId, String currentSessionId)`: Terminate all sessions except current
- `List<Session> getActiveSessions(UUID userId)`: Get all active sessions for user
- `List<Session> getAllActiveSessions()`: Get all active sessions in system
- `SessionInfo getSessionInfo(String sessionId)`: Get detailed session information
- `void addSessionAttribute(String sessionId, String key, Object value)`: Add attribute to session
- `Object getSessionAttribute(String sessionId, String key)`: Get session attribute
- `void removeSessionAttribute(String sessionId, String key)`: Remove session attribute
- `void cleanupExpiredSessions()`: Clean up expired sessions
- `SessionStatistics getSessionStatistics()`: Get session usage statistics

### Session
- `Session()`: Default constructor
- `Session(String sessionId, UUID userId, DeviceInfo deviceInfo)`: Constructor with basic info
- `String getSessionId()`: Get unique session ID
- `UUID getUserId()`: Get user ID for this session
- `DeviceInfo getDeviceInfo()`: Get device information
- `void setDeviceInfo(DeviceInfo deviceInfo)`: Set device information
- `Instant getCreatedAt()`: Get session creation time
- `Instant getLastAccessedAt()`: Get last access time
- `void updateLastAccessed()`: Update last accessed timestamp
- `Instant getExpiresAt()`: Get session expiration time
- `void setExpiresAt(Instant expiresAt)`: Set session expiration time
- `boolean isExpired()`: Check if session is expired
- `boolean isActive()`: Check if session is active
- `String getIpAddress()`: Get client IP address
- `void setIpAddress(String ipAddress)`: Set client IP address
- `String getUserAgent()`: Get client user agent
- `void setUserAgent(String userAgent)`: Set client user agent
- `Map<String, Object> getAttributes()`: Get all session attributes
- `void addAttribute(String key, Object value)`: Add session attribute
- `Object getAttribute(String key)`: Get specific session attribute
- `void removeAttribute(String key)`: Remove session attribute

### DeviceInfo
- `DeviceInfo()`: Default constructor
- `DeviceInfo(String deviceId, String deviceType, String operatingSystem)`: Constructor with basic info
- `String getDeviceId()`: Get unique device identifier
- `void setDeviceId(String deviceId)`: Set device identifier
- `String getDeviceType()`: Get device type (mobile, desktop, tablet)
- `void setDeviceType(String deviceType)`: Set device type
- `String getOperatingSystem()`: Get operating system
- `void setOperatingSystem(String os)`: Set operating system
- `String getBrowser()`: Get browser information
- `void setBrowser(String browser)`: Set browser information
- `String getAppVersion()`: Get application version
- `void setAppVersion(String version)`: Set application version
- `boolean isTrusted()`: Check if device is trusted
- `void setTrusted(boolean trusted)`: Set device trust status
- `LocalDateTime getFirstSeen()`: Get first seen timestamp
- `LocalDateTime getLastSeen()`: Get last seen timestamp

## Security Audit API

### AuditLogger
- `AuditLogger()`: Default constructor
- `AuditLogger(AuditConfig config)`: Constructor with audit configuration
- `void logAuthentication(String email, boolean success, String ipAddress)`: Log authentication attempt
- `void logAuthentication(String email, boolean success, String ipAddress, Map<String, Object> details)`: Log with additional details
- `void logAuthorization(UUID userId, String resource, String action, boolean granted)`: Log authorization event
- `void logPasswordChange(UUID userId, String ipAddress)`: Log password change event
- `void logPasswordReset(String email, String ipAddress)`: Log password reset event
- `void logRoleChange(UUID userId, String oldRoles, String newRoles, UUID adminId)`: Log role modification
- `void logPermissionChange(UUID userId, String permission, String action, UUID adminId)`: Log permission change
- `void logAccountLockout(UUID userId, String reason, String ipAddress)`: Log account lockout
- `void logAccountUnlock(UUID userId, UUID adminId)`: Log account unlock
- `void logMFASetup(UUID userId, MFAMethod method, boolean success)`: Log MFA setup attempt
- `void logMFAVerification(UUID userId, MFAMethod method, boolean success)`: Log MFA verification
- `void logSessionCreation(String sessionId, UUID userId, String ipAddress)`: Log session creation
- `void logSessionTermination(String sessionId, UUID userId, String reason)`: Log session termination
- `void logSecurityEvent(SecurityEventType type, Map<String, Object> details)`: Log custom security event
- `List<AuditEvent> getAuditLog(AuditFilter filter)`: Get filtered audit log
- `List<AuditEvent> getUserAuditLog(UUID userId, LocalDateTime from, LocalDateTime to)`: Get user-specific audit log
- `void exportAuditLog(AuditFilter filter, OutputStream output)`: Export audit log to stream
- `AuditStatistics getAuditStatistics()`: Get audit statistics

### AuditEvent
- `AuditEvent()`: Default constructor
- `AuditEvent(SecurityEventType type, String description)`: Constructor with type and description
- `UUID getId()`: Get unique event ID
- `SecurityEventType getType()`: Get event type
- `String getDescription()`: Get event description
- `UUID getUserId()`: Get user ID associated with event
- `void setUserId(UUID userId)`: Set user ID
- `String getIpAddress()`: Get IP address
- `void setIpAddress(String ipAddress)`: Set IP address
- `String getUserAgent()`: Get user agent
- `void setUserAgent(String userAgent)`: Set user agent
- `Map<String, Object> getDetails()`: Get additional event details
- `void addDetail(String key, Object value)`: Add event detail
- `Object getDetail(String key)`: Get specific event detail
- `LocalDateTime getTimestamp()`: Get event timestamp
- `boolean isSuccessful()`: Check if event was successful
- `void setSuccessful(boolean successful)`: Set event success status
- `String getResource()`: Get resource involved in event
- `void setResource(String resource)`: Set resource
- `String getAction()`: Get action performed
- `void setAction(String action)`: Set action

### SecurityEventType
- `AUTHENTICATION_SUCCESS`: Successful user authentication
- `AUTHENTICATION_FAILURE`: Failed authentication attempt
- `AUTHORIZATION_GRANTED`: Authorization granted
- `AUTHORIZATION_DENIED`: Authorization denied
- `PASSWORD_CHANGED`: User password changed
- `PASSWORD_RESET`: Password reset performed
- `ACCOUNT_LOCKED`: User account locked
- `ACCOUNT_UNLOCKED`: User account unlocked
- `ROLE_ASSIGNED`: Role assigned to user
- `ROLE_REMOVED`: Role removed from user
- `PERMISSION_GRANTED`: Permission granted to user
- `PERMISSION_REVOKED`: Permission revoked from user
- `MFA_SETUP`: Multi-factor authentication setup
- `MFA_VERIFICATION`: MFA verification attempt
- `SESSION_CREATED`: User session created
- `SESSION_TERMINATED`: User session terminated
- `SUSPICIOUS_ACTIVITY`: Suspicious activity detected
- `SECURITY_VIOLATION`: Security policy violation

## Configuration API

### SecurityConfig
- `SecurityConfig()`: Default constructor with secure defaults
- `SecurityConfig.builder()`: Create configuration builder
- `String getJwtSecret()`: Get JWT signing secret
- `SecurityConfig jwtSecret(String secret)`: Set JWT signing secret
- `Duration getJwtExpirationTime()`: Get JWT expiration time
- `SecurityConfig jwtExpirationTime(Duration expiration)`: Set JWT expiration time
- `String getJwtIssuer()`: Get JWT issuer
- `SecurityConfig jwtIssuer(String issuer)`: Set JWT issuer
- `String getJwtAudience()`: Get JWT audience
- `SecurityConfig jwtAudience(String audience)`: Set JWT audience
- `int getPasswordMinLength()`: Get minimum password length
- `SecurityConfig passwordMinLength(int length)`: Set minimum password length
- `boolean isPasswordRequireUppercase()`: Check if password requires uppercase
- `SecurityConfig passwordRequireUppercase(boolean require)`: Set uppercase requirement
- `boolean isPasswordRequireLowercase()`: Check if password requires lowercase
- `SecurityConfig passwordRequireLowercase(boolean require)`: Set lowercase requirement
- `boolean isPasswordRequireDigits()`: Check if password requires digits
- `SecurityConfig passwordRequireDigits(boolean require)`: Set digits requirement
- `boolean isPasswordRequireSpecialChars()`: Check if password requires special characters
- `SecurityConfig passwordRequireSpecialChars(boolean require)`: Set special characters requirement
- `int getMaxLoginAttempts()`: Get maximum login attempts before lockout
- `SecurityConfig maxLoginAttempts(int attempts)`: Set maximum login attempts
- `Duration getLockoutDuration()`: Get account lockout duration
- `SecurityConfig lockoutDuration(Duration duration)`: Set lockout duration
- `Duration getSessionTimeout()`: Get session timeout duration
- `SecurityConfig sessionTimeout(Duration timeout)`: Set session timeout
- `boolean isMfaRequired()`: Check if MFA is required
- `SecurityConfig mfaRequired(boolean required)`: Set MFA requirement
- `SecurityConfig build()`: Build the configuration

### AuthConfig
- `AuthConfig()`: Default constructor
- `List<String> getExcludedPaths()`: Get paths excluded from authentication
- `AuthConfig excludePath(String path)`: Add path to exclude from authentication
- `AuthConfig excludePaths(List<String> paths)`: Set multiple excluded paths
- `TokenExtractionStrategy getTokenExtractionStrategy()`: Get token extraction strategy
- `AuthConfig tokenExtractionStrategy(TokenExtractionStrategy strategy)`: Set token extraction strategy
- `boolean isRequireSecureTransport()`: Check if HTTPS is required
- `AuthConfig requireSecureTransport(boolean require)`: Set HTTPS requirement
- `Duration getTokenValidationCacheTtl()`: Get token validation cache TTL
- `AuthConfig tokenValidationCacheTtl(Duration ttl)`: Set token validation cache TTL

### TokenExtractionStrategy
- `HEADER_BEARER`: Extract token from Authorization header with Bearer prefix
- `HEADER_CUSTOM`: Extract token from custom header
- `QUERY_PARAMETER`: Extract token from query parameter
- `COOKIE`: Extract token from HTTP cookie
- `MULTIPLE`: Try multiple extraction strategies