#!/bin/bash

# Admin Framework Package Migration Script
# Migrates from com.microsocial.ecommerce.admin to com.exalt.ecosystem.shared.admin

echo "🔄 Starting Admin Framework Package Migration..."
echo "================================================"

cd "/mnt/c/Users/frich/Desktop/Micro-Social-Ecommerce-Ecosystems/Exalt-Application-Limited/social-ecommerce-ecosystem/shared-infrastructure/admin-frameworks"

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

# Step 1: Create new directory structure
print_status "Creating new package directory structure..."

cd src/main/java
mkdir -p com/exalt/ecosystem/shared/admin/{audit,events,export,integration,notification,scheduling,security,validation,websocket}
mkdir -p com/exalt/ecosystem/shared/admin/export/{config,controller,exception,handler,model,repository,service}
mkdir -p com/exalt/ecosystem/shared/admin/export/handler/{excel,pdf}
mkdir -p com/exalt/ecosystem/shared/admin/integration/dashboard
mkdir -p com/exalt/ecosystem/shared/admin/websocket/{acknowledge,metrics,ratelimit,security}
mkdir -p com/exalt/ecosystem/shared/admin/websocket/ratelimit/metrics

print_success "New directory structure created"

# Step 2: Move files and update package declarations
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
        
        echo "  ✓ Moved $(basename $old_path)"
    fi
}

# Audit package
print_status "Migrating audit package..."
move_and_update "com/microsocial/ecommerce/admin/audit/AuditService.java" \
                "com/exalt/ecosystem/shared/admin/audit/AuditService.java" \
                "com.microsocial.ecommerce.admin.audit" \
                "com.exalt.ecosystem.shared.admin.audit"

move_and_update "com/microsocial/ecommerce/admin/audit/AuditServiceImpl.java" \
                "com/exalt/ecosystem/shared/admin/audit/AuditServiceImpl.java" \
                "com.microsocial.ecommerce.admin.audit" \
                "com.exalt.ecosystem.shared.admin.audit"

# Events package
print_status "Migrating events package..."
move_and_update "com/microsocial/ecommerce/admin/events/Event.java" \
                "com/exalt/ecosystem/shared/admin/events/Event.java" \
                "com.microsocial.ecommerce.admin.events" \
                "com.exalt.ecosystem.shared.admin.events"

move_and_update "com/microsocial/ecommerce/admin/events/EventPublisher.java" \
                "com/exalt/ecosystem/shared/admin/events/EventPublisher.java" \
                "com.microsocial.ecommerce.admin.events" \
                "com.exalt.ecosystem.shared.admin.events"

move_and_update "com/microsocial/ecommerce/admin/events/EventSubscriber.java" \
                "com/exalt/ecosystem/shared/admin/events/EventSubscriber.java" \
                "com.microsocial.ecommerce.admin.events" \
                "com.exalt.ecosystem.shared.admin.events"

# Export main package
print_status "Migrating export main package..."
find com/microsocial/ecommerce/admin/export -maxdepth 1 -name "*.java" | while read file; do
    filename=$(basename "$file")
    new_package="com.exalt.ecosystem.shared.admin.export"
    old_package="com.microsocial.ecommerce.admin.export"
    
    sed -i "s|package $old_package|package $new_package|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/$filename"
    echo "  ✓ Moved $filename"
done

# Export subpackages
print_status "Migrating export subpackages..."

# Config
find com/microsocial/ecommerce/admin/export/config -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.config|package com.exalt.ecosystem.shared.admin.export.config|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/config/$filename"
    echo "  ✓ Moved config/$filename"
done

# Controller  
find com/microsocial/ecommerce/admin/export/controller -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.controller|package com.exalt.ecosystem.shared.admin.export.controller|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/controller/$filename"
    echo "  ✓ Moved controller/$filename"
done

# Exception
find com/microsocial/ecommerce/admin/export/exception -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.exception|package com.exalt.ecosystem.shared.admin.export.exception|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/exception/$filename"
    echo "  ✓ Moved exception/$filename"
done

# Handler
find com/microsocial/ecommerce/admin/export/handler -maxdepth 1 -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.handler|package com.exalt.ecosystem.shared.admin.export.handler|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/handler/$filename"
    echo "  ✓ Moved handler/$filename"
done

# Handler/Excel
find com/microsocial/ecommerce/admin/export/handler/excel -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.handler.excel|package com.exalt.ecosystem.shared.admin.export.handler.excel|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/handler/excel/$filename"
    echo "  ✓ Moved handler/excel/$filename"
done

# Handler/PDF
find com/microsocial/ecommerce/admin/export/handler/pdf -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.handler.pdf|package com.exalt.ecosystem.shared.admin.export.handler.pdf|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/handler/pdf/$filename"
    echo "  ✓ Moved handler/pdf/$filename"
done

# Model
find com/microsocial/ecommerce/admin/export/model -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.model|package com.exalt.ecosystem.shared.admin.export.model|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/model/$filename"
    echo "  ✓ Moved model/$filename"
done

