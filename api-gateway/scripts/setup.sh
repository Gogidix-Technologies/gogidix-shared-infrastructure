#!/bin/bash

# Setup script for api-gateway
# Reference: microservices-architecture.md Section 5

echo "🚀 Setting up api-gateway"
echo "Reference: microservices-architecture.md Section 5"
echo "====================================="

# Check Java and Maven
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java version: $JAVA_VERSION"
else
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✅ Maven found: $MVN_VERSION"
else
    echo "❌ Maven not found. Please install Maven 3.8 or higher."
    exit 1
fi

# Install dependencies
echo "📦 Installing dependencies..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Dependencies installed successfully"
else
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo "✅ Setup complete for api-gateway!"
echo "📖 Next steps:"
echo "   1. Run tests: mvn test"
echo "   2. Start development: ./scripts/dev.sh"
echo "   3. Build Docker image: docker build -t api-gateway ."
