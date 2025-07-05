# Setup Guide - Document Verification Service

## Overview

This document provides comprehensive setup instructions for the Document Verification Service, including local development environment, Docker setup, and cloud deployment with ML model configuration.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Setup](#docker-setup)
4. [Kubernetes Setup](#kubernetes-setup)
5. [Configuration](#configuration)
6. [ML Model Setup](#ml-model-setup)
7. [Verification](#verification)
8. [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

- **Operating System**: Windows 10/11, macOS 10.15+, Linux Ubuntu 18.04+
- **Memory**: Minimum 16GB RAM (32GB recommended for ML models)
- **Storage**: Minimum 50GB free space (for ML models and document storage)
- **GPU**: Optional NVIDIA GPU for ML acceleration
- **Network**: Stable internet connection for external verification APIs

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

#### AI/ML Dependencies

```bash
# Python (for ML models)
Python 3.8+
pip 21+

# OCR Engine
Tesseract 5.0+

# Image Processing
OpenCV 4.5+
```

#### Database and Storage

```bash
# Database
PostgreSQL 14+

# Cache
Redis 6.2+

# Message Broker
Apache Kafka 3.0+

# File Storage
AWS CLI (for S3 integration)
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

#### Tesseract OCR

**Windows:**
```powershell
# Using Chocolatey
choco install tesseract

# Or download from GitHub releases
# https://github.com/UB-Mannheim/tesseract/wiki
```

**macOS:**
```bash
# Using Homebrew
brew install tesseract
brew install tesseract-lang
```

**Linux:**
```bash
# Ubuntu/Debian
sudo apt install tesseract-ocr tesseract-ocr-all

# CentOS/RHEL
sudo yum install tesseract tesseract-langpack-eng
```

## Local Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/gogidix/social-ecommerce-ecosystem.git
cd social-ecommerce-ecosystem/shared-infrastructure/document-verification
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
SERVER_PORT=8405
SPRING_PROFILES_ACTIVE=dev

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=document_verification_db
DB_USER=doc_verify_user
DB_PASSWORD=secure_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Service Discovery
EUREKA_SERVER_URL=http://localhost:8761/eureka

# File Storage (AWS S3)
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
S3_BUCKET_NAME=document-verification-bucket

# OCR Configuration
TESSERACT_PATH=/usr/bin/tesseract
OCR_LANGUAGES=eng,fra,deu,spa,ara

# ML Models
MODEL_PATH=/opt/models/document-verification
FRAUD_DETECTION_MODEL=fraud_detector_v1.model
CLASSIFICATION_MODEL=doc_classifier_v1.model

# External Verification APIs
JUMIO_API_KEY=your-jumio-api-key
JUMIO_API_SECRET=your-jumio-secret
ONFIDO_API_KEY=your-onfido-api-key

# Security
JWT_SECRET=your-secret-key
ENCRYPTION_KEY=your-encryption-key

# Processing Configuration
MAX_DOCUMENT_SIZE=50MB
PROCESSING_TIMEOUT=300s
MAX_CONCURRENT_VERIFICATIONS=10
```

### 3. Infrastructure Setup

#### Option A: Docker Compose (Recommended)

```bash
# Start infrastructure services
docker-compose -f docker-compose.yml up -d postgres redis kafka s3-mock

# Verify services are running
docker-compose ps
```

#### Option B: Native Installation

**PostgreSQL:**
```bash
# Create database
sudo -u postgres createdb document_verification_db
sudo -u postgres psql -c "CREATE USER doc_verify_user WITH PASSWORD 'secure_password';"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE document_verification_db TO doc_verify_user;"
```

### 4. ML Model Setup

#### Download Pre-trained Models

```bash
# Create model directory
sudo mkdir -p /opt/models/document-verification
sudo chown -R $USER:$USER /opt/models/document-verification

# Download fraud detection model
curl -L "https://models.gogidix.com/fraud_detector_v1.model" \
  -o /opt/models/document-verification/fraud_detector_v1.model

# Download document classification model
curl -L "https://models.gogidix.com/doc_classifier_v1.model" \
  -o /opt/models/document-verification/doc_classifier_v1.model
```

#### Install Python Dependencies for ML

```bash
# Create Python virtual environment
python3 -m venv ml-env
source ml-env/bin/activate

# Install ML dependencies
pip install tensorflow==2.12.0
pip install opencv-python==4.7.1
pip install pillow==9.5.0
pip install numpy==1.24.3
```

### 5. Build and Run

```bash
# Install Java dependencies
mvn clean install

# Run database migrations
mvn flyway:migrate

# Start the service
mvn spring-boot:run
```

### 6. Initialize Document Processing

```bash
# Initialize document templates
curl -X POST http://localhost:8405/api/v1/admin/initialize \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{"loadTemplates": true, "warmupModels": true}'

# Test OCR functionality
curl -X POST http://localhost:8405/api/v1/admin/test-ocr \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Docker Setup

### 1. Build Docker Image

```bash
# Build the image
docker build -t document-verification:latest .

# Or use the build script
./scripts/build-docker.sh
```

### 2. Run with Docker Compose

```bash
# Start all services
docker-compose up -d

# Check logs
docker-compose logs -f document-verification

# Scale the service
docker-compose up -d --scale document-verification=3
```

### 3. Docker Configuration

```yaml
# docker-compose.yml
version: '3.8'
services:
  document-verification:
    image: document-verification:latest
    ports:
      - "8405:8405"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - REDIS_HOST=redis
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - AWS_ENDPOINT_URL=http://s3-mock:9000
    volumes:
      - ./models:/opt/models/document-verification
      - ./temp:/tmp/document-processing
    depends_on:
      - postgres
      - redis
      - kafka
      - s3-mock
```

## Kubernetes Setup

### 1. Prerequisites

```bash
# Create namespace
kubectl create namespace shared-infrastructure

# Create secrets
kubectl create secret generic document-verification-secrets \
  --from-literal=db-password=secure_password \
  --from-literal=jwt-secret=your-secret-key \
  --from-literal=aws-access-key=your-access-key \
  --from-literal=aws-secret-key=your-secret-key \
  --from-literal=jumio-api-key=your-jumio-key \
  -n shared-infrastructure

# Create configmap for models
kubectl create configmap ml-models \
  --from-file=/opt/models/document-verification/ \
  -n shared-infrastructure
```

### 2. Deploy with Kubernetes

```bash
# Apply configurations
kubectl apply -f k8s/ -n shared-infrastructure

# Check deployment status
kubectl get pods -l app=document-verification -n shared-infrastructure

# View logs
kubectl logs -f deployment/document-verification -n shared-infrastructure
```

### 3. Helm Deployment

```bash
# Add Helm repository
helm repo add gogidix https://charts.gogidix.com
helm repo update

# Install the chart
helm install document-verification gogidix/document-verification \
  --namespace shared-infrastructure \
  --set image.tag=latest \
  --set service.port=8405 \
  --set ml.enabled=true
```

## Configuration

### Application Configuration

```yaml
# application.yml
spring:
  application:
    name: document-verification
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:document_verification_db}
    username: ${DB_USER:doc_verify_user}
    password: ${DB_PASSWORD:secure_password}
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
  servlet:
    multipart:
      max-file-size: ${MAX_DOCUMENT_SIZE:50MB}
      max-request-size: ${MAX_DOCUMENT_SIZE:50MB}

server:
  port: ${SERVER_PORT:8405}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka}

# Document Processing Configuration
document:
  processing:
    max-concurrent: ${MAX_CONCURRENT_VERIFICATIONS:10}
    timeout: ${PROCESSING_TIMEOUT:300s}
    temp-directory: /tmp/document-processing
  
  ocr:
    engine: tesseract
    path: ${TESSERACT_PATH:/usr/bin/tesseract}
    languages: ${OCR_LANGUAGES:eng,fra,deu,spa,ara}
    confidence-threshold: 60
  
  ml:
    models-path: ${MODEL_PATH:/opt/models/document-verification}
    fraud-detection: ${FRAUD_DETECTION_MODEL:fraud_detector_v1.model}
    classification: ${CLASSIFICATION_MODEL:doc_classifier_v1.model}
    gpu-enabled: false

# External Services
external:
  jumio:
    api-key: ${JUMIO_API_KEY}
    api-secret: ${JUMIO_API_SECRET}
    base-url: https://api.jumio.com
  onfido:
    api-key: ${ONFIDO_API_KEY}
    base-url: https://api.onfido.com

# AWS Configuration
aws:
  access-key: ${AWS_ACCESS_KEY_ID}
  secret-key: ${AWS_SECRET_ACCESS_KEY}
  region: ${AWS_REGION:us-east-1}
  s3:
    bucket: ${S3_BUCKET_NAME:document-verification-bucket}
```

### Document Type Configuration

```yaml
# document-types.yml
document-types:
  identity:
    - type: PASSPORT
      countries: [US, CA, GB, DE, FR, ES, IT, NL, BE, SE, NO, DK]
      required-fields: [document_number, given_names, surname, date_of_birth, nationality]
      validation-rules:
        - field: document_number
          pattern: "^[A-Z0-9]{6,9}$"
    - type: DRIVERS_LICENSE
      countries: [US, CA, GB, DE, FR, ES, IT]
      required-fields: [license_number, given_names, surname, date_of_birth, address]
    - type: NATIONAL_ID
      countries: [DE, FR, ES, IT, NL, BE, SE, NO, DK]
      required-fields: [id_number, given_names, surname, date_of_birth]

  business:
    - type: BUSINESS_REGISTRATION
      required-fields: [company_name, registration_number, registration_date, address]
    - type: TAX_CERTIFICATE
      required-fields: [tax_number, company_name, issue_date, validity_date]
    - type: TRADE_LICENSE
      required-fields: [license_number, business_type, issue_date, expiry_date]
```

## ML Model Setup

### Model Training Environment

```bash
# Set up model training environment
python3 -m venv model-training
source model-training/bin/activate

# Install training dependencies
pip install tensorflow==2.12.0
pip install opencv-python==4.7.1
pip install scikit-learn==1.2.2
pip install pandas==2.0.2
pip install matplotlib==3.7.1
```

### Model Configuration

```python
# ml-config.py
ML_CONFIG = {
    'fraud_detection': {
        'model_type': 'cnn',
        'input_shape': (224, 224, 3),
        'confidence_threshold': 0.8,
        'batch_size': 32
    },
    'document_classification': {
        'model_type': 'resnet50',
        'num_classes': 15,
        'confidence_threshold': 0.7,
        'batch_size': 16
    },
    'preprocessing': {
        'resize_dimensions': (224, 224),
        'normalization': 'imagenet',
        'augmentation': True
    }
}
```

## Verification

### 1. Health Checks

```bash
# Check service health
curl http://localhost:8405/actuator/health

# Check ML model status
curl http://localhost:8405/api/v1/models/status

# Verify OCR functionality
curl http://localhost:8405/api/v1/ocr/test
```

### 2. Document Processing Testing

```bash
# Test document upload
curl -X POST http://localhost:8405/api/v1/documents/verify \
  -H "Authorization: Bearer $TOKEN" \
  -F "document=@test-passport.jpg" \
  -F "document_type=PASSPORT"

# Check verification status
curl http://localhost:8405/api/v1/documents/{document_id}/status \
  -H "Authorization: Bearer $TOKEN"

# Get verification results
curl http://localhost:8405/api/v1/documents/{document_id}/results \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Integration Testing

```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify -Pintegration-tests

# Run ML model tests
mvn test -Dtest=MLModelTest

# Run OCR tests
mvn test -Dtest=OcrEngineTest
```

## Troubleshooting

### Common Issues

#### 1. OCR Installation Issues

**Problem**: Tesseract not found or language packs missing

**Solutions**:
```bash
# Check Tesseract installation
tesseract --version

# Install missing language packs
sudo apt install tesseract-ocr-fra tesseract-ocr-deu

# Update TESSERACT_PATH
export TESSERACT_PATH=/usr/bin/tesseract
```

#### 2. ML Model Loading Issues

**Problem**: Models fail to load or produce errors

**Solutions**:
```bash
# Check model files
ls -la /opt/models/document-verification/

# Verify model format
python -c "import tensorflow as tf; print(tf.keras.models.load_model('fraud_detector_v1.model'))"

# Clear model cache
rm -rf /tmp/model-cache/*
```

#### 3. Document Processing Failures

**Problem**: Documents fail to process or extract data

**Solutions**:
```bash
# Check document format support
curl http://localhost:8405/api/v1/supported-formats

# Enable debug logging
export LOG_LEVEL=DEBUG

# Check processing logs
tail -f logs/document-processing.log
```

#### 4. Memory Issues

**Problem**: OutOfMemoryError during document processing

**Solutions**:
```bash
# Increase heap size
export JAVA_OPTS="-Xmx4g -Xms2g"

# Limit concurrent processing
export MAX_CONCURRENT_VERIFICATIONS=5

# Enable memory monitoring
export ENABLE_MEMORY_MONITORING=true
```

### Debug Mode

```bash
# Enable debug logging
export LOG_LEVEL=DEBUG
export DEBUG_OCR=true
export DEBUG_ML=true

# Or in application.yml
logging:
  level:
    com.gogidix.verification: DEBUG
    org.springframework.security: DEBUG
```

### Performance Tuning

```bash
# JVM tuning for ML workloads
export JAVA_OPTS="
  -Xmx4g 
  -Xms2g 
  -XX:+UseG1GC 
  -XX:MaxGCPauseMillis=200 
  -XX:+UseStringDeduplication
  -XX:+HeapDumpOnOutOfMemoryError 
"

# GPU acceleration (if available)
export CUDA_VISIBLE_DEVICES=0
export TF_ENABLE_GPU_MEMORY_GROWTH=true
```

## Next Steps

1. **Configure External APIs**: Set up Jumio and Onfido integrations
2. **Train Custom Models**: Develop domain-specific ML models
3. **Set Up Monitoring**: Configure Prometheus and Grafana
4. **Enable Security**: Configure encryption and access controls
5. **Deploy to Production**: Follow production deployment guide

## Support

- **Documentation**: `/docs`
- **API Reference**: `http://localhost:8405/swagger-ui.html`
- **Model Documentation**: `/docs/ml-models`
- **Issues**: GitHub Issues
- **Team Chat**: Slack #document-verification
- **Email**: verification-team@gogidix.com