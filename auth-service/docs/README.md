# Authentication Service Documentation

## Overview

The Authentication Service provides centralized authentication and authorization capabilities for the Social E-commerce Ecosystem. It handles user registration, login, JWT token management, role-based access control (RBAC), multi-factor authentication (MFA), and security auditing across all platform services.

## Components

### Core Components
- **AuthenticationManager**: Central authentication processing engine
- **AuthorizationManager**: Role-based access control and permission management
- **UserManager**: User account management and profile handling
- **TokenManager**: JWT token generation, validation, and lifecycle management

### Security Components
- **PasswordEncoder**: Secure password hashing and validation using bcrypt
- **SecurityConfig**: Comprehensive security configuration and policies
- **MFAManager**: Multi-factor authentication implementation
- **AuditLogger**: Security event logging and audit trail management

### User Management Components
- **UserRepository**: User data persistence and retrieval
- **RoleManager**: Role definition and hierarchy management
- **PermissionManager**: Fine-grained permission control
- **SessionManager**: User session management and tracking

### Integration Components
- **LDAPConnector**: LDAP/Active Directory integration
- **SSOProvider**: Single Sign-On integration with external providers
- **APIKeyManager**: API key generation and validation for service-to-service auth
- **NotificationClient**: Security notification and alert system

## Getting Started

To use the Authentication Service, follow these steps:

1. Configure the authentication service with database and security settings
2. Set up user roles and permissions
3. Configure JWT token settings and signing keys
4. Enable multi-factor authentication if required
5. Integrate with external identity providers as needed

## Examples

### Configuring the Authentication Service

```java
import com.gogidix.auth.core.AuthenticationManager;
import com.gogidix.auth.core.AuthorizationManager;
import com.gogidix.auth.core.UserManager;
import com.gogidix.auth.core.TokenManager;
import com.gogidix.auth.security.SecurityConfig;
import com.gogidix.auth.security.PasswordEncoder;

@SpringBootApplication
@EnableAuthenticationService
public class AuthServiceApplication {
    private final AuthenticationManager authenticationManager;
    private final AuthorizationManager authorizationManager;
    private final UserManager userManager;
    private final TokenManager tokenManager;
    private final SecurityConfig securityConfig;
    
    public AuthServiceApplication() {
        this.securityConfig = new SecurityConfig();
        this.authenticationManager = new AuthenticationManager(securityConfig);
        this.authorizationManager = new AuthorizationManager();
        this.userManager = new UserManager();
        this.tokenManager = new TokenManager(securityConfig);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder(12); // bcrypt strength
    }
    
    @Bean
    public SecurityConfig securityConfig() {
        return SecurityConfig.builder()
            .jwtSecret("your-256-bit-secret-key")
            .jwtExpirationTime(Duration.ofHours(24))
            .passwordMinLength(8)
            .passwordRequireSpecialChars(true)
            .maxLoginAttempts(5)
            .lockoutDuration(Duration.ofMinutes(30))
            .sessionTimeout(Duration.ofHours(8))
            .build();
    }
}
```

### User Registration and Authentication

```java
import com.gogidix.auth.service.UserService;
import com.gogidix.auth.service.AuthenticationService;
import com.gogidix.auth.dto.UserRegistrationRequest;
import com.gogidix.auth.dto.LoginRequest;
import com.gogidix.auth.dto.AuthenticationResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    
    public AuthController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        try {
            // Validate registration data
            userService.validateRegistrationData(request);
            
            // Create user account
            User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
            );
            
            // Assign default roles
            userService.assignRole(user.getId(), "USER");
            
            // Send welcome email
            userService.sendWelcomeEmail(user);
            
            return ResponseEntity.ok(UserResponse.from(user));
            
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                    .error("USER_EXISTS")
                    .message("User with this email already exists")
                    .build());
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticate user credentials
            AuthenticationResult result = authenticationService.authenticate(
                request.getEmail(),
                request.getPassword()
            );
            
            if (!result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                        .success(false)
                        .message(result.getErrorMessage())
                        .build());
            }
            
            // Generate JWT token
            String accessToken = tokenManager.generateAccessToken(result.getUser());
            String refreshToken = tokenManager.generateRefreshToken(result.getUser());
            
            // Create user session
            Session session = sessionManager.createSession(result.getUser(), request.getDeviceInfo());
            
            return ResponseEntity.ok(AuthenticationResponse.builder()
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenManager.getAccessTokenExpiration())
                .user(UserResponse.from(result.getUser()))
                .build());
                
        } catch (AccountLockedException e) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                .body(AuthenticationResponse.builder()
                    .success(false)
                    .message("Account is locked due to multiple failed login attempts")
                    .lockoutDuration(e.getLockoutDuration())
                    .build());
        }
    }
}
```

