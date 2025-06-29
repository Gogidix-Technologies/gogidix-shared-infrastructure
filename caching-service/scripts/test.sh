#!/bin/bash

# Test script for Caching Service
# Runs comprehensive test suite with coverage reporting

set -e

echo "🧪 Testing Caching Service..."
echo "============================="

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
COVERAGE_THRESHOLD=${COVERAGE_THRESHOLD:-80}
GENERATE_REPORT=${GENERATE_REPORT:-true}

echo -e "${YELLOW}📋 Test Configuration:${NC}"
echo "Coverage Threshold: $COVERAGE_THRESHOLD%"
echo "Generate Report: $GENERATE_REPORT"
echo ""

# Set up test environment
echo -e "${YELLOW}🔧 Setting up test environment...${NC}"
export SPRING_PROFILES_ACTIVE=test

# Start Redis for integration tests
echo -e "${YELLOW}🚀 Starting Redis for tests...${NC}"
docker run -d --name redis-test -p 6380:6379 redis:7-alpine > /dev/null 2>&1 || true

# Wait for Redis to be ready
echo -e "${YELLOW}⏳ Waiting for Redis to be ready...${NC}"
sleep 3

# Run unit tests
echo -e "${BLUE}🏃 Running unit tests...${NC}"
mvn test -Dtest="**/*Test.java"

# Run integration tests
echo -e "${BLUE}🏃 Running integration tests...${NC}"
mvn test -Dtest="**/*IntegrationTest.java" -Dspring.redis.port=6380

# Generate coverage report
if [ "$GENERATE_REPORT" = "true" ]; then
    echo -e "${BLUE}📊 Generating coverage report...${NC}"
    mvn jacoco:report
    
    # Check coverage
    echo -e "${YELLOW}📈 Coverage Analysis:${NC}"
    if [ -f "target/site/jacoco/index.html" ]; then
        echo -e "${GREEN}✅ Coverage report generated: target/site/jacoco/index.html${NC}"
        
        # Extract coverage percentage (simplified)
        COVERAGE=$(grep -o 'Total[^%]*%' target/site/jacoco/index.html | grep -o '[0-9]*%' | head -1 | tr -d '%' || echo "0")
        
        echo "Current Coverage: $COVERAGE%"
        
        if [ "$COVERAGE" -ge "$COVERAGE_THRESHOLD" ]; then
            echo -e "${GREEN}✅ Coverage meets threshold ($COVERAGE% >= $COVERAGE_THRESHOLD%)${NC}"
        else
            echo -e "${RED}❌ Coverage below threshold ($COVERAGE% < $COVERAGE_THRESHOLD%)${NC}"
        fi
    fi
fi

# Run performance tests
echo -e "${BLUE}🏃 Running performance tests...${NC}"
mvn test -Dtest="**/*PerformanceTest.java" -Dspring.redis.port=6380

# Clean up test Redis
echo -e "${YELLOW}🧹 Cleaning up test environment...${NC}"
docker stop redis-test > /dev/null 2>&1 || true
docker rm redis-test > /dev/null 2>&1 || true

# Run static analysis
echo -e "${BLUE}🔍 Running static analysis...${NC}"
mvn spotbugs:check || echo -e "${YELLOW}⚠️  SpotBugs analysis completed with warnings${NC}"

# Summary
echo ""
echo -e "${GREEN}🎉 Test suite completed!${NC}"
echo ""

# Test results summary
UNIT_TESTS=$(grep -o "Tests run: [0-9]*" target/surefire-reports/*.txt 2>/dev/null | grep -o "[0-9]*" | head -1 || echo "0")
FAILURES=$(grep -o "Failures: [0-9]*" target/surefire-reports/*.txt 2>/dev/null | grep -o "[0-9]*" | head -1 || echo "0")
ERRORS=$(grep -o "Errors: [0-9]*" target/surefire-reports/*.txt 2>/dev/null | grep -o "[0-9]*" | head -1 || echo "0")

echo -e "${YELLOW}📊 Test Results Summary:${NC}"
echo "Tests Run: $UNIT_TESTS"
echo "Failures: $FAILURES"
echo "Errors: $ERRORS"

if [ "$FAILURES" -eq 0 ] && [ "$ERRORS" -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed${NC}"
    exit 1
fi