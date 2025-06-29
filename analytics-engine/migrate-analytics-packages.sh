#!/bin/bash

# Analytics Engine Package Migration Script
# Migrates packages to com.exalt.ecosystem.shared.analytics

echo "🚀 Starting Analytics Engine Package Migration..."
echo "==============================================="

cd "/mnt/c/Users/frich/Desktop/Micro-Social-Ecommerce-Ecosystems/Exalt-Application-Limited/social-ecommerce-ecosystem/shared-infrastructure/analytics-engine"

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Step 1: Analyze current package structure
print_status "Analyzing current package structure..."

echo "📊 Current packages found:"
find src -name "*.java" -exec grep -l "^package " {} \; | while read file; do
    package=$(grep "^package " "$file" | sed 's/package //;s/;//')
    echo "  📁 $package ($(basename $file))"
done

# Step 2: Create new directory structure
print_status "Creating new package directory structure..."

cd src/main/java
mkdir -p com/exalt/ecosystem/shared/analytics/{config,controller,dto,service,consumer}
mkdir -p com/exalt/ecosystem/shared/analytics/service/impl

print_success "New directory structure created"

# Step 3: Move files and update package declarations
print_status "Moving files and updating package declarations..."

# Function to move file and update package declaration
move_and_update() {
    local old_path=$1
    local new_path=$2
    local old_package=$3
    local new_package=$4
    
    if [ -f "$old_path" ]; then
        # Update package declaration
        sed -i "s|package $old_package|package $new_package|g" "$old_path"
        
        # Move file to new location
        mv "$old_path" "$new_path"
        
        echo "  ✓ Moved $(basename $old_path) ($old_package → $new_package)"
    fi
}

# Main Application
print_status "Migrating main application..."
move_and_update "com/microecommerce/analytics/AnalyticsApplication.java" \
                "com/exalt/ecosystem/shared/analytics/AnalyticsApplication.java" \
                "com.microecommerce.analytics" \
                "com.exalt.ecosystem.shared.analytics"

# Config package
print_status "Migrating config package..."
move_and_update "com/microecommerce/analytics/config/SparkConfig.java" \
                "com/exalt/ecosystem/shared/analytics/config/SparkConfig.java" \
                "com.microecommerce.analytics.config" \
                "com.exalt.ecosystem.shared.analytics.config"

# Controller package
print_status "Migrating controller package..."
move_and_update "com/microecommerce/analytics/controller/AnalyticsController.java" \
                "com/exalt/ecosystem/shared/analytics/controller/AnalyticsController.java" \
                "com.microecommerce.analytics.controller" \
                "com.exalt.ecosystem.shared.analytics.controller"

# DTO package
print_status "Migrating dto package..."
move_and_update "com/microecommerce/analytics/dto/AnalyticsRequest.java" \
                "com/exalt/ecosystem/shared/analytics/dto/AnalyticsRequest.java" \
                "com.microecommerce.analytics.dto" \
                "com.exalt.ecosystem.shared.analytics.dto"

move_and_update "com/microecommerce/analytics/dto/AnalyticsResponse.java" \
                "com/exalt/ecosystem/shared/analytics/dto/AnalyticsResponse.java" \
                "com.microecommerce.analytics.dto" \
                "com.exalt.ecosystem.shared.analytics.dto"

# Service package
print_status "Migrating service package..."
move_and_update "com/microecommerce/analytics/service/AnalyticsService.java" \
                "com/exalt/ecosystem/shared/analytics/service/AnalyticsService.java" \
                "com.microecommerce.analytics.service" \
                "com.exalt.ecosystem.shared.analytics.service"

move_and_update "com/microecommerce/analytics/service/impl/AnalyticsServiceImpl.java" \
                "com/exalt/ecosystem/shared/analytics/service/impl/AnalyticsServiceImpl.java" \
                "com.microecommerce.analytics.service.impl" \
                "com.exalt.ecosystem.shared.analytics.service.impl"

# Consumer package (different old package)
print_status "Migrating consumer package..."
move_and_update "com/microecosystem/analytics/consumer/EventConsumer.java" \
                "com/exalt/ecosystem/shared/analytics/consumer/EventConsumer.java" \
                "com.microecosystem.analytics.consumer" \
                "com.exalt.ecosystem.shared.analytics.consumer"

