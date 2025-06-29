#!/bin/bash

# Package script for Caching Service
# Creates distribution-ready packages

set -e

echo "📦 Packaging Caching Service..."
echo "==============================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Package configuration
VERSION=${VERSION:-"1.0.0"}
PROFILE=${PROFILE:-"prod"}
INCLUDE_DOCS=${INCLUDE_DOCS:-true}

echo -e "${YELLOW}📋 Package Configuration:${NC}"
echo "Version: $VERSION"
echo "Profile: $PROFILE"
echo "Include Docs: $INCLUDE_DOCS"
echo ""

# Clean and prepare
echo -e "${YELLOW}🧹 Preparing for packaging...${NC}"
mvn clean

# Run tests
echo -e "${YELLOW}🧪 Running tests...${NC}"
mvn test

# Package application
echo -e "${YELLOW}📦 Creating application package...${NC}"
mvn package -Pproduction -DskipTests

# Create distribution directory
DIST_DIR="target/dist"
echo -e "${YELLOW}📁 Creating distribution directory: $DIST_DIR${NC}"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

# Copy JAR file
echo -e "${BLUE}📋 Copying application JAR...${NC}"
cp target/caching-service-*.jar "$DIST_DIR/caching-service-$VERSION.jar"

# Copy configuration files
echo -e "${BLUE}📋 Copying configuration files...${NC}"
mkdir -p "$DIST_DIR/config"
cp src/main/resources/application*.yml "$DIST_DIR/config/" 2>/dev/null || true
cp .env.template "$DIST_DIR/config/" 2>/dev/null || true

# Copy scripts
echo -e "${BLUE}📋 Copying scripts...${NC}"
mkdir -p "$DIST_DIR/scripts"
cp scripts/*.sh "$DIST_DIR/scripts/"
chmod +x "$DIST_DIR/scripts"/*.sh

# Copy Kubernetes manifests
echo -e "${BLUE}📋 Copying Kubernetes manifests...${NC}"
mkdir -p "$DIST_DIR/k8s"
cp k8s/*.yaml "$DIST_DIR/k8s/"

# Copy Docker files
echo -e "${BLUE}📋 Copying Docker files...${NC}"
cp Dockerfile "$DIST_DIR/"
cp docker-compose.yml "$DIST_DIR/" 2>/dev/null || true

# Copy documentation
if [ "$INCLUDE_DOCS" = "true" ]; then
    echo -e "${BLUE}📋 Copying documentation...${NC}"
    mkdir -p "$DIST_DIR/docs"
    cp README.md "$DIST_DIR/" 2>/dev/null || true
    cp -r docs/* "$DIST_DIR/docs/" 2>/dev/null || true
    cp -r api-docs "$DIST_DIR/" 2>/dev/null || true
fi

# Generate startup script
echo -e "${BLUE}📋 Generating startup script...${NC}"
cat > "$DIST_DIR/start.sh" << 'EOF'
#!/bin/bash

# Caching Service Startup Script
echo "🚀 Starting Caching Service..."

# Set default values
JAVA_OPTS=${JAVA_OPTS:-"-Xmx512m -Xms256m -XX:+UseG1GC"}
SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"prod"}

# Load environment if .env exists
if [ -f ".env" ]; then
    echo "📋 Loading environment from .env file..."
    export $(cat .env | grep -v '^#' | xargs)
fi

# Start the application
echo "🏃 Starting application with profile: $SPRING_PROFILES_ACTIVE"
java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -jar caching-service-*.jar

EOF

chmod +x "$DIST_DIR/start.sh"

# Generate stop script
echo -e "${BLUE}📋 Generating stop script...${NC}"
cat > "$DIST_DIR/stop.sh" << 'EOF'
#!/bin/bash

# Caching Service Stop Script
echo "🛑 Stopping Caching Service..."

# Find and kill the process
PID=$(ps aux | grep 'caching-service-.*\.jar' | grep -v grep | awk '{print $2}')

if [ -n "$PID" ]; then
    echo "🔍 Found process: $PID"
    kill -TERM $PID
    
    # Wait for graceful shutdown
    sleep 5
    
    # Force kill if still running
    if ps -p $PID > /dev/null; then
        echo "⚡ Force killing process: $PID"
        kill -KILL $PID
    fi
    
    echo "✅ Service stopped"
else
    echo "❌ Service not running"
fi

EOF

chmod +x "$DIST_DIR/stop.sh"

# Create archive
echo -e "${YELLOW}🗜️  Creating archive...${NC}"
cd target
tar -czf "caching-service-$VERSION.tar.gz" dist/
zip -r "caching-service-$VERSION.zip" dist/ > /dev/null
cd ..

# Generate checksum
echo -e "${YELLOW}🔐 Generating checksums...${NC}"
cd target
sha256sum "caching-service-$VERSION.tar.gz" > "caching-service-$VERSION.tar.gz.sha256"
sha256sum "caching-service-$VERSION.zip" > "caching-service-$VERSION.zip.sha256"
cd ..

# Summary
echo ""
echo -e "${GREEN}🎉 Packaging completed successfully!${NC}"
echo ""
echo -e "${YELLOW}📦 Generated Packages:${NC}"
echo "📁 Directory: target/dist/"
echo "🗜️  Archive: target/caching-service-$VERSION.tar.gz"
echo "🗜️  Archive: target/caching-service-$VERSION.zip"
echo "🔐 Checksums: target/caching-service-$VERSION.*.sha256"
echo ""

# Show directory contents
echo -e "${YELLOW}📋 Package Contents:${NC}"
find "$DIST_DIR" -type f | sort

echo ""
echo -e "${YELLOW}🚀 Next Steps:${NC}"
echo "1. Extract: tar -xzf target/caching-service-$VERSION.tar.gz"
echo "2. Configure: Edit config/.env"
echo "3. Deploy: ./start.sh"
echo "4. Monitor: Check logs and /actuator/health"