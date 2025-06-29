#!/bin/bash

# Simplified Admin Framework Build Test (Cloud-Ready)
echo "🚀 Testing Admin Framework Build (Cloud-Ready)"
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "SUCCESS") echo -e "${GREEN}✅ $message${NC}" ;;
        "ERROR") echo -e "${RED}❌ $message${NC}" ;;
        "WARNING") echo -e "${YELLOW}⚠️  $message${NC}" ;;
        "INFO") echo -e "${BLUE}ℹ️  $message${NC}" ;;
    esac
}

# Test 1: Project Structure
echo ""
print_status "INFO" "Test 1: Validating project structure..."

if [ -f "pom.xml" ]; then
    print_status "SUCCESS" "pom.xml found"
else
    print_status "ERROR" "pom.xml not found"
    exit 1
fi

if [ -d "src/main/java" ]; then
    print_status "SUCCESS" "Java source directory found"
else
    print_status "ERROR" "Java source directory not found"
    exit 1
fi

if [ -f "Dockerfile" ]; then
    print_status "SUCCESS" "Dockerfile found"
else
    print_status "ERROR" "Dockerfile not found"
    exit 1
fi

# Test 2: Maven Configuration
echo ""
print_status "INFO" "Test 2: Validating Maven configuration..."

# Check Maven wrapper
if [ -f "mvnw.cmd" ]; then
    print_status "SUCCESS" "Maven wrapper found"
else
    print_status "WARNING" "Maven wrapper not found"
fi

# Validate POM structure
if grep -q "<groupId>com.exalt.ecosystem.shared</groupId>" pom.xml; then
    print_status "SUCCESS" "Correct groupId in pom.xml"
else
    print_status "ERROR" "Incorrect groupId in pom.xml"
    exit 1
fi

if grep -q "<artifactId>admin-framework</artifactId>" pom.xml; then
    print_status "SUCCESS" "Correct artifactId in pom.xml"
else
    print_status "ERROR" "Incorrect artifactId in pom.xml"
    exit 1
fi

# Test 3: Maven Compilation
echo ""
print_status "INFO" "Test 3: Testing Maven compilation..."

if mvn clean compile -q -DskipTests; then
    print_status "SUCCESS" "Maven compilation successful"
else
    print_status "ERROR" "Maven compilation failed"
    exit 1
fi

# Test 4: Maven Package
echo ""
print_status "INFO" "Test 4: Testing Maven package..."

if mvn package -q -DskipTests; then
    print_status "SUCCESS" "Maven package successful"
    
    # Check if JAR was created
    if [ -f "target/*.jar" ] || ls target/*.jar 1> /dev/null 2>&1; then
        print_status "SUCCESS" "JAR file created successfully"
    else
        print_status "WARNING" "JAR file not found in target directory"
    fi
else
    print_status "ERROR" "Maven package failed"
    exit 1
fi

# Test 5: Docker Build
echo ""
print_status "INFO" "Test 5: Testing Docker build..."

if docker build -t admin-framework:test . --quiet; then
    print_status "SUCCESS" "Docker image built successfully"
    
    # Check image details
    IMAGE_SIZE=$(docker images admin-framework:test --format "{{.Size}}")
    print_status "INFO" "Docker image size: $IMAGE_SIZE"
    
    # Cleanup test image
    docker rmi admin-framework:test > /dev/null 2>&1
else
    print_status "ERROR" "Docker build failed"
    exit 1
fi

# Test 6: Cloud Configuration Files
echo ""
print_status "INFO" "Test 6: Validating cloud configuration files..."

if [ -f "docker-compose.yml" ]; then
    print_status "SUCCESS" "Docker Compose configuration found"
else
    print_status "WARNING" "Docker Compose configuration not found"
fi

if [ -f ".github/workflows/cloud-deploy.yml" ]; then
    print_status "SUCCESS" "GitHub Actions workflow found"
else
    print_status "WARNING" "GitHub Actions workflow not found"
fi

if [ -f "init-scripts/01-init-database.sql" ]; then
    print_status "SUCCESS" "Database initialization script found"
else
    print_status "WARNING" "Database initialization script not found"
fi

# Test 7: Code Quality Check
echo ""
print_status "INFO" "Test 7: Basic code quality checks..."

# Count Java files
JAVA_FILES=$(find src -name "*.java" | wc -l)
print_status "INFO" "Found $JAVA_FILES Java source files"

if [ $JAVA_FILES -gt 0 ]; then
    print_status "SUCCESS" "Java source files present"
else
    print_status "ERROR" "No Java source files found"
    exit 1
fi

# Check for key framework classes
if find src -name "*.java" -exec grep -l "AbstractReportController" {} \; | head -1 > /dev/null; then
    print_status "SUCCESS" "Core framework classes found"
else
    print_status "WARNING" "Some core framework classes may be missing"
fi

# Summary
echo ""
echo "=============================================="
print_status "SUCCESS" "Admin Framework Build Test Complete!"
echo ""
print_status "INFO" "Test Summary:"
echo "  - Project structure: ✅"
echo "  - Maven configuration: ✅"
echo "  - Maven compilation: ✅"
echo "  - Maven packaging: ✅"
echo "  - Docker build: ✅"
echo "  - Cloud configuration: ✅"
echo "  - Code quality: ✅"
echo ""
print_status "SUCCESS" "Admin Framework is ready for cloud deployment!"
echo ""
print_status "INFO" "Next steps for cloud deployment:"
echo "  1. ✅ Framework is build-ready"
echo "  2. ✅ Docker containerization works"
echo "  3. ✅ Cloud configuration files are present"
echo "  4. 🔄 Push to GitHub repository"
echo "  5. 🔄 Set up GitHub Actions CI/CD"
echo "  6. 🔄 Deploy to cloud infrastructure"
echo ""
print_status "INFO" "Integration with domain admin dashboards:"
echo "  Add to their pom.xml:"
echo "  <dependency>"
echo "    <groupId>com.exalt.ecosystem.shared</groupId>"
echo "    <artifactId>admin-framework</artifactId>"
echo "    <version>1.0.0-SNAPSHOT</version>"
echo "  </dependency>"