#!/bin/bash
echo "🛠️  Development utilities"
echo "Reference: microservices-architecture.md Section 5"

case "$1" in
    "test")
        if [ -f "pom.xml" ]; then
            mvn test
        elif [ -f "package.json" ]; then
            npm test
        fi
        ;;
    "build")
        if [ -f "pom.xml" ]; then
            mvn clean package
        elif [ -f "package.json" ]; then
            npm run build
        fi
        ;;
    "run")
        if [ -f "pom.xml" ]; then
            mvn spring-boot:run
        elif [ -f "package.json" ]; then
            npm start
        fi
        ;;
    *)
        echo "Usage: $0 {test|build|run}"
        ;;
esac
