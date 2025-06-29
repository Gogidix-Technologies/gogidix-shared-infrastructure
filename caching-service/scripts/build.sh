#!/bin/bash

# Build script for Caching Service
# This script builds the Java application and Docker image

set -e

echo "🏗️  Building Caching Service..."
echo "=================================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Build configuration
SERVICE_NAME="caching-service"
VERSION=${VERSION:-"latest"}
SKIP_TESTS=${SKIP_TESTS:-false}

echo -e "${YELLOW}📋 Build Configuration:${NC}"
echo "Service: $SERVICE_NAME"
echo "Version: $VERSION"
echo "Skip Tests: $SKIP_TESTS"
echo ""

# Clean previous builds
echo -e "${YELLOW}🧹 Cleaning previous builds...${NC}"
mvn clean

# Run tests (unless skipped)
if [ "$SKIP_TESTS" = "false" ]; then
    echo -e "${YELLOW}🧪 Running tests...${NC}"
    mvn test
else
    echo -e "${YELLOW}⏭️  Skipping tests...${NC}"
fi

# Package the application
echo -e "${YELLOW}📦 Packaging application...${NC}"
if [ "$SKIP_TESTS" = "true" ]; then
    mvn package -DskipTests
else
    mvn package
fi

# Build Docker image
echo -e "${YELLOW}🐳 Building Docker image...${NC}"
docker build -t "exalt/$SERVICE_NAME:$VERSION" .
docker tag "exalt/$SERVICE_NAME:$VERSION" "exalt/$SERVICE_NAME:latest"

# Verify build
echo -e "${YELLOW}✅ Verifying build...${NC}"
if [ -f "target/$SERVICE_NAME-*.jar" ]; then
    echo -e "${GREEN}✅ JAR file created successfully${NC}"
else
    echo -e "${RED}❌ JAR file not found${NC}"
    exit 1
fi

if docker images "exalt/$SERVICE_NAME:$VERSION" | grep -q "$SERVICE_NAME"; then
    echo -e "${GREEN}✅ Docker image created successfully${NC}"
else
    echo -e "${RED}❌ Docker image not found${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Build completed successfully!${NC}"
echo -e "${GREEN}📦 JAR: target/$SERVICE_NAME-*.jar${NC}"
echo -e "${GREEN}🐳 Image: exalt/$SERVICE_NAME:$VERSION${NC}"
echo ""

# Show image details
echo -e "${YELLOW}📊 Docker Image Details:${NC}"
docker images "exalt/$SERVICE_NAME:$VERSION"

echo ""
echo -e "${YELLOW}🚀 Next steps:${NC}"
echo "1. Run locally: docker run -p 8403:8403 exalt/$SERVICE_NAME:$VERSION"
echo "2. Deploy to K8s: kubectl apply -f k8s/"
echo "3. Push to registry: docker push exalt/$SERVICE_NAME:$VERSION"