### Role-Based Authorization

```java
import com.gogidix.auth.authorization.RoleManager;
import com.gogidix.auth.authorization.PermissionManager;
import com.gogidix.auth.model.Role;
import com.gogidix.auth.model.Permission;

@Service
public class RoleBasedAuthorizationService {
    private final RoleManager roleManager;
    private final PermissionManager permissionManager;
    
    public RoleBasedAuthorizationService(RoleManager roleManager, PermissionManager permissionManager) {
        this.roleManager = roleManager;
        this.permissionManager = permissionManager;
    }
    
    public void setupDefaultRoles() {
        // Create basic roles
        Role superAdmin = roleManager.createRole("SUPER_ADMIN", "Super Administrator with full access");
        Role admin = roleManager.createRole("ADMIN", "Administrator with management access");
        Role vendorAdmin = roleManager.createRole("VENDOR_ADMIN", "Vendor administrator");
        Role warehouseAdmin = roleManager.createRole("WAREHOUSE_ADMIN", "Warehouse administrator");
        Role user = roleManager.createRole("USER", "Regular user");
        
        // Define permissions
        Permission userManagement = permissionManager.createPermission("USER_MANAGEMENT", "Manage users");
        Permission orderManagement = permissionManager.createPermission("ORDER_MANAGEMENT", "Manage orders");
        Permission inventoryManagement = permissionManager.createPermission("INVENTORY_MANAGEMENT", "Manage inventory");
        Permission analyticsAccess = permissionManager.createPermission("ANALYTICS_ACCESS", "Access analytics");
        Permission systemConfig = permissionManager.createPermission("SYSTEM_CONFIG", "System configuration");
        
        // Assign permissions to roles
        roleManager.assignPermission(superAdmin.getId(), userManagement);
        roleManager.assignPermission(superAdmin.getId(), orderManagement);
        roleManager.assignPermission(superAdmin.getId(), inventoryManagement);
        roleManager.assignPermission(superAdmin.getId(), analyticsAccess);
        roleManager.assignPermission(superAdmin.getId(), systemConfig);
        
        roleManager.assignPermission(admin.getId(), userManagement);
        roleManager.assignPermission(admin.getId(), orderManagement);
        roleManager.assignPermission(admin.getId(), analyticsAccess);
        
        roleManager.assignPermission(vendorAdmin.getId(), orderManagement);
        roleManager.assignPermission(vendorAdmin.getId(), inventoryManagement);
        
        roleManager.assignPermission(warehouseAdmin.getId(), inventoryManagement);
        
        // Set up role hierarchy
        roleManager.setRoleHierarchy(superAdmin, Arrays.asList(admin, vendorAdmin, warehouseAdmin, user));
        roleManager.setRoleHierarchy(admin, Arrays.asList(vendorAdmin, warehouseAdmin, user));
        roleManager.setRoleHierarchy(vendorAdmin, Arrays.asList(user));
        roleManager.setRoleHierarchy(warehouseAdmin, Arrays.asList(user));
    }
    
    public boolean hasPermission(UUID userId, String permission) {
        User user = userService.findById(userId);
        if (user == null) {
            return false;
        }
        
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.getName().equals(permission));
    }
    
    public boolean hasRole(UUID userId, String roleName) {
        User user = userService.findById(userId);
        if (user == null) {
            return false;
        }
        
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }
}
```

### Multi-Factor Authentication

