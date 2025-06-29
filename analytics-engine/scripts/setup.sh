#!/bin/bash
echo "🚀 Setting up service"
echo "Reference: microservices-architecture.md Section 5"

if command -v mvn &> /dev/null; then
    mvn clean install -DskipTests
elif command -v npm &> /dev/null; then
    npm install
fi

echo "✅ Setup complete!"
