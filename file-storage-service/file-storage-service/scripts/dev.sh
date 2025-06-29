#!/bin/bash
# Development utility script

case "$1" in
    start)
        echo "🚀 Starting service in development mode..."
        mvn spring-boot:run -Dspring.profiles.active=dev
        ;;
    test)
        echo "🧪 Running tests..."
        mvn test
        ;;
    build)
        echo "🔨 Building service..."
        mvn clean package
        ;;
    docker)
        echo "🐳 Building Docker image..."
        docker build -t $service:dev .
        ;;
    *)
        echo "Usage: $0 {start|test|build|docker}"
        exit 1
        ;;
esac