```java
import com.gogidix.auth.mfa.MFAManager;
import com.gogidix.auth.mfa.TOTPGenerator;
import com.gogidix.auth.mfa.SMSProvider;

@Service
public class MultiFactorAuthenticationService {
    private final MFAManager mfaManager;
    private final TOTPGenerator totpGenerator;
    private final SMSProvider smsProvider;
    
    public MultiFactorAuthenticationService(MFAManager mfaManager, TOTPGenerator totpGenerator, SMSProvider smsProvider) {
        this.mfaManager = mfaManager;
        this.totpGenerator = totpGenerator;
        this.smsProvider = smsProvider;
    }
    
    @PostMapping("/mfa/setup")
    public ResponseEntity<MFASetupResponse> setupMFA(@RequestBody MFASetupRequest request) {
        User user = getCurrentUser();
        
        switch (request.getMethod()) {
            case TOTP:
                return setupTOTP(user);
            case SMS:
                return setupSMS(user, request.getPhoneNumber());
            case EMAIL:
                return setupEmail(user);
            default:
                throw new UnsupportedMFAMethodException("MFA method not supported: " + request.getMethod());
        }
    }
    
    private ResponseEntity<MFASetupResponse> setupTOTP(User user) {
        // Generate secret key for TOTP
        String secretKey = totpGenerator.generateSecretKey();
        
        // Store secret key for user
        mfaManager.storeTOTPSecret(user.getId(), secretKey);
        
        // Generate QR code for authenticator app
        String qrCodeUrl = totpGenerator.generateQRCodeUrl(
            user.getEmail(),
            "Gogidix E-commerce",
            secretKey
        );
        
        return ResponseEntity.ok(MFASetupResponse.builder()
            .method(MFAMethod.TOTP)
            .secretKey(secretKey)
            .qrCodeUrl(qrCodeUrl)
            .backupCodes(mfaManager.generateBackupCodes(user.getId()))
            .build());
    }
    
    @PostMapping("/mfa/verify")
    public ResponseEntity<MFAVerificationResponse> verifyMFA(@RequestBody MFAVerificationRequest request) {
        User user = getCurrentUser();
        
        boolean isValid = false;
        
        switch (request.getMethod()) {
            case TOTP:
                isValid = totpGenerator.verifyCode(
                    mfaManager.getTOTPSecret(user.getId()),
                    request.getCode()
                );
                break;
            case SMS:
                isValid = smsProvider.verifyCode(
                    user.getPhoneNumber(),
                    request.getCode()
                );
                break;
            case BACKUP_CODE:
                isValid = mfaManager.verifyBackupCode(user.getId(), request.getCode());
                break;
        }
        
        if (isValid) {
            // Complete MFA verification
            mfaManager.completeMFAVerification(user.getId());
            
            // Generate final authentication token
            String finalToken = tokenManager.generateFinalToken(user);
            
            return ResponseEntity.ok(MFAVerificationResponse.builder()
                .success(true)
                .token(finalToken)
                .build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(MFAVerificationResponse.builder()
                    .success(false)
                    .message("Invalid MFA code")
                    .build());
        }
    }
}
```

### JWT Token Management

