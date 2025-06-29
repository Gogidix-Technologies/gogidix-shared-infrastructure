# Payment Processing Service - Setup Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Configuration](#configuration)
4. [Database Setup](#database-setup)
5. [Docker Deployment](#docker-deployment)
6. [Kubernetes Deployment](#kubernetes-deployment)
7. [Payment Gateway Setup](#payment-gateway-setup)
8. [Security Configuration](#security-configuration)
9. [Testing](#testing)
10. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements
- **Java**: JDK 17 or higher
- **Maven**: 3.8.0 or higher
- **Docker**: 20.10 or higher
- **Kubernetes**: 1.24 or higher (for K8s deployment)
- **PostgreSQL**: 14.0 or higher
- **Redis**: 7.0 or higher

### Development Tools
```bash
# Verify Java installation
java -version

# Verify Maven installation
mvn -version

# Verify Docker installation
docker --version

# Verify Kubernetes installation (optional)
kubectl version --client
```

## Local Development Setup

### 1. Clone the Repository
```bash
git clone https://github.com/exalt/payment-processing-service.git
cd payment-processing-service
```

### 2. Install Dependencies
```bash
# Install Maven dependencies
mvn clean install -DskipTests

# Download dependencies only
mvn dependency:go-offline
```

### 3. Set Up Environment Variables
Create a `.env` file in the project root:

```bash
# Application Configuration
SERVER_PORT=8092
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=payment_db
DB_USERNAME=payment_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DATABASE=11
REDIS_PASSWORD=redis_password

# Security Configuration
JWT_SECRET=your-256-bit-secret-key-for-jwt-tokens
ENCRYPTION_KEY=your-256-bit-encryption-key-for-sensitive-data

# Stripe Configuration
STRIPE_API_KEY=sk_test_your_stripe_api_key
STRIPE_PUBLISHABLE_KEY=pk_test_your_stripe_publishable_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# PayPal Configuration
PAYPAL_MODE=sandbox
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_client_secret

# Service Discovery (Optional for local dev)
EUREKA_SERVER_URL=http://localhost:8761/eureka
EUREKA_CLIENT_ENABLED=false

# Exchange Rate API
EXCHANGE_RATE_API_KEY=your_exchange_rate_api_key
EXCHANGE_RATE_API_URL=https://api.exchangerate-api.com/v4/latest/
```

### 4. Run Local Services
```bash
# Start PostgreSQL using Docker
docker run -d \
  --name payment-postgres \
  -e POSTGRES_DB=payment_db \
  -e POSTGRES_USER=payment_user \
  -e POSTGRES_PASSWORD=secure_password \
  -p 5432:5432 \
  postgres:14-alpine

# Start Redis using Docker
docker run -d \
  --name payment-redis \
  -p 6379:6379 \
  redis:7-alpine redis-server --requirepass redis_password
```

### 5. Run the Application
```bash
# Using Maven
mvn spring-boot:run

# Or using Java directly
java -jar target/payment-processing-service-1.0.0.jar

# With specific profile
java -jar target/payment-processing-service-1.0.0.jar --spring.profiles.active=dev
```

## Configuration

### Application Configuration Files

#### application.yml
```yaml
server:
  port: ${SERVER_PORT:8092}
  servlet:
    context-path: /api/v1

spring:
  application:
    name: payment-processing-service
  
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        show_sql: false
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true
  
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    database: ${REDIS_DATABASE}
    password: ${REDIS_PASSWORD}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:8080/auth/realms/exalt}

# Payment Gateway Configuration
payment:
  stripe:
    api-key: ${STRIPE_API_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
    capture-method: manual
    payment-method-types:
      - card
      - ach_debit
      - sepa_debit
  
  paypal:
    mode: ${PAYPAL_MODE:sandbox}
    client-id: ${PAYPAL_CLIENT_ID}
    client-secret: ${PAYPAL_CLIENT_SECRET}
    webhook-id: ${PAYPAL_WEBHOOK_ID}
  
  fraud:
    enabled: true
    threshold: 75
    rules:
      - high-risk-country
      - velocity-check
      - amount-threshold
      - device-fingerprint

# Service Discovery
eureka:
  client:
    enabled: ${EUREKA_CLIENT_ENABLED:true}
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90

# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  health:
    redis:
      enabled: true
    db:
      enabled: true

# Logging Configuration
logging:
  level:
    root: INFO
    com.exalt.ecosystem.shared.paymentprocessing: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/payment-processing.log
    max-size: 10MB
    max-history: 30
```

#### application-prod.yml
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        show_sql: false
  
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10

logging:
  level:
    root: WARN
    com.exalt.ecosystem.shared.paymentprocessing: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN

payment:
  stripe:
    capture-method: automatic
  
  fraud:
    threshold: 50
    ml-enabled: true
```

## Database Setup

### 1. Create Database Schema
```sql
-- Create database
CREATE DATABASE payment_db;

-- Create user
CREATE USER payment_user WITH ENCRYPTED PASSWORD 'secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_user;

-- Connect to payment_db
\c payment_db;

-- Create schema
CREATE SCHEMA IF NOT EXISTS payment;

-- Grant schema privileges
GRANT ALL ON SCHEMA payment TO payment_user;
```

### 2. Run Database Migrations
```bash
# Using Flyway
mvn flyway:migrate

# Or using Liquibase
mvn liquibase:update
```

### 3. Initial Database Schema
```sql
-- payments table
CREATE TABLE payment.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    external_id VARCHAR(255) UNIQUE NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    gateway_transaction_id VARCHAR(255),
    payment_method_id UUID,
    customer_id UUID NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_id (customer_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- payment_methods table
CREATE TABLE payment.payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    gateway VARCHAR(50) NOT NULL,
    token VARCHAR(255) NOT NULL,
    last_four VARCHAR(4),
    brand VARCHAR(50),
    exp_month INTEGER,
    exp_year INTEGER,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_payment_methods (customer_id)
);

-- transactions table
CREATE TABLE payment.transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL REFERENCES payment.payments(id),
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    gateway_response JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payment_transactions (payment_id)
);

-- Additional tables for refunds, disputes, webhooks, etc.
```

## Docker Deployment

### 1. Build Docker Image
```bash
# Build using Dockerfile
docker build -t exalt/payment-processing-service:latest .

# Or using Maven
mvn clean package docker:build
```

### 2. Docker Compose Setup
Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  payment-service:
    image: exalt/payment-processing-service:latest
    container_name: payment-processing-service
    ports:
      - "8092:8092"
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=payment_db
      - DB_USERNAME=payment_user
      - DB_PASSWORD=secure_password
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - REDIS_PASSWORD=redis_password
    depends_on:
      - postgres
      - redis
    networks:
      - payment-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8092/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  postgres:
    image: postgres:14-alpine
    container_name: payment-postgres
    environment:
      - POSTGRES_DB=payment_db
      - POSTGRES_USER=payment_user
      - POSTGRES_PASSWORD=secure_password
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - payment-network

  redis:
    image: redis:7-alpine
    container_name: payment-redis
    command: redis-server --requirepass redis_password
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - payment-network

volumes:
  postgres-data:
  redis-data:

networks:
  payment-network:
    driver: bridge
```

### 3. Run with Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f payment-service

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Kubernetes Deployment

### 1. Create Namespace
```bash
kubectl create namespace exalt-shared
```

### 2. Create Secrets
```bash
# Create payment secrets
kubectl create secret generic payment-secrets \
  --from-literal=db-username=payment_user \
  --from-literal=db-password=secure_password \
  --from-literal=jwt-secret=your-jwt-secret \
  --from-literal=encryption-key=your-encryption-key \
  --from-literal=stripe-secret-key=sk_live_your_key \
  --from-literal=stripe-webhook-secret=whsec_your_secret \
  --from-literal=paypal-client-id=your_client_id \
  --from-literal=paypal-client-secret=your_client_secret \
  --from-literal=exchange-rate-api-key=your_api_key \
  -n exalt-shared
```

### 3. Deploy ConfigMap
Create `configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: payment-service-config
  namespace: exalt-shared
data:
  application.yml: |
    server:
      port: 8092
    spring:
      profiles:
        active: kubernetes
    # Additional configuration...
```

### 4. Deploy Application
```bash
# Apply deployment
kubectl apply -f k8s/deployment.yaml

# Apply service
kubectl apply -f k8s/service.yaml

# Apply HPA
kubectl apply -f k8s/hpa.yaml

# Verify deployment
kubectl get pods -n exalt-shared
kubectl logs -f deployment/payment-processing-service -n exalt-shared
```

### 5. Ingress Configuration
Create `ingress.yaml`:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: payment-service-ingress
  namespace: exalt-shared
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
spec:
  tls:
  - hosts:
    - payments.exalt.com
    secretName: payment-service-tls
  rules:
  - host: payments.exalt.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: payment-processing-service
            port:
              number: 8092
```

## Payment Gateway Setup

### Stripe Configuration

#### 1. Create Stripe Account
1. Sign up at https://dashboard.stripe.com
2. Get API keys from Dashboard > Developers > API keys
3. Configure webhook endpoint

#### 2. Webhook Setup
```bash
# Using Stripe CLI for local testing
stripe listen --forward-to localhost:8092/api/v1/webhooks/stripe

# Configure production webhook
# Endpoint: https://payments.exalt.com/api/v1/webhooks/stripe
# Events to listen:
# - payment_intent.succeeded
# - payment_intent.payment_failed
# - charge.refunded
# - charge.dispute.created
# - customer.subscription.created
# - customer.subscription.updated
# - customer.subscription.deleted
```

#### 3. Stripe SDK Configuration
```java
@Configuration
public class StripeConfig {
    @Value("${payment.stripe.api-key}")
    private String apiKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
        Stripe.setAppInfo(
            "ExaltPaymentService",
            "1.0.0",
            "https://github.com/exalt/payment-service"
        );
    }
}
```

### PayPal Configuration

#### 1. Create PayPal Developer Account
1. Sign up at https://developer.paypal.com
2. Create sandbox accounts for testing
3. Get client ID and secret from My Apps & Credentials

#### 2. Configure Webhooks
```bash
# Webhook URL: https://payments.exalt.com/api/v1/webhooks/paypal
# Events to subscribe:
# - PAYMENT.CAPTURE.COMPLETED
# - PAYMENT.CAPTURE.DENIED
# - PAYMENT.CAPTURE.REFUNDED
# - BILLING.SUBSCRIPTION.CREATED
# - BILLING.SUBSCRIPTION.CANCELLED
```

#### 3. PayPal SDK Configuration
```java
@Configuration
public class PayPalConfig {
    @Value("${payment.paypal.client-id}")
    private String clientId;
    
    @Value("${payment.paypal.client-secret}")
    private String clientSecret;
    
    @Value("${payment.paypal.mode}")
    private String mode;
    
    @Bean
    public PayPalHttpClient payPalClient() {
        PayPalEnvironment environment = mode.equals("live") 
            ? new PayPalEnvironment.Live(clientId, clientSecret)
            : new PayPalEnvironment.Sandbox(clientId, clientSecret);
        
        return new PayPalHttpClient(environment);
    }
}
```

## Security Configuration

### 1. SSL/TLS Configuration
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: payment-service
    enabled-protocols: TLSv1.2,TLSv1.3
```

### 2. API Security
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/webhooks/**").permitAll()
                .requestMatchers("/api/v1/payments/**").authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt()
            .and()
            .build();
    }
}
```

### 3. Encryption Configuration
```java
@Configuration
public class EncryptionConfig {
    
    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey;
    
    @Bean
    public AESEncryptor aesEncryptor() {
        return new AESEncryptor(encryptionKey);
    }
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## Testing

### 1. Unit Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=PaymentServiceTest

# Run with coverage
mvn test jacoco:report
```

### 2. Integration Tests
```bash
# Run integration tests
mvn verify -Pintegration-tests

# Run with test containers
mvn test -Dspring.profiles.active=test
```

### 3. Load Testing
```bash
# Using Apache JMeter
jmeter -n -t payment-load-test.jmx -l results.csv

# Using K6
k6 run payment-load-test.js
```

### 4. Test Payment Cards

#### Stripe Test Cards
```
Success: 4242 4242 4242 4242
Decline: 4000 0000 0000 0002
3D Secure: 4000 0000 0000 3220
```

#### PayPal Sandbox Accounts
```
Buyer: sb-buyer@exalt.com
Seller: sb-seller@exalt.com
```

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check PostgreSQL status
docker ps | grep postgres

# Test connection
psql -h localhost -U payment_user -d payment_db

# Check logs
docker logs payment-postgres
```

#### 2. Redis Connection Issues
```bash
# Test Redis connection
redis-cli -h localhost -p 6379 -a redis_password ping

# Check Redis logs
docker logs payment-redis
```

#### 3. Payment Gateway Issues
```bash
# Check Stripe webhook signatures
curl -X POST http://localhost:8092/api/v1/webhooks/stripe \
  -H "Stripe-Signature: t=timestamp,v1=signature" \
  -d '{}'

# Verify API keys
curl https://api.stripe.com/v1/charges \
  -u sk_test_your_key:
```

#### 4. Service Discovery Issues
```bash
# Check Eureka registration
curl http://localhost:8761/eureka/apps/PAYMENT-PROCESSING-SERVICE

# Force re-registration
curl -X POST http://localhost:8092/actuator/refresh
```

### Debugging Tips

#### 1. Enable Debug Logging
```yaml
logging:
  level:
    com.exalt.ecosystem.shared.paymentprocessing: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type: TRACE
```

#### 2. Remote Debugging
```bash
# Start with debug port
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
     -jar payment-processing-service.jar
```

#### 3. Health Check Endpoints
```bash
# Overall health
curl http://localhost:8092/actuator/health

# Detailed health
curl http://localhost:8092/actuator/health/details

# Readiness probe
curl http://localhost:8092/actuator/health/readiness

# Liveness probe
curl http://localhost:8092/actuator/health/liveness
```

### Performance Tuning

#### 1. JVM Options
```bash
JAVA_OPTS="-Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof"
```

#### 2. Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      validation-timeout: 5000
      leak-detection-threshold: 60000
```

#### 3. Redis Performance
```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 16
        max-idle: 8
        min-idle: 4
        max-wait: -1ms
```

## Maintenance

### Regular Tasks
1. **Certificate Renewal**: Update SSL certificates before expiration
2. **Dependency Updates**: Regular security patches and updates
3. **Database Maintenance**: Vacuum, analyze, and reindex
4. **Log Rotation**: Configure logrotate for application logs
5. **Backup Verification**: Test restore procedures monthly

### Monitoring Checklist
- [ ] API response times < 200ms
- [ ] Database connection pool usage < 80%
- [ ] Redis memory usage < 75%
- [ ] Error rate < 0.1%
- [ ] Payment success rate > 95%
- [ ] Webhook processing lag < 5 seconds