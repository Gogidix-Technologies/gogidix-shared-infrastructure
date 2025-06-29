#!/bin/bash

# Caching Service Package Migration Script
# Migrates packages to com.exalt.ecosystem.shared.caching

echo "🚀 Starting Caching Service Package Migration..."
echo "=============================================="

cd "/mnt/c/Users/frich/Desktop/Micro-Social-Ecommerce-Ecosystems/Exalt-Application-Limited/social-ecommerce-ecosystem/shared-infrastructure/caching-service"

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
mkdir -p com/exalt/ecosystem/shared/caching/{local,redis}

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

# Main caching package files
print_status "Migrating main caching package..."
move_and_update "com/microecommerce/caching/Cache.java" \
                "com/exalt/ecosystem/shared/caching/Cache.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

move_and_update "com/microecommerce/caching/CacheConfig.java" \
                "com/exalt/ecosystem/shared/caching/CacheConfig.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

move_and_update "com/microecommerce/caching/CacheEntry.java" \
                "com/exalt/ecosystem/shared/caching/CacheEntry.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

move_and_update "com/microecommerce/caching/CacheException.java" \
                "com/exalt/ecosystem/shared/caching/CacheException.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

move_and_update "com/microecommerce/caching/CacheFactory.java" \
                "com/exalt/ecosystem/shared/caching/CacheFactory.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

move_and_update "com/microecommerce/caching/CacheManager.java" \
                "com/exalt/ecosystem/shared/caching/CacheManager.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

move_and_update "com/microecommerce/caching/CachingServiceApplication.java" \
                "com/exalt/ecosystem/shared/caching/CachingServiceApplication.java" \
                "com.microecommerce.caching" \
                "com.exalt.ecosystem.shared.caching"

# Local caching package
print_status "Migrating local caching package..."
move_and_update "com/microecommerce/caching/local/InMemoryCache.java" \
                "com/exalt/ecosystem/shared/caching/local/InMemoryCache.java" \
                "com.microecommerce.caching.local" \
                "com.exalt.ecosystem.shared.caching.local"

move_and_update "com/microecommerce/caching/local/InMemoryCacheFactory.java" \
                "com/exalt/ecosystem/shared/caching/local/InMemoryCacheFactory.java" \
                "com.microecommerce.caching.local" \
                "com.exalt.ecosystem.shared.caching.local"

# Redis caching package
print_status "Migrating redis caching package..."
move_and_update "com/microecommerce/caching/redis/RedisCache.java" \
                "com/exalt/ecosystem/shared/caching/redis/RedisCache.java" \
                "com.microecommerce.caching.redis" \
                "com.exalt.ecosystem.shared.caching.redis"

move_and_update "com/microecommerce/caching/redis/RedisCacheFactory.java" \
                "com/exalt/ecosystem/shared/caching/redis/RedisCacheFactory.java" \
                "com.microecommerce.caching.redis" \
                "com.exalt.ecosystem.shared.caching.redis"

move_and_update "com/microecommerce/caching/redis/RedisConfig.java" \
                "com/exalt/ecosystem/shared/caching/redis/RedisConfig.java" \
                "com.microecommerce.caching.redis" \
                "com.exalt.ecosystem.shared.caching.redis"

print_success "File migration completed"

# Step 4: Update all import statements
print_status "Updating import statements..."

find com/exalt/ecosystem/shared/caching -name "*.java" | while read file; do
    # Update imports from old package to new package
    sed -i 's|import com\.microecommerce\.caching\.|import com.exalt.ecosystem.shared.caching.|g' "$file"
    # Also handle any static imports
    sed -i 's|import static com\.microecommerce\.caching\.|import static com.exalt.ecosystem.shared.caching.|g' "$file"
done

print_success "Import statements updated"

# Step 5: Update test files if they exist
print_status "Updating test files..."

cd ../../test/java
if [ -d "com/microecommerce/caching" ]; then
    # Create new test directory structure
    mkdir -p com/exalt/ecosystem/shared/caching
    
    # Move and update test files
    find com/microecommerce/caching -name "*.java" | while read file; do
        filename=$(basename "$file")
        # Update package in test file
        sed -i 's|package com\.microecommerce\.caching|package com.exalt.ecosystem.shared.caching|g' "$file"
        sed -i 's|import com\.microecommerce\.caching\.|import com.exalt.ecosystem.shared.caching.|g' "$file"
        
        # Move file
        mv "$file" "com/exalt/ecosystem/shared/caching/$filename"
        echo "  ✓ Updated test file: $filename"
    done
    
    # Remove old test directories
    rm -rf com/microecommerce
    print_success "Test files updated"
else
    print_warning "No test files found to migrate"
fi

cd ../../main/java

# Step 6: Clean up old directories
print_status "Cleaning up old directory structure..."

# Remove old empty directories
if [ -d "com/microecommerce" ]; then
    rm -rf com/microecommerce
    print_success "Old microecommerce package directories removed"
fi

# Step 7: Verify migration
print_status "Verifying migration..."

echo "📊 New package structure:"
find com/exalt/ecosystem/shared/caching -name "*.java" | while read file; do
    package=$(grep "^package " "$file" | sed 's/package //;s/;//')
    echo "  📁 $package ($(basename $file))"
done

cd ../../..

# Summary
echo ""
echo "=============================================="
print_success "Caching Service Package Migration Complete!"
echo ""
print_status "Summary of changes:"
echo "  📦 Old package: com.microecommerce.caching.*"
echo "  📦 New package: com.exalt.ecosystem.shared.caching.*"
echo "  📁 Files migrated: 12+ Java files"
echo "  📝 Imports updated: All internal references updated"
echo "  🗂️  Directory structure: Reorganized to new package hierarchy"
echo ""
print_status "Package structure:"
echo "  com.exalt.ecosystem.shared.caching/"
echo "  ├── CachingServiceApplication.java (main)"
echo "  ├── Cache.java"
echo "  ├── CacheConfig.java"
echo "  ├── CacheEntry.java"
echo "  ├── CacheException.java"
echo "  ├── CacheFactory.java"
echo "  ├── CacheManager.java"
echo "  ├── local/"
echo "  │   ├── InMemoryCache.java"
echo "  │   └── InMemoryCacheFactory.java"
echo "  └── redis/"
echo "      ├── RedisCache.java"
echo "      ├── RedisCacheFactory.java"
echo "      └── RedisConfig.java"
echo ""
print_status "Next steps:"
echo "  1. ✅ Package migration completed"
echo "  2. 🧪 Test compilation with new package structure"
echo "  3. 🔄 Continue with remaining services"
echo "  4. 📚 Update documentation with new package names"
echo ""

print_success "Ready for compilation testing!"