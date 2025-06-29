#!/bin/bash
# Setup script for local development environment

echo "🔧 Setting up $service development environment..."

# Check Java version
if ! java -version 2>&1 | grep -q "version \"17"; then
    echo "❌ Java 17 is required but not found"
    exit 1
fi

# Check Maven
if ! mvn --version > /dev/null 2>&1; then
    echo "❌ Maven is required but not found"
    exit 1
fi

# Install dependencies
echo "📦 Installing dependencies..."
mvn clean install -DskipTests

# Setup local database (if needed)
if grep -q "postgresql" pom.xml; then
    echo "🗄️ Note: PostgreSQL required for this service"
fi

echo "✅ Setup complete!"
