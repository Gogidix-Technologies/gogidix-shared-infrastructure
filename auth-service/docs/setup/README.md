# Setup Guide - Authentication Service

## Overview

This comprehensive setup guide covers the installation, configuration, and deployment of the Authentication Service for the Social E-commerce Ecosystem. It includes local development setup, Docker deployment, Kubernetes configuration, and production security hardening.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Database Setup](#database-setup)
4. [Security Configuration](#security-configuration)
5. [Docker Setup](#docker-setup)
6. [Kubernetes Setup](#kubernetes-setup)
7. [SSL/TLS Configuration](#ssltls-configuration)
8. [External Integrations](#external-integrations)
9. [Testing and Verification](#testing-and-verification)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 8GB RAM (16GB recommended for development)
- **Storage**: Minimum 10GB free space
- **Network**: Stable internet connection for external dependencies

### Required Software

#### Development Environment

```bash
# Java Development Kit
Java 17+ (OpenJDK or Oracle JDK)

# Build Tools
Maven 3.8+ or Gradle 7.5+

# Database
PostgreSQL 13+ (recommended)
MySQL 8.0+ (alternative)
Redis 6.0+ (for session storage)

# IDE (Optional but recommended)
IntelliJ IDEA or Visual Studio Code with Java extensions
```

#### Infrastructure Dependencies

```bash
# Service Discovery
Eureka Server (shared-infrastructure/service-registry)

# Message Broker (Optional)
Apache Kafka 3.0+ or RabbitMQ 3.10+

# Monitoring
Prometheus 2.37+
Grafana 9.0+

# Container Platform
Docker Desktop 4.0+ or Docker Engine 20.10+
Kubernetes 1.24+ (for production deployment)
```

### Installation Instructions

#### Java 17 and Maven

**Windows:**
```powershell
# Using Chocolatey
choco install openjdk17 maven

# Or using Scoop
scoop install openjdk17 maven

# Verify installation
java -version
mvn -version
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@17 maven

# Add Java to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@17"' >> ~/.zshrc
source ~/.zshrc

# Verify installation
java -version
mvn -version
```

**Linux (Ubuntu/Debian):**
```bash
# Update package list
sudo apt update

# Install OpenJDK 17 and Maven
sudo apt install openjdk-17-jdk maven

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc

# Verify installation
java -version
mvn -version
```

#### PostgreSQL Setup

**Windows:**
```powershell
# Download and install from postgresql.org
# Or using Chocolatey
choco install postgresql

# Start PostgreSQL service
net start postgresql-x64-13
```

**macOS:**
```bash
# Using Homebrew
brew install postgresql@13
brew services start postgresql@13

# Create user and database
createuser --interactive --pwprompt authservice
createdb -O authservice authservice_db
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Create user and database
sudo -u postgres createuser --interactive --pwprompt authservice
sudo -u postgres createdb -O authservice authservice_db
```

#### Redis Setup

**Windows:**
```powershell
# Using Chocolatey
choco install redis-64

# Or download from GitHub releases
# Start Redis service
redis-server
```

**macOS:**
```bash
# Using Homebrew
brew install redis
brew services start redis

# Test Redis
redis-cli ping
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt install redis-server

# Start Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Test Redis
redis-cli ping
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/auth-service
```

### 2. Environment Configuration

```bash
# Copy environment template
cp .env.template .env

# Edit configuration file
nano .env
```

Required environment variables:

```properties
# Service Configuration
SERVER_PORT=8083
SPRING_PROFILES_ACTIVE=dev
SERVICE_NAME=auth-service

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=authservice_db
DB_USERNAME=authservice
DB_PASSWORD=your_secure_password

# Redis Configuration (for sessions)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_EXPIRATION_ACCESS=3600
JWT_EXPIRATION_REFRESH=2592000
JWT_ISSUER=auth.exalt.com
JWT_AUDIENCE=exalt-services

# Password Policy
PASSWORD_MIN_LENGTH=8
PASSWORD_REQUIRE_UPPERCASE=true
PASSWORD_REQUIRE_LOWERCASE=true
PASSWORD_REQUIRE_DIGITS=true
PASSWORD_REQUIRE_SPECIAL=true

# Security Configuration
MAX_LOGIN_ATTEMPTS=5
ACCOUNT_LOCKOUT_DURATION=1800
SESSION_TIMEOUT=28800

# MFA Configuration
MFA_TOTP_ISSUER=Exalt E-commerce
MFA_SMS_ENABLED=false
MFA_EMAIL_ENABLED=true

# External Services (Optional)
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@exalt.com
SMTP_PASSWORD=your-app-password
SMTP_TLS_ENABLED=true

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka
EUREKA_INSTANCE_HOSTNAME=localhost

# Monitoring
METRICS_EXPORT_PROMETHEUS=true
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE=health,info,metrics,prometheus

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_AUTH=DEBUG
LOGGING_FILE_NAME=/var/log/auth-service/application.log
```

### 3. Build and Run

```bash
# Install dependencies
mvn clean install

# Run tests
mvn test

# Start the service
mvn spring-boot:run

# Or run the JAR directly
java -jar target/auth-service-1.0.0.jar
```

### 4. Verify Service Registration

```bash
# Check if service registered with Eureka
curl http://localhost:8761/eureka/apps/AUTH-SERVICE

# Check service health
curl http://localhost:8083/actuator/health

# Check database connectivity
curl http://localhost:8083/actuator/health/db
```

## Database Setup

### 1. Database Schema Initialization

The service uses Flyway for database migrations:

```sql
-- V1__Create_user_tables.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BOOLEAN DEFAULT true,
    account_locked BOOLEAN DEFAULT false,
    password_expired BOOLEAN DEFAULT false,
    failed_login_attempts INTEGER DEFAULT 0,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    category VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    resource VARCHAR(255),
    action VARCHAR(255),
    category VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Junction tables
CREATE TABLE user_roles (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    assigned_by UUID REFERENCES users(id),
    assigned_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,
    assigned_by UUID REFERENCES users(id),
    assigned_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id)
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_enabled ON users(enabled);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_permissions_name ON permissions(name);
```

### 2. Default Data Setup

```sql
-- V2__Insert_default_roles_and_permissions.sql
INSERT INTO roles (name, description, category) VALUES
('SUPER_ADMIN', 'Super Administrator with full system access', 'ADMIN'),
('ADMIN', 'System Administrator', 'ADMIN'),
('VENDOR_ADMIN', 'Vendor Administrator', 'VENDOR'),
('WAREHOUSE_ADMIN', 'Warehouse Administrator', 'WAREHOUSE'),
('MANAGER', 'General Manager', 'MANAGEMENT'),
('USER', 'Regular User', 'USER');

INSERT INTO permissions (name, description, resource, action, category) VALUES
-- User Management
('USER_CREATE', 'Create new users', 'USER', 'CREATE', 'USER_MANAGEMENT'),
('USER_READ', 'View user information', 'USER', 'READ', 'USER_MANAGEMENT'),
('USER_UPDATE', 'Update user information', 'USER', 'UPDATE', 'USER_MANAGEMENT'),
('USER_DELETE', 'Delete users', 'USER', 'DELETE', 'USER_MANAGEMENT'),

-- Role Management
('ROLE_CREATE', 'Create new roles', 'ROLE', 'CREATE', 'ROLE_MANAGEMENT'),
('ROLE_READ', 'View roles', 'ROLE', 'READ', 'ROLE_MANAGEMENT'),
('ROLE_UPDATE', 'Update roles', 'ROLE', 'UPDATE', 'ROLE_MANAGEMENT'),
('ROLE_DELETE', 'Delete roles', 'ROLE', 'DELETE', 'ROLE_MANAGEMENT'),

-- Order Management
('ORDER_CREATE', 'Create orders', 'ORDER', 'CREATE', 'ORDER_MANAGEMENT'),
('ORDER_READ', 'View orders', 'ORDER', 'READ', 'ORDER_MANAGEMENT'),
('ORDER_UPDATE', 'Update orders', 'ORDER', 'UPDATE', 'ORDER_MANAGEMENT'),
('ORDER_DELETE', 'Cancel orders', 'ORDER', 'DELETE', 'ORDER_MANAGEMENT'),

-- Analytics
('ANALYTICS_READ', 'View analytics data', 'ANALYTICS', 'READ', 'ANALYTICS'),
('ANALYTICS_EXPORT', 'Export analytics data', 'ANALYTICS', 'EXPORT', 'ANALYTICS'),

-- System Configuration
('SYSTEM_CONFIG', 'System configuration access', 'SYSTEM', 'CONFIG', 'SYSTEM');
```

### 3. Database Connection Configuration

```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:authservice_db}
    username: ${DB_USERNAME:authservice}
    password: ${DB_PASSWORD:password}
    driver-class-name: org.postgresql.Driver
    
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

## Security Configuration

### 1. JWT Configuration

```bash
# Generate secure JWT secret
openssl rand -base64 64 > jwt-secret.key

# For production, use RSA key pair
openssl genrsa -out jwt-private.pem 2048
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
```

```yaml
# application-security.yml
security:
  jwt:
    secret: ${JWT_SECRET:default-secret-change-this}
    expiration:
      access-token: ${JWT_EXPIRATION_ACCESS:3600}    # 1 hour
      refresh-token: ${JWT_EXPIRATION_REFRESH:2592000} # 30 days
    issuer: ${JWT_ISSUER:auth.exalt.com}
    audience: ${JWT_AUDIENCE:exalt-services}
    
  password-policy:
    min-length: ${PASSWORD_MIN_LENGTH:8}
    require-uppercase: ${PASSWORD_REQUIRE_UPPERCASE:true}
    require-lowercase: ${PASSWORD_REQUIRE_LOWERCASE:true}
    require-digits: ${PASSWORD_REQUIRE_DIGITS:true}
    require-special-characters: ${PASSWORD_REQUIRE_SPECIAL:true}
    
  account-lockout:
    max-attempts: ${MAX_LOGIN_ATTEMPTS:5}
    lockout-duration: ${ACCOUNT_LOCKOUT_DURATION:1800} # 30 minutes
    
  session:
    timeout: ${SESSION_TIMEOUT:28800} # 8 hours
    max-concurrent-sessions: 5
```

### 2. Password Encryption Configuration

```java
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public PasswordPolicyValidator passwordPolicyValidator() {
        return PasswordPolicyValidator.builder()
            .minLength(8)
            .requireUppercase(true)
            .requireLowercase(true)
            .requireDigits(true)
            .requireSpecialCharacters(true)
            .build();
    }
}
```

### 3. CORS Configuration

```yaml
cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:3001
    - https://app.exalt.com
    - https://admin.exalt.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
  allowed-headers:
    - Authorization
    - Content-Type
    - X-Requested-With
    - X-API-Version
  allow-credentials: true
  max-age: 3600
```

## Docker Setup

### 1. Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim

# Install curl for healthchecks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /app

# Copy application JAR
COPY target/auth-service-*.jar app.jar

# Create non-root user
RUN groupadd -r authservice && useradd -r -g authservice authservice
RUN chown -R authservice:authservice /app
USER authservice

# Expose ports
EXPOSE 8083 9090

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8083/actuator/health || exit 1

# JVM options for container
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseContainerSupport"

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 2. Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'

services:
  auth-service:
    build: .
    ports:
      - "8083:8083"
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka
    depends_on:
      - postgres
      - redis
      - eureka-server
    networks:
      - auth-network
    volumes:
      - ./logs:/var/log/auth-service
    restart: unless-stopped
    
  postgres:
    image: postgres:13
    environment:
      - POSTGRES_DB=authservice_db
      - POSTGRES_USER=authservice
      - POSTGRES_PASSWORD=secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    networks:
      - auth-network
    restart: unless-stopped
    
  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - auth-network
    restart: unless-stopped
    
  eureka-server:
    image: eureka-server:latest
    ports:
      - "8761:8761"
    networks:
      - auth-network
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

networks:
  auth-network:
    driver: bridge
```

### 3. Build and Run with Docker

```bash
# Build Docker image
docker build -t auth-service:latest .

# Run with Docker Compose
docker-compose up -d

# Check logs
docker-compose logs -f auth-service

# Check service health
curl http://localhost:8083/actuator/health

# Stop services
docker-compose down
```

## Kubernetes Setup

### 1. Namespace and Secrets

```bash
# Create namespace
kubectl create namespace auth-service

# Create database secret
kubectl create secret generic auth-db-secret \
  --from-literal=username=authservice \
  --from-literal=password=secure_password \
  --from-literal=database=authservice_db \
  -n auth-service

# Create JWT secret
kubectl create secret generic jwt-secret \
  --from-literal=secret=$(openssl rand -base64 64) \
  -n auth-service

# Create email credentials secret
kubectl create secret generic email-secret \
  --from-literal=username=your-email@exalt.com \
  --from-literal=password=your-app-password \
  -n auth-service
```

### 2. ConfigMap

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: auth-service-config
  namespace: auth-service
data:
  application.yml: |
    server:
      port: 8083
    spring:
      profiles:
        active: kubernetes
      datasource:
        url: jdbc:postgresql://postgres-service:5432/authservice_db
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
      redis:
        host: redis-service
        port: 6379
    eureka:
      client:
        service-url:
          defaultZone: http://eureka-server:8761/eureka
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      metrics:
        export:
          prometheus:
            enabled: true
```

### 3. Deployment

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-service
  namespace: auth-service
  labels:
    app: auth-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-service
  template:
    metadata:
      labels:
        app: auth-service
    spec:
      containers:
      - name: auth-service
        image: auth-service:latest
        ports:
        - containerPort: 8083
          name: http
        - containerPort: 9090
          name: metrics
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: auth-db-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: auth-db-secret
              key: password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        volumeMounts:
        - name: config-volume
          mountPath: /etc/config
        - name: logs-volume
          mountPath: /var/log/auth-service
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8083
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8083
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
        resources:
          limits:
            memory: "2Gi"
            cpu: "1000m"
          requests:
            memory: "1Gi"
            cpu: "500m"
      volumes:
      - name: config-volume
        configMap:
          name: auth-service-config
      - name: logs-volume
        emptyDir: {}
```

### 4. Service and Ingress

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: auth-service
  namespace: auth-service
  labels:
    app: auth-service
spec:
  type: ClusterIP
  ports:
  - port: 8083
    targetPort: 8083
    protocol: TCP
    name: http
  - port: 9090
    targetPort: 9090
    protocol: TCP
    name: metrics
  selector:
    app: auth-service

---
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: auth-service-ingress
  namespace: auth-service
  annotations:
    kubernetes.io/ingress.class: "nginx"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - auth.exalt.com
    secretName: auth-service-tls
  rules:
  - host: auth.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: auth-service
            port:
              number: 8083
```

### 5. Deploy to Kubernetes

```bash
# Apply all configurations
kubectl apply -f k8s/ -n auth-service

# Check deployment status
kubectl get pods -l app=auth-service -n auth-service

# View logs
kubectl logs -f deployment/auth-service -n auth-service

# Check service endpoints
kubectl get services -n auth-service

# Test external access
kubectl port-forward service/auth-service 8083:8083 -n auth-service
```

## SSL/TLS Configuration

### 1. Generate SSL Certificates

```bash
# For development - self-signed certificate
openssl req -x509 -newkey rsa:2048 -keyout auth-service.key -out auth-service.crt -days 365 -nodes \
  -subj "/CN=auth.exalt.com/O=Exalt/C=US"

# Create PKCS12 keystore
openssl pkcs12 -export -in auth-service.crt -inkey auth-service.key -out auth-service.p12 -name auth-service

# For production - use Let's Encrypt or CA-signed certificates
```

### 2. SSL Application Configuration

```yaml
# application-ssl.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:auth-service.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: auth-service
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers:
      - TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
      - TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
      - TLS_RSA_WITH_AES_256_GCM_SHA384
      - TLS_RSA_WITH_AES_128_GCM_SHA256

# HTTP to HTTPS redirect configuration
management:
  server:
    port: 9090
    ssl:
      enabled: false
```

### 3. HTTP to HTTPS Redirect

```java
@Configuration
public class HttpsRedirectConfig {
    
    @Bean
    public ServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint constraint = new SecurityConstraint();
                constraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                constraint.addCollection(collection);
                context.addConstraint(constraint);
            }
        };
        
        tomcat.addAdditionalTomcatConnectors(redirectConnector());
        return tomcat;
    }
    
    private Connector redirectConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8083);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
```

## External Integrations

### 1. SMTP Configuration

```yaml
# Email service configuration
spring:
  mail:
    host: ${SMTP_HOST:smtp.gmail.com}
    port: ${SMTP_PORT:587}
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
    properties:
      mail:
        smtp:
          starttls:
            enable: ${SMTP_TLS_ENABLED:true}
          auth: true
    default-encoding: UTF-8
```

### 2. SMS Provider Integration

```yaml
# SMS service configuration (Twilio example)
sms:
  provider: twilio
  twilio:
    account-sid: ${TWILIO_ACCOUNT_SID}
    auth-token: ${TWILIO_AUTH_TOKEN}
    from-number: ${TWILIO_FROM_NUMBER}
  
# Alternative SMS providers
sms:
  provider: aws-sns
  aws:
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    region: ${AWS_REGION:us-east-1}
```

### 3. OAuth Provider Configuration

```yaml
# OAuth 2.0 / OpenID Connect configuration
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,email,profile
          facebook:
            client-id: ${FACEBOOK_CLIENT_ID}
            client-secret: ${FACEBOOK_CLIENT_SECRET}
            scope: email,public_profile
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
```

## Testing and Verification

### 1. Health Checks

```bash
# Check service health
curl http://localhost:8083/actuator/health

# Check database connectivity
curl http://localhost:8083/actuator/health/db

# Check Redis connectivity
curl http://localhost:8083/actuator/health/redis

# Check Eureka registration
curl http://localhost:8083/actuator/health/eureka
```

### 2. Authentication Testing

```bash
# Register new user
curl -X POST http://localhost:8083/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@exalt.com",
    "password": "TestPass123!",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login user
curl -X POST http://localhost:8083/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@exalt.com",
    "password": "TestPass123!"
  }'

# Test JWT token validation
JWT_TOKEN="your-jwt-token-here"
curl -X GET http://localhost:8083/api/v1/auth/profile \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### 3. Authorization Testing

```bash
# Test role-based access
curl -X GET http://localhost:8083/api/v1/admin/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Test permission-based access
curl -X POST http://localhost:8083/api/v1/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TEST_ROLE",
    "description": "Test role for verification"
  }'
```

### 4. Load Testing

```bash
# Simple load test with curl
for i in {1..100}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8083/actuator/health &
done
wait

# Load test with Apache Bench
ab -n 1000 -c 10 http://localhost:8083/actuator/health

# Load test authentication endpoint
ab -n 100 -c 5 -p login.json -T application/json http://localhost:8083/api/v1/auth/login
```

## Troubleshooting

### Common Issues

#### 1. Service Won't Start

**Problem**: Service fails to start with various errors

**Solutions:**
```bash
# Check Java version
java -version

# Check if port is already in use
netstat -tlnp | grep 8083

# Check database connectivity
pg_isready -h localhost -p 5432

# Review application logs
tail -f logs/application.log

# Check environment variables
env | grep -E "(DB_|JWT_|SPRING_)"
```

#### 2. Database Connection Issues

**Problem**: Cannot connect to PostgreSQL database

**Solutions:**
```bash
# Test database connection
psql -h localhost -p 5432 -U authservice -d authservice_db

# Check PostgreSQL service status
sudo systemctl status postgresql

# Check PostgreSQL logs
sudo tail -f /var/log/postgresql/postgresql-13-main.log

# Verify database exists and user has permissions
sudo -u postgres psql -c "\l"
sudo -u postgres psql -c "\du"
```

#### 3. JWT Token Issues

**Problem**: JWT tokens not working or validation failures

**Solutions:**
```bash
# Check JWT secret configuration
echo $JWT_SECRET | base64 -d | wc -c  # Should be >= 32 bytes

# Validate token structure (use online JWT debugger)
echo $JWT_TOKEN | cut -d. -f2 | base64 -d | jq

# Check token expiration
curl -X POST http://localhost:8083/api/v1/auth/validate \
  -H "Content-Type: application/json" \
  -d '{"token": "'$JWT_TOKEN'"}'
```

#### 4. Eureka Registration Issues

**Problem**: Service not registering with Eureka

**Solutions:**
```bash
# Check Eureka server connectivity
curl http://localhost:8761/eureka/apps

# Verify Eureka configuration
curl http://localhost:8083/actuator/configprops | grep eureka

# Check network connectivity
telnet localhost 8761

# Review Eureka logs
docker logs eureka-server
```

#### 5. High Memory Usage

**Problem**: Service consuming too much memory

**Solutions:**
```bash
# Monitor memory usage
curl http://localhost:8083/actuator/metrics/jvm.memory.used

# Generate heap dump
jcmd $(pgrep -f auth-service) GC.run_finalization
jcmd $(pgrep -f auth-service) VM.dump_heap /tmp/auth-heap.hprof

# Adjust JVM settings
export JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC"
```

### Performance Tuning

#### JVM Optimization

```bash
# Production JVM settings
export JAVA_OPTS="
  -Xmx2g
  -Xms1g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+UseStringDeduplication
  -XX:+OptimizeStringConcat
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/auth-service/heapdumps/
  -Dserver.tomcat.max-threads=200
  -Dserver.tomcat.max-connections=2048
"
```

#### Database Optimization

```sql
-- Add database indexes for performance
CREATE INDEX CONCURRENTLY idx_users_last_login ON users(last_login);
CREATE INDEX CONCURRENTLY idx_audit_log_timestamp ON security_audit_log(created_at);
CREATE INDEX CONCURRENTLY idx_sessions_expires_at ON user_sessions(expires_at);

-- Optimize PostgreSQL settings
-- In postgresql.conf:
shared_buffers = 256MB
effective_cache_size = 1GB
random_page_cost = 1.1
```

### Monitoring Setup

```bash
# Check metrics endpoint
curl http://localhost:8083/actuator/metrics

# Monitor specific metrics
curl http://localhost:8083/actuator/metrics/http.server.requests
curl http://localhost:8083/actuator/metrics/jvm.memory.used
curl http://localhost:8083/actuator/metrics/jdbc.connections.active
```

## Next Steps

1. **Set up monitoring**: Configure Prometheus and Grafana
2. **Implement backup**: Set up database backup procedures
3. **Security hardening**: Implement additional security measures
4. **Performance testing**: Conduct comprehensive load testing
5. **Documentation**: Complete API documentation and user guides

## Support

- **Documentation**: `/docs` directory
- **API Reference**: `http://localhost:8083/swagger-ui.html`
- **Metrics**: `http://localhost:8083/actuator/metrics`
- **Health Checks**: `http://localhost:8083/actuator/health`
- **Issues**: GitHub Issues
- **Team Contact**: auth-team@exalt.com

---

*Last Updated: 2024-06-24*
*Document Version: 1.0*
*Review Schedule: Monthly*