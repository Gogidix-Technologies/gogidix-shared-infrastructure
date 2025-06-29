# Setup Guide - File Storage Service

## Overview

This document provides comprehensive setup instructions for the File Storage Service, including local development environment, Docker setup, cloud deployment, and storage backend configuration.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Setup](#kubernetes-setup)
5. [Storage Backend Configuration](#storage-backend-configuration)
6. [CDN Configuration](#cdn-configuration)
7. [Verification](#verification)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 8GB RAM (16GB recommended)
- **Storage**: Minimum 20GB free space
- **Network**: Stable internet connection for cloud storage APIs

### Required Software

#### Development Tools

```bash
# Java Development Kit
Java 17+ (OpenJDK or Oracle JDK)

# Build Tool
Maven 3.8+

# IDE (Recommended)
IntelliJ IDEA with Spring Boot plugins

# Version Control
Git 2.30+
```

#### Container and Orchestration

```bash
# Docker
Docker Desktop 4.0+ or Docker Engine 20.10+
Docker Compose 2.0+

# Kubernetes (for cloud deployment)
kubectl 1.24+
helm 3.8+
```

#### Image Processing Dependencies

```bash
# ImageMagick (for advanced image processing)
ImageMagick 7.0+

# FFmpeg (for video processing)
FFmpeg 4.4+
```

#### Database and Storage

```bash
# Database
PostgreSQL 14+

# Cache
Redis 6.2+

# Message Broker
Apache Kafka 3.0+

# Cloud CLI Tools
AWS CLI 2.0+
```

### Installation Instructions

#### Java 17

**Windows:**
```powershell
# Using Chocolatey
choco install openjdk17

# Or download from Oracle/OpenJDK website
```

**macOS:**
```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel
```

#### ImageMagick

**Windows:**
```powershell
# Using Chocolatey
choco install imagemagick

# Or download from official website
```

**macOS:**
```bash
# Using Homebrew
brew install imagemagick
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt install imagemagick imagemagick-dev

# CentOS/RHEL
sudo yum install ImageMagick ImageMagick-devel
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/exalt/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/file-storage-service
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
SERVER_PORT=8406
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=file_storage_db
DB_USER=file_storage_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# AWS S3 Configuration
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
S3_BUCKET_NAME=file-storage-bucket
S3_CDN_DISTRIBUTION_ID=your-cloudfront-distribution

# Storage Configuration
STORAGE_ROOT_PATH=/var/storage/files
TEMP_STORAGE_PATH=/tmp/file-processing
MAX_FILE_SIZE=100MB
ALLOWED_FILE_TYPES=jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,txt,zip

# Image Processing
IMAGE_MAX_WIDTH=2048
IMAGE_MAX_HEIGHT=2048
THUMBNAIL_SIZE=150
IMAGE_QUALITY=85

# Security
JWT_SECRET=your-secret-key
ENCRYPTION_KEY=your-encryption-key
VIRUS_SCAN_ENABLED=true

# Processing
MAX_CONCURRENT_UPLOADS=10
PROCESSING_TIMEOUT=300s
CLEANUP_INTERVAL=3600s
```

### 3. Infrastructure Setup

#### Option A: Docker Compose (Recommended)

```bash
# Start infrastructure services
docker-compose -f docker-compose.yml up -d postgres redis kafka localstack

# Verify services are running
docker-compose ps
```

#### Option B: Native Installation

**PostgreSQL:**
```bash
# Create database
sudo -u postgres createdb file_storage_db
sudo -u postgres psql -c "CREATE USER file_storage_user WITH PASSWORD 'secure_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE file_storage_db TO file_storage_user;"
```

**LocalStack (for S3 testing):**
```bash
# Install LocalStack
pip install localstack

# Start LocalStack
localstack start -d

# Create test bucket
aws --endpoint-url=http://localhost:4566 s3 mb s3://file-storage-bucket
```

### 4. Build and Run

```bash
# Install dependencies
mvn clean install

# Run database migrations
mvn flyway:migrate

# Start the service
mvn spring-boot:run
```

### 5. Initialize Storage Backends

```bash
# Initialize storage configuration
curl -X POST http://localhost:8406/api/v1/admin/storage/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "backends": ["s3", "local"],
    "defaultBackend": "s3",
    "createDirectories": true
  }'

# Test storage connectivity
curl -X POST http://localhost:8406/api/v1/admin/storage/test \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

### 6. Configure File Processing

```bash
# Set up image processing
curl -X POST http://localhost:8406/api/v1/admin/processing/configure \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "enableThumbnails": true,
    "enableCompression": true,
    "enableVirusScanning": true,
    "processingQueue": "file-processing"
  }'
```

## Docker Setup

### 1. Build Docker Image

```bash
# Build the image
docker build -t file-storage-service:latest .

# Or use the build script
./scripts/build-docker.sh
```

### 2. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f file-storage-service

# Scale the service
docker-compose up -d --scale file-storage-service=3
```

### 3. Docker Configuration

```yaml
# docker-compose.yml
version: '3.8'
services:
  file-storage-service:
    image: file-storage-service:latest
    ports:
      - "8406:8406"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - AWS_ENDPOINT_URL=http://localstack:4566
    volumes:
      - file-storage-data:/var/storage/files
      - file-temp-data:/tmp/file-processing
    depends_on:
      - postgres
      - redis
      - kafka
      - localstack

volumes:
  file-storage-data:
  file-temp-data:
```

## Kubernetes Setup

### 1. Prerequisites

```bash
# Create namespace
kubectl create namespace shared-infrastructure

# Create secrets
kubectl create secret generic file-storage-secrets \
  --from-literal=db-password=secure_password \
  --from-literal=jwt-secret=your-secret-key \
  --from-literal=aws-access-key=your-access-key \
  --from-literal=aws-secret-key=your-secret-key \
  --from-literal=encryption-key=your-encryption-key \
  -n shared-infrastructure

# Create persistent volumes
kubectl apply -f k8s/storage-pv.yaml -n shared-infrastructure
```

### 2. Deploy with Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/ -n shared-infrastructure

# Check deployment status
kubectl get pods -l app=file-storage-service -n shared-infrastructure

# View logs
kubectl logs -f deployment/file-storage-service -n shared-infrastructure
```

### 3. Helm Deployment

```bash
# Add Helm repository
helm repo add exalt https://charts.exalt.com
helm repo update

# Install the chart
helm install file-storage-service exalt/file-storage-service \
  --namespace shared-infrastructure \
  --set image.tag=latest \
  --set service.port=8406 \
  --set storage.backend=s3
```

## Storage Backend Configuration

### AWS S3 Configuration

#### 1. Create S3 Bucket

```bash
# Create bucket
aws s3 mb s3://your-file-storage-bucket

# Configure bucket policy
aws s3api put-bucket-policy \
  --bucket your-file-storage-bucket \
  --policy file://s3-bucket-policy.json

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket your-file-storage-bucket \
  --versioning-configuration Status=Enabled

# Configure lifecycle rules
aws s3api put-bucket-lifecycle-configuration \
  --bucket your-file-storage-bucket \
  --lifecycle-configuration file://s3-lifecycle.json
```

#### 2. S3 Bucket Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "FileStorageServiceAccess",
      "Effect": "Allow",
      "Principal": {
        "AWS": "arn:aws:iam::ACCOUNT-ID:user/file-storage-service"
      },
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::your-file-storage-bucket",
        "arn:aws:s3:::your-file-storage-bucket/*"
      ]
    }
  ]
}
```

#### 3. Application Configuration

```yaml
# application.yml
spring:
  cloud:
    aws:
      credentials:
        access-key: ${AWS_ACCESS_KEY_ID}
        secret-key: ${AWS_SECRET_ACCESS_KEY}
      region:
        static: ${AWS_REGION:us-east-1}
      s3:
        bucket: ${S3_BUCKET_NAME}
        path-style-access: false

storage:
  backends:
    s3:
      enabled: true
      bucket-name: ${S3_BUCKET_NAME}
      region: ${AWS_REGION:us-east-1}
      server-side-encryption: AES256
      storage-class: STANDARD
    local:
      enabled: true
      root-path: ${STORAGE_ROOT_PATH:/var/storage/files}
      temp-path: ${TEMP_STORAGE_PATH:/tmp/file-processing}
  
  default-backend: s3
  max-file-size: ${MAX_FILE_SIZE:100MB}
  allowed-types: ${ALLOWED_FILE_TYPES:jpg,jpeg,png,gif,pdf,doc,docx}
```

## CDN Configuration

### AWS CloudFront Setup

#### 1. Create CloudFront Distribution

```bash
# Create distribution configuration
cat > cloudfront-config.json << EOF
{
  "CallerReference": "file-storage-$(date +%s)",
  "Comment": "File Storage Service CDN",
  "DefaultCacheBehavior": {
    "TargetOriginId": "S3-file-storage-bucket",
    "ViewerProtocolPolicy": "redirect-to-https",
    "MinTTL": 0,
    "ForwardedValues": {
      "QueryString": false,
      "Cookies": {"Forward": "none"}
    }
  },
  "Origins": {
    "Quantity": 1,
    "Items": [
      {
        "Id": "S3-file-storage-bucket",
        "DomainName": "your-file-storage-bucket.s3.amazonaws.com",
        "S3OriginConfig": {
          "OriginAccessIdentity": ""
        }
      }
    ]
  },
  "Enabled": true
}
EOF

# Create distribution
aws cloudfront create-distribution --distribution-config file://cloudfront-config.json
```

#### 2. Configure CDN in Application

```yaml
cdn:
  enabled: true
  provider: cloudfront
  distribution-id: ${S3_CDN_DISTRIBUTION_ID}
  domain: ${CDN_DOMAIN:your-cdn-domain.cloudfront.net}
  cache-control:
    images: "public, max-age=31536000"
    documents: "public, max-age=86400"
    default: "public, max-age=3600"
```

## Verification

### 1. Health Checks

```bash
# Check service health
curl http://localhost:8406/actuator/health

# Check storage backend status
curl http://localhost:8406/api/v1/admin/storage/status

# Verify image processing
curl http://localhost:8406/api/v1/admin/processing/status
```

### 2. File Operations Testing

```bash
# Test file upload
curl -X POST http://localhost:8406/api/v1/files/upload \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@test-image.jpg" \
  -F "category=images"

# Test file download
curl -X GET http://localhost:8406/api/v1/files/{file-id}/download \
  -H "Authorization: Bearer $TOKEN" \
  -o downloaded-file.jpg

# Test thumbnail generation
curl -X GET http://localhost:8406/api/v1/files/{file-id}/thumbnail \
  -H "Authorization: Bearer $TOKEN" \
  -o thumbnail.jpg

# Get file metadata
curl -X GET http://localhost:8406/api/v1/files/{file-id}/metadata \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Integration Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify -Pintegration-tests

# Run storage backend tests
mvn test -Dtest=StorageBackendTest

# Run image processing tests
mvn test -Dtest=ImageProcessingTest
```

## Troubleshooting

### Common Issues

#### 1. S3 Connection Issues

**Problem**: Cannot connect to S3 or access denied errors

**Solutions**:
```bash
# Test AWS credentials
aws sts get-caller-identity

# Test S3 access
aws s3 ls s3://your-file-storage-bucket

# Check bucket policy
aws s3api get-bucket-policy --bucket your-file-storage-bucket
```

#### 2. Image Processing Failures

**Problem**: Image processing fails or produces errors

**Solutions**:
```bash
# Check ImageMagick installation
convert --version

# Test image processing manually
convert test-image.jpg -resize 150x150 test-thumbnail.jpg

# Check system resources
free -h
df -h
```

#### 3. File Upload Timeouts

**Problem**: Large file uploads timeout or fail

**Solutions**:
```bash
# Increase timeout settings
export PROCESSING_TIMEOUT=600s

# Check disk space
df -h /tmp

# Monitor upload progress
tail -f logs/file-storage.log | grep UPLOAD
```

#### 4. CDN Cache Issues

**Problem**: Files not updating in CDN or cache misses

**Solutions**:
```bash
# Invalidate CDN cache
aws cloudfront create-invalidation \
  --distribution-id YOUR_DISTRIBUTION_ID \
  --paths "/*"

# Check CDN status
curl -I https://your-cdn-domain.cloudfront.net/path/to/file
```

### Debug Mode

```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
export DEBUG_STORAGE=true
export DEBUG_PROCESSING=true

# Or in application.yml
logging:
  level:
    com.exalt.storage: DEBUG
    com.amazonaws: DEBUG
    org.springframework.web.multipart: DEBUG
```

### Performance Tuning

```bash
# JVM tuning for file processing
export JAVA_OPTS="
  -Xmx4g 
  -Xms2g 
  -XX:+UseG1GC 
  -XX:MaxGCPauseMillis=200 
  -XX:+HeapDumpOnOutOfMemoryError 
  -Djava.io.tmpdir=/tmp/file-processing
"

# Optimize for large files
export MAX_CONCURRENT_UPLOADS=5
export MULTIPART_THRESHOLD=50MB
```

## Next Steps

1. **Configure Cloud Storage**: Set up AWS S3 and CloudFront
2. **Set Up Monitoring**: Configure Prometheus and Grafana
3. **Enable Security**: Configure virus scanning and encryption
4. **Optimize Performance**: Configure CDN and caching
5. **Deploy to Production**: Follow production deployment guide

## Support

- **Documentation**: `/docs`
- **API Reference**: `http://localhost:8406/swagger-ui.html`
- **Issues**: GitHub Issues
- **Team Chat**: Slack #file-storage
- **Email**: storage-team@exalt.com