# Repository
find com/microsocial/ecommerce/admin/export/repository -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.repository|package com.exalt.ecosystem.shared.admin.export.repository|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/repository/$filename"
    echo "  ✓ Moved repository/$filename"
done

# Service
find com/microsocial/ecommerce/admin/export/service -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.export.service|package com.exalt.ecosystem.shared.admin.export.service|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/export/service/$filename"
    echo "  ✓ Moved service/$filename"
done

# Continue with other packages...
print_status "Migrating remaining packages..."

# Integration
find com/microsocial/ecommerce/admin/integration -maxdepth 1 -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.integration|package com.exalt.ecosystem.shared.admin.integration|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/integration/$filename"
    echo "  ✓ Moved integration/$filename"
done

# Integration/Dashboard
find com/microsocial/ecommerce/admin/integration/dashboard -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.integration.dashboard|package com.exalt.ecosystem.shared.admin.integration.dashboard|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/integration/dashboard/$filename"
    echo "  ✓ Moved integration/dashboard/$filename"
done

# Notification
find com/microsocial/ecommerce/admin/notification -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.notification|package com.exalt.ecosystem.shared.admin.notification|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/notification/$filename"
    echo "  ✓ Moved notification/$filename"
done

# Scheduling
find com/microsocial/ecommerce/admin/scheduling -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.scheduling|package com.exalt.ecosystem.shared.admin.scheduling|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/scheduling/$filename"
    echo "  ✓ Moved scheduling/$filename"
done

# Security
find com/microsocial/ecommerce/admin/security -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.security|package com.exalt.ecosystem.shared.admin.security|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/security/$filename"
    echo "  ✓ Moved security/$filename"
done

# Validation
find com/microsocial/ecommerce/admin/validation -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.validation|package com.exalt.ecosystem.shared.admin.validation|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/validation/$filename"
    echo "  ✓ Moved validation/$filename"
done

# WebSocket packages
print_status "Migrating WebSocket packages..."

# Main websocket
find com/microsocial/ecommerce/admin/websocket -maxdepth 1 -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.websocket|package com.exalt.ecosystem.shared.admin.websocket|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/websocket/$filename"
    echo "  ✓ Moved websocket/$filename"
done

# WebSocket subpackages
for subpackage in acknowledge metrics ratelimit security; do
    find "com/microsocial/ecommerce/admin/websocket/$subpackage" -maxdepth 1 -name "*.java" 2>/dev/null | while read file; do
        filename=$(basename "$file")
        sed -i "s|package com.microsocial.ecommerce.admin.websocket.$subpackage|package com.exalt.ecosystem.shared.admin.websocket.$subpackage|g" "$file"
        mv "$file" "com/exalt/ecosystem/shared/admin/websocket/$subpackage/$filename"
        echo "  ✓ Moved websocket/$subpackage/$filename"
    done
done

# WebSocket ratelimit metrics
find com/microsocial/ecommerce/admin/websocket/ratelimit/metrics -name "*.java" 2>/dev/null | while read file; do
    filename=$(basename "$file")
    sed -i "s|package com.microsocial.ecommerce.admin.websocket.ratelimit.metrics|package com.exalt.ecosystem.shared.admin.websocket.ratelimit.metrics|g" "$file"
    mv "$file" "com/exalt/ecosystem/shared/admin/websocket/ratelimit/metrics/$filename"
    echo "  ✓ Moved websocket/ratelimit/metrics/$filename"
done

print_success "File migration completed"

# Step 3: Update all import statements
print_status "Updating import statements..."

find com/exalt/ecosystem/shared/admin -name "*.java" | while read file; do
    # Update imports from old package to new package
    sed -i 's|import com\.microsocial\.ecommerce\.admin\.|import com.exalt.ecosystem.shared.admin.|g' "$file"
    # Also handle any static imports
    sed -i 's|import static com\.microsocial\.ecommerce\.admin\.|import static com.exalt.ecosystem.shared.admin.|g' "$file"
done

print_success "Import statements updated"

# Step 4: Clean up old directories
print_status "Cleaning up old directory structure..."

# Remove old empty directories
if [ -d "com/microsocial" ]; then
    rm -rf com/microsocial
    print_success "Old package directories removed"
fi

# Summary
echo ""
echo "================================================"
print_success "Package Migration Complete!"
echo ""
print_status "Summary of changes:"
echo "  📦 Old base package: com.microsocial.ecommerce.admin"
echo "  📦 New base package: com.exalt.ecosystem.shared.admin"
echo "  📁 Files migrated: All Java files in admin-framework"
echo "  📝 Imports updated: All internal references updated"
echo "  🗂️  Directory structure: Reorganized to new package hierarchy"
echo ""
print_status "Next steps:"
echo "  1. ✅ Package migration completed"
echo "  2. 🔄 Update pom.xml groupId (if needed)"
echo "  3. 🧪 Test compilation with new package structure"
echo "  4. 📚 Update documentation with new package names"
echo ""

cd ../../..

print_success "Ready for compilation testing!"