# Setup Guide - API Gateway

## Overview

This document provides comprehensive setup instructions for the API Gateway service, including local development environment, Docker setup, and cloud deployment with security configurations.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Setup](#kubernetes-setup)
5. [Security Configuration](#security-configuration)
6. [SSL/TLS Setup](#ssltls-setup)
7. [Service Discovery Setup](#service-discovery-setup)
8. [Verification](#verification)
9. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 8GB RAM (16GB recommended for development)
- **Storage**: Minimum 20GB free space
- **Network**: Stable internet connection with access to external services

### Required Software

#### Development Tools

```bash
# Java Development Kit
Java 17+ (OpenJDK or Oracle JDK)

# Build Tool
Maven 3.8+

# IDE (Recommended)
IntelliJ IDEA or Visual Studio Code

# Version Control
Git 2.30+
```

#### Infrastructure Dependencies

```bash
# Service Discovery
Eureka Server (part of shared-infrastructure)

# Security
JWT signing key/certificate

# Monitoring
Prometheus 2.37+
Grafana 9.0+

# Load Testing
Apache Bench or JMeter
```

#### Container and Orchestration

```bash
# Docker
Docker Desktop 4.0+ or Docker Engine 20.10+
Docker Compose 2.0+

# Kubernetes (for cloud deployment)
kubectl 1.24+
helm 3.8+

# SSL/TLS
OpenSSL 1.1.1+
```

### Installation Instructions

#### Java 17 and Maven

**Windows:**
```powershell
# Using Chocolatey
choco install openjdk17 maven

# Verify installation
java -version
mvn -version
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@17 maven

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk maven

# CentOS/RHEL
sudo yum install java-17-openjdk-devel maven
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/api-gateway
```

### 2. Environment Configuration

```bash
# Copy environment template
cp .env.template .env

# Edit with your local settings
nano .env
```

Required environment variables:
```properties
# Service Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
GATEWAY_NAME=api-gateway

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka
EUREKA_INSTANCE_HOSTNAME=localhost

# Security Configuration
JWT_SECRET=your-jwt-secret-key-min-256-bits
JWT_EXPIRATION_TIME=3600
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# Rate Limiting
RATE_LIMIT_GLOBAL_RPS=1000
RATE_LIMIT_USER_RPS=100
RATE_LIMIT_BURST_CAPACITY=2000

# Circuit Breaker
CIRCUIT_BREAKER_FAILURE_RATE=50
CIRCUIT_BREAKER_WAIT_DURATION=60s
CIRCUIT_BREAKER_SLIDING_WINDOW=10

# Backend Services
SOCIAL_COMMERCE_SERVICE_URL=http://localhost:8081
WAREHOUSING_SERVICE_URL=http://localhost:8082
ANALYTICS_SERVICE_URL=http://localhost:8200
AUTH_SERVICE_URL=http://localhost:8083

# SSL/TLS (for production)
SSL_ENABLED=false
SSL_KEYSTORE_PATH=/etc/ssl/keystore.p12
SSL_KEYSTORE_PASSWORD=your-keystore-password
SSL_KEY_ALIAS=api-gateway

# Monitoring
METRICS_EXPORT_PROMETHEUS=true
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE=health,info,metrics,prometheus

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_GATEWAY=DEBUG
LOGGING_FILE_NAME=/var/log/api-gateway/application.log
```

### 3. Infrastructure Setup

#### Option A: Start Dependencies with Docker Compose

```bash
# Start Eureka and monitoring stack
docker-compose -f docker-compose.dev.yml up -d eureka-server prometheus grafana

# Verify services are running
docker-compose ps
```

#### Option B: Native Eureka Setup

```bash
# Navigate to service registry
cd ../service-registry

# Start Eureka server
mvn spring-boot:run

# Verify Eureka is running
curl http://localhost:8761/eureka/apps
```

### 4. Generate JWT Keys

```bash
# Generate JWT signing key
openssl rand -base64 32 > jwt-secret.key

# For production, generate RSA key pair
openssl genrsa -out jwt-private.pem 2048
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem

# Set environment variable
export JWT_SECRET=$(cat jwt-secret.key)
```

### 5. Build and Run

```bash
# Install dependencies and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Start the API Gateway
mvn spring-boot:run

# Or run the JAR directly
java -jar target/api-gateway-1.0.0.jar
```

### 6. Verify Gateway Registration

```bash
# Check if gateway registered with Eureka
curl http://localhost:8761/eureka/apps/API-GATEWAY

# Check gateway health
curl http://localhost:8080/actuator/health

# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes
```

## Docker Setup

### 1. Build Docker Image

```bash
# Build the image
docker build -t api-gateway:latest .

# Build with custom JVM settings
docker build \
  --build-arg JAVA_OPTS="-Xmx2g -Xms1g" \
  -t api-gateway:production .

# Or use the build script
./scripts/build-docker.sh
```

### 2. Docker Compose Configuration

```yaml
# docker-compose.yml
version: '3.8'
services:
  api-gateway:
    image: api-gateway:latest
    ports:
      - "8080:8080"
      - "9090:9090"  # Prometheus metrics
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_SERVER_URL=http://eureka-server:8761/eureka
      - JWT_SECRET=${JWT_SECRET}
      - SOCIAL_COMMERCE_SERVICE_URL=http://social-commerce:8081
      - ANALYTICS_SERVICE_URL=http://analytics-engine:8200
    depends_on:
      - eureka-server
    networks:
      - gateway-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    
  eureka-server:
    image: eureka-server:latest
    ports:
      - "8761:8761"
    networks:
      - gateway-network

networks:
  gateway-network:
    driver: bridge
```

### 3. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f api-gateway

# Scale the gateway
docker-compose up -d --scale api-gateway=3

# Stop services
docker-compose down
```

## Kubernetes Setup

### 1. Prerequisites

```bash
# Create namespace
kubectl create namespace api-gateway

# Create secrets for JWT
kubectl create secret generic jwt-secret \
  --from-literal=jwt-secret-key=$(openssl rand -base64 32) \
  -n api-gateway

# Create SSL certificate secret (for production)
kubectl create secret tls api-gateway-tls \
  --cert=api-gateway.crt \
  --key=api-gateway.key \
  -n api-gateway

# Create configmap for application properties
kubectl create configmap api-gateway-config \
  --from-file=application.yml \
  -n api-gateway
```

### 2. Kubernetes Manifests

#### Deployment Configuration

```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: api-gateway
  namespace: api-gateway
  labels:
    app: api-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: api-gateway
  template:
    metadata:
      labels:
        app: api-gateway
    spec:
      containers:
      - name: api-gateway
        image: api-gateway:latest
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 9090
          name: metrics
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: jwt-secret-key
        - name: EUREKA_SERVER_URL
          value: "http://eureka-server:8761/eureka"
        volumeMounts:
        - name: config-volume
          mountPath: /etc/config
        - name: ssl-certs
          mountPath: /etc/ssl/certs
          readOnly: true
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
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
          name: api-gateway-config
      - name: ssl-certs
        secret:
          secretName: api-gateway-tls
```

#### Service Configuration

```yaml
# k8s/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: api-gateway
  namespace: api-gateway
  labels:
    app: api-gateway
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
    name: http
  - port: 443
    targetPort: 8443
    protocol: TCP
    name: https
  - port: 9090
    targetPort: 9090
    protocol: TCP
    name: metrics
  selector:
    app: api-gateway
```

#### Ingress Configuration

```yaml
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: api-gateway-ingress
  namespace: api-gateway
  annotations:
    kubernetes.io/ingress.class: "nginx"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/rate-limit: "1000"
spec:
  tls:
  - hosts:
    - api.exalt.com
    secretName: api-gateway-tls
  rules:
  - host: api.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: api-gateway
            port:
              number: 80
```

### 3. Deploy to Kubernetes

```bash
# Apply all configurations
kubectl apply -f k8s/ -n api-gateway

# Check deployment status
kubectl get pods -l app=api-gateway -n api-gateway

# View logs
kubectl logs -f deployment/api-gateway -n api-gateway

# Check service endpoints
kubectl get services -n api-gateway

# Test external access
kubectl port-forward service/api-gateway 8080:80 -n api-gateway
```

### 4. Helm Deployment

```bash
# Add Helm repository
helm repo add exalt https://charts.exalt.com
helm repo update

# Install with custom values
helm install api-gateway exalt/api-gateway \
  --namespace api-gateway \
  --create-namespace \
  --set image.tag=latest \
  --set replicaCount=3 \
  --set ingress.enabled=true \
  --set ingress.hostname=api.exalt.com \
  --set ssl.enabled=true

# Upgrade deployment
helm upgrade api-gateway exalt/api-gateway \
  --namespace api-gateway \
  --set image.tag=v1.1.0

# Check status
helm status api-gateway -n api-gateway
```

## Security Configuration

### 1. JWT Configuration

```yaml
# application-security.yml
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 3600
    issuer: "api-gateway.exalt.com"
    audience: "exalt-services"
    
  authentication:
    exclude-paths:
      - "/actuator/health"
      - "/api/v1/public/**"
      - "/api/v1/auth/login"
      - "/api/v1/auth/register"
    
  authorization:
    admin-paths:
      - "/api/v1/admin/**"
      - "/api/v1/analytics/admin/**"
    
    user-paths:
      - "/api/v1/orders/**"
      - "/api/v1/profile/**"
```

### 2. CORS Configuration

```yaml
cors:
  allowed-origins:
    - "https://app.exalt.com"
    - "https://admin.exalt.com"
    - "https://mobile.exalt.com"
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
  exposed-headers:
    - X-Total-Count
    - X-Rate-Limit-Remaining
  allow-credentials: true
  max-age: 3600
```

### 3. Rate Limiting Configuration

```yaml
rate-limiting:
  global:
    requests-per-second: 1000
    burst-capacity: 2000
    
  per-user:
    requests-per-second: 100
    burst-capacity: 200
    
  per-ip:
    requests-per-second: 50
    burst-capacity: 100
    
  api-specific:
    "/api/v1/auth/**":
      requests-per-second: 10
      burst-capacity: 20
    "/api/v1/analytics/**":
      requests-per-second: 500
      burst-capacity: 1000
```

## SSL/TLS Setup

### 1. Generate SSL Certificates

```bash
# For development - self-signed certificate
openssl req -x509 -newkey rsa:2048 -keyout api-gateway.key -out api-gateway.crt -days 365 -nodes \
  -subj "/CN=localhost/O=Exalt/C=US"

# Create PKCS12 keystore
openssl pkcs12 -export -in api-gateway.crt -inkey api-gateway.key -out api-gateway.p12 -name api-gateway

# For production - use Let's Encrypt or CA-signed certificates
```

### 2. SSL Configuration

```yaml
# application-ssl.yml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:api-gateway.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: api-gateway
    trust-store: classpath:truststore.p12
    trust-store-password: ${SSL_TRUSTSTORE_PASSWORD}
    protocol: TLS
    enabled-protocols: TLSv1.2,TLSv1.3
    ciphers: 
      - TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384
      - TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
      - TLS_RSA_WITH_AES_256_GCM_SHA384
      - TLS_RSA_WITH_AES_128_GCM_SHA256

  # Redirect HTTP to HTTPS
  port: 8080
  forward-headers-strategy: native
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
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }
}
```

## Service Discovery Setup

### 1. Eureka Client Configuration

```yaml
# eureka configuration
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
    register-with-eureka: true
    fetch-registry: true
    registry-fetch-interval-seconds: 30
    
  instance:
    hostname: ${EUREKA_INSTANCE_HOSTNAME:localhost}
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    metadata-map:
      version: ${spring.application.version:1.0.0}
      git-version: ${git.commit.id.abbrev:unknown}
```

### 2. Service Registration Health Check

```bash
# Check if API Gateway is registered
curl http://localhost:8761/eureka/apps/API-GATEWAY | jq '.application.instance[0].status'

# Check discovered services
curl http://localhost:8080/actuator/gateway/routes | jq '.[] | {id: .route_id, uri: .uri}'

# Test service discovery
curl http://localhost:8080/api/v1/social-commerce/health
```

## Verification

### 1. Health Checks

```bash
# Check gateway health
curl http://localhost:8080/actuator/health

# Check specific health indicators
curl http://localhost:8080/actuator/health/diskSpace
curl http://localhost:8080/actuator/health/eureka

# Check readiness and liveness
curl http://localhost:8080/actuator/health/readiness
curl http://localhost:8080/actuator/health/liveness
```

### 2. Authentication Testing

```bash
# Test public endpoint (should work without auth)
curl http://localhost:8080/api/v1/public/health

# Test protected endpoint (should return 401)
curl http://localhost:8080/api/v1/orders

# Test with JWT token
JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
curl -H "Authorization: Bearer $JWT_TOKEN" http://localhost:8080/api/v1/orders
```

### 3. Rate Limiting Testing

```bash
# Test rate limiting
for i in {1..110}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/test
done

# Expected: First 100 requests return 200, subsequent requests return 429
```

### 4. Circuit Breaker Testing

```bash
# Simulate service failure
curl -X POST http://localhost:8080/actuator/gateway/circuit-breaker/social-commerce/force-open

# Test circuit breaker state
curl http://localhost:8080/actuator/gateway/circuit-breaker/social-commerce/state
```

## Troubleshooting

### Common Issues

#### 1. Gateway Not Registering with Eureka

**Problem**: Gateway not appearing in Eureka dashboard

**Solutions:**
```bash
# Check Eureka server connectivity
curl http://localhost:8761/eureka/apps

# Verify Eureka configuration
curl http://localhost:8080/actuator/configprops | grep eureka

# Check network connectivity
telnet eureka-server 8761
```

#### 2. JWT Authentication Failures

**Problem**: 401 errors despite valid tokens

**Solutions:**
```bash
# Verify JWT secret configuration
echo $JWT_SECRET | base64 -d | wc -c  # Should be >= 32 bytes

# Check token expiration
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/debug/token

# Verify token signature
# Use online JWT debugger or jwt-cli tool
```

#### 3. SSL/TLS Issues

**Problem**: SSL handshake failures

**Solutions:**
```bash
# Test SSL connectivity
openssl s_client -connect localhost:8443 -servername localhost

# Check certificate validity
openssl x509 -in api-gateway.crt -text -noout

# Verify keystore
keytool -list -v -keystore api-gateway.p12 -storetype PKCS12
```

#### 4. High Memory Usage

**Problem**: OutOfMemoryError or high memory consumption

**Solutions:**
```bash
# Adjust JVM heap size
export JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"

# Monitor memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# Generate heap dump for analysis
jcmd $(pgrep -f api-gateway) GC.run_finalization
jcmd $(pgrep -f api-gateway) VM.dump_heap /tmp/gateway-heap.hprof
```

### Performance Tuning

```bash
# Optimize for high throughput
export JAVA_OPTS="
  -Xmx4g 
  -Xms4g 
  -XX:+UseG1GC 
  -XX:MaxGCPauseMillis=200 
  -XX:+UseStringDeduplication
  -Dserver.tomcat.max-threads=400
  -Dserver.tomcat.max-connections=8192
"

# Monitor performance
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/metrics/gateway.requests
```

## Next Steps

1. **Configure Monitoring**: Set up Prometheus and Grafana dashboards
2. **Setup Load Balancer**: Configure external load balancer (AWS ALB, NGINX)
3. **Security Hardening**: Implement additional security measures
4. **Performance Testing**: Conduct load testing and optimization
5. **Blue-Green Deployment**: Set up zero-downtime deployment pipeline
6. **Disaster Recovery**: Configure backup and recovery procedures

## Support

- **Documentation**: `/docs`
- **API Reference**: `http://localhost:8080/swagger-ui.html`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Health**: `http://localhost:8080/actuator/health`
- **Issues**: GitHub Issues
- **Team Chat**: Slack #api-gateway
- **Email**: gateway-team@exalt.com