```java
import com.gogidix.auth.token.TokenManager;
import com.gogidix.auth.token.JWTProcessor;
import com.gogidix.auth.token.TokenBlacklist;

@Service
public class JWTTokenService {
    private final JWTProcessor jwtProcessor;
    private final TokenBlacklist tokenBlacklist;
    private final TokenManager tokenManager;
    
    public JWTTokenService(JWTProcessor jwtProcessor, TokenBlacklist tokenBlacklist, TokenManager tokenManager) {
        this.jwtProcessor = jwtProcessor;
        this.tokenBlacklist = tokenBlacklist;
        this.tokenManager = tokenManager;
    }
    
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getRoles().stream()
            .map(Role::getName)
            .collect(Collectors.toList()));
        claims.put("permissions", user.getPermissions().stream()
            .map(Permission::getName)
            .collect(Collectors.toList()));
        claims.put("sessionId", UUID.randomUUID().toString());
        
        return jwtProcessor.generateToken(
            user.getEmail(),
            claims,
            Duration.ofHours(1) // Access token expires in 1 hour
        );
    }
    
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("tokenType", "refresh");
        
        return jwtProcessor.generateToken(
            user.getEmail(),
            claims,
            Duration.ofDays(30) // Refresh token expires in 30 days
        );
    }
    
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            // Validate refresh token
            Claims claims = jwtProcessor.validateToken(request.getRefreshToken());
            
            // Check if token is blacklisted
            if (tokenBlacklist.isBlacklisted(request.getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenRefreshResponse.builder()
                        .success(false)
                        .message("Refresh token has been revoked")
                        .build());
            }
            
            // Get user from token
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            User user = userService.findById(userId);
            
            if (user == null || !user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(TokenRefreshResponse.builder()
                        .success(false)
                        .message("User account is disabled")
                        .build());
            }
            
            // Generate new access token
            String newAccessToken = generateAccessToken(user);
            
            return ResponseEntity.ok(TokenRefreshResponse.builder()
                .success(true)
                .accessToken(newAccessToken)
                .expiresIn(3600) // 1 hour
                .build());
                
        } catch (JWTValidationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(TokenRefreshResponse.builder()
                    .success(false)
                    .message("Invalid refresh token")
                    .build());
        }
    }
    
    @PostMapping("/token/revoke")
    public ResponseEntity<Void> revokeToken(@RequestBody TokenRevokeRequest request) {
        // Add token to blacklist
        tokenBlacklist.addToBlacklist(request.getToken());
        
        // If it's a refresh token, revoke all associated access tokens
        if (isRefreshToken(request.getToken())) {
            UUID userId = extractUserIdFromToken(request.getToken());
            tokenManager.revokeAllUserTokens(userId);
        }
        
        return ResponseEntity.ok().build();
    }
}
```

## API Reference

### Core Authentication API

#### AuthenticationManager
- `AuthenticationManager(SecurityConfig config)`: Initialize with security configuration
- `AuthenticationResult authenticate(String email, String password)`: Authenticate user credentials
- `AuthenticationResult authenticateWithMFA(String email, String password, String mfaCode)`: Authenticate with MFA
- `boolean isAccountLocked(String email)`: Check if account is locked
- `void unlockAccount(String email)`: Manually unlock account
- `void recordLoginAttempt(String email, boolean success)`: Record login attempt
- `LoginAttemptInfo getLoginAttempts(String email)`: Get login attempt history

#### UserManager
- `UserManager()`: Default constructor
- `User createUser(String username, String email, String password, String firstName, String lastName)`: Create new user
- `User findById(UUID userId)`: Find user by ID
- `User findByEmail(String email)`: Find user by email
- `User findByUsername(String username)`: Find user by username
- `boolean updateUser(UUID userId, UserUpdateRequest request)`: Update user information
- `boolean deleteUser(UUID userId)`: Delete user account
- `boolean enableUser(UUID userId)`: Enable user account
- `boolean disableUser(UUID userId)`: Disable user account
- `void assignRole(UUID userId, String roleName)`: Assign role to user
- `void removeRole(UUID userId, String roleName)`: Remove role from user
- `List<Role> getUserRoles(UUID userId)`: Get user roles
- `List<Permission> getUserPermissions(UUID userId)`: Get user permissions

#### TokenManager
- `TokenManager(SecurityConfig config)`: Initialize with configuration
- `String generateAccessToken(User user)`: Generate access token
- `String generateRefreshToken(User user)`: Generate refresh token
- `TokenValidationResult validateToken(String token)`: Validate token
- `Claims extractClaims(String token)`: Extract token claims
- `boolean isTokenExpired(String token)`: Check token expiration
- `void revokeToken(String token)`: Revoke specific token
- `void revokeAllUserTokens(UUID userId)`: Revoke all tokens for user
- `Duration getAccessTokenExpiration()`: Get access token expiration time
- `Duration getRefreshTokenExpiration()`: Get refresh token expiration time

### Role and Permission API

