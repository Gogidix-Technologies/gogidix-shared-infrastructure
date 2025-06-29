#!/bin/bash

# Development utility script for admin-frameworks
# Reference: microservices-architecture.md Section 5

echo "🛠️  Development utilities for admin-frameworks"
echo "Reference: microservices-architecture.md Section 5"
echo "============================================"

function show_help() {
    echo "Usage: $0 {command}"
    echo ""
    echo "Available commands:"
    echo "  test         - Run all tests"
    echo "  build        - Build the service"
    echo "  run          - Run the service locally"
    echo "  docker       - Build and run Docker container"
    echo "  clean        - Clean build artifacts"
    echo "  help         - Show this help message"
}

case "$1" in
    "test")
        echo "🧪 Running tests..."
        mvn test
        ;;
    "build")
        echo "🔨 Building service..."
        mvn clean package
        ;;
    "run")
        echo "🚀 Starting admin-frameworks locally..."
        echo "🌐 Service will be available at: http://localhost:8080"
        mvn spring-boot:run -Dspring-boot.run.profiles=dev
        ;;
    "docker")
        echo "🐳 Building Docker image..."
        docker build -t admin-frameworks:dev .
        if [ $? -eq 0 ]; then
            echo "🚀 Running Docker container..."
            docker run -p 8080:8080 --name admin-frameworks-dev admin-frameworks:dev
        fi
        ;;
    "clean")
        echo "🧹 Cleaning build artifacts..."
        mvn clean
        docker rmi admin-frameworks:dev 2>/dev/null || true
        echo "✅ Clean complete"
        ;;
    "help"|"")
        show_help
        ;;
    *)
        echo "❌ Unknown command: $1"
        show_help
        exit 1
        ;;
esac