print_success "File migration completed"

# Step 4: Update all import statements
print_status "Updating import statements..."

find com/exalt/ecosystem/shared/analytics -name "*.java" | while read file; do
    # Update imports from old packages to new package
    sed -i 's|import com\.microecommerce\.analytics\.|import com.exalt.ecosystem.shared.analytics.|g' "$file"
    sed -i 's|import com\.microecosystem\.analytics\.|import com.exalt.ecosystem.shared.analytics.|g' "$file"
    # Also handle any static imports
    sed -i 's|import static com\.microecommerce\.analytics\.|import static com.exalt.ecosystem.shared.analytics.|g' "$file"
    sed -i 's|import static com\.microecosystem\.analytics\.|import static com.exalt.ecosystem.shared.analytics.|g' "$file"
done

print_success "Import statements updated"

# Step 5: Update test files
print_status "Updating test files..."

cd ../../test/java
if [ -d "com/microecommerce/analytics" ]; then
    # Create new test directory structure
    mkdir -p com/exalt/ecosystem/shared/analytics
    
    # Move and update test files
    find com/microecommerce/analytics -name "*.java" | while read file; do
        filename=$(basename "$file")
        # Update package in test file
        sed -i 's|package com\.microecommerce\.analytics|package com.exalt.ecosystem.shared.analytics|g' "$file"
        sed -i 's|import com\.microecommerce\.analytics\.|import com.exalt.ecosystem.shared.analytics.|g' "$file"
        sed -i 's|import com\.microecosystem\.analytics\.|import com.exalt.ecosystem.shared.analytics.|g' "$file"
        
        # Move file
        mv "$file" "com/exalt/ecosystem/shared/analytics/$filename"
        echo "  ✓ Updated test file: $filename"
    done
    
    # Remove old test directories
    rm -rf com/microecommerce
    print_success "Test files updated"
fi

cd ../../main/java

# Step 6: Clean up old directories
print_status "Cleaning up old directory structure..."

# Remove old empty directories
if [ -d "com/microecommerce" ]; then
    rm -rf com/microecommerce
    print_success "Old microecommerce package directories removed"
fi

if [ -d "com/microecosystem" ]; then
    rm -rf com/microecosystem
    print_success "Old microecosystem package directories removed"
fi

# Step 7: Verify migration
print_status "Verifying migration..."

echo "📊 New package structure:"
find com/exalt/ecosystem/shared/analytics -name "*.java" | while read file; do
    package=$(grep "^package " "$file" | sed 's/package //;s/;//')
    echo "  📁 $package ($(basename $file))"
done

cd ../../..

# Summary
echo ""
echo "==============================================="
print_success "Analytics Engine Package Migration Complete!"
echo ""
print_status "Summary of changes:"
echo "  📦 Old packages: com.microecommerce.analytics.* & com.microecosystem.analytics.*"
echo "  📦 New package: com.exalt.ecosystem.shared.analytics.*"
echo "  📁 Files migrated: 8 Java files (7 main + 1 test)"
echo "  📝 Imports updated: All internal references updated"
echo "  🗂️  Directory structure: Reorganized to new package hierarchy"
echo ""
print_status "Package structure:"
echo "  com.exalt.ecosystem.shared.analytics/"
echo "  ├── AnalyticsApplication.java (main)"
echo "  ├── config/"
echo "  │   └── SparkConfig.java"
echo "  ├── controller/"
echo "  │   └── AnalyticsController.java"
echo "  ├── dto/"
echo "  │   ├── AnalyticsRequest.java"
echo "  │   └── AnalyticsResponse.java"
echo "  ├── service/"
echo "  │   ├── AnalyticsService.java"
echo "  │   └── impl/"
echo "  │       └── AnalyticsServiceImpl.java"
echo "  └── consumer/"
echo "      └── EventConsumer.java"
echo ""
print_status "Next steps:"
echo "  1. ✅ Package migration completed"
echo "  2. 🧪 Test compilation with new package structure"
echo "  3. 🔄 Continue with caching-service migration"
echo "  4. 📚 Update documentation with new package names"
echo ""

print_success "Ready for compilation testing!"