#### RoleManager
- `RoleManager()`: Default constructor
- `Role createRole(String name, String description)`: Create new role
- `boolean deleteRole(UUID roleId)`: Delete role
- `Role findRoleByName(String name)`: Find role by name
- `List<Role> getAllRoles()`: Get all roles
- `void assignPermission(UUID roleId, Permission permission)`: Assign permission to role
- `void removePermission(UUID roleId, UUID permissionId)`: Remove permission from role
- `List<Permission> getRolePermissions(UUID roleId)`: Get role permissions
- `void setRoleHierarchy(Role parentRole, List<Role> childRoles)`: Set role hierarchy
- `List<Role> getChildRoles(UUID roleId)`: Get child roles

#### PermissionManager
- `PermissionManager()`: Default constructor
- `Permission createPermission(String name, String description)`: Create new permission
- `boolean deletePermission(UUID permissionId)`: Delete permission
- `Permission findPermissionByName(String name)`: Find permission by name
- `List<Permission> getAllPermissions()`: Get all permissions
- `boolean checkPermission(UUID userId, String permissionName)`: Check user permission
- `void grantPermission(UUID userId, String permissionName)`: Grant permission to user
- `void revokePermission(UUID userId, String permissionName)`: Revoke permission from user

### Multi-Factor Authentication API

#### MFAManager
- `MFAManager()`: Default constructor
- `MFASetupResult setupTOTP(UUID userId)`: Setup TOTP for user
- `MFASetupResult setupSMS(UUID userId, String phoneNumber)`: Setup SMS MFA for user
- `boolean verifyMFACode(UUID userId, String code, MFAMethod method)`: Verify MFA code
- `List<String> generateBackupCodes(UUID userId)`: Generate backup codes
- `boolean verifyBackupCode(UUID userId, String code)`: Verify backup code
- `void disableMFA(UUID userId)`: Disable MFA for user
- `boolean isMFAEnabled(UUID userId)`: Check if MFA is enabled
- `List<MFAMethod> getEnabledMFAMethods(UUID userId)`: Get enabled MFA methods

#### TOTPGenerator
- `TOTPGenerator()`: Default constructor
- `String generateSecretKey()`: Generate TOTP secret key
- `String generateQRCodeUrl(String email, String issuer, String secretKey)`: Generate QR code URL
- `boolean verifyCode(String secretKey, String code)`: Verify TOTP code
- `String generateCode(String secretKey)`: Generate TOTP code
- `int getTimeStep()`: Get time step (usually 30 seconds)
- `int getCodeDigits()`: Get code length (usually 6 digits)

### Session Management API

#### SessionManager
- `SessionManager()`: Default constructor
- `Session createSession(User user, DeviceInfo deviceInfo)`: Create user session
- `boolean validateSession(String sessionId)`: Validate session
- `void extendSession(String sessionId)`: Extend session expiration
- `void terminateSession(String sessionId)`: Terminate specific session
- `void terminateAllUserSessions(UUID userId)`: Terminate all user sessions
- `List<Session> getActiveSessions(UUID userId)`: Get active sessions for user
- `SessionInfo getSessionInfo(String sessionId)`: Get session information
- `void cleanupExpiredSessions()`: Clean up expired sessions

#### AuditLogger
- `AuditLogger()`: Default constructor
- `void logAuthentication(String email, boolean success, String ipAddress)`: Log authentication event
- `void logAuthorization(UUID userId, String resource, String action, boolean granted)`: Log authorization event
- `void logPasswordChange(UUID userId, String ipAddress)`: Log password change
- `void logRoleChange(UUID userId, String oldRoles, String newRoles, UUID adminId)`: Log role change
- `void logSecurityEvent(SecurityEventType type, Map<String, Object> details)`: Log security event
- `List<AuditEvent> getAuditLog(AuditFilter filter)`: Get filtered audit log
- `void exportAuditLog(AuditFilter filter, OutputStream output)`: Export audit log

## Best Practices

1. **Password Security**: Use strong password policies and bcrypt hashing
2. **Token Management**: Implement proper token rotation and revocation
3. **MFA Implementation**: Provide multiple MFA options for users
4. **Session Security**: Implement secure session management with proper timeouts
5. **Audit Logging**: Maintain comprehensive security audit logs
6. **Role Hierarchy**: Design clear role hierarchies and permission structures