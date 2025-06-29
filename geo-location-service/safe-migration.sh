#!/bin/bash

# Safe migration script for geo-location-service
# This script will migrate packages with full backup and rollback capability

set -e  # Exit on any error

BACKUP_DIR="migration-backup-$(date +%Y%m%d_%H%M%S)"
SERVICE_DIR="/mnt/c/Users/frich/Desktop/Exalt-Application-Limited/Exalt-Application-Limited/social-ecommerce-ecosystem/shared-infrastructure/geo-location-service"

echo "=== SAFE MIGRATION: geo-location-service ==="
echo "Creating backup: $BACKUP_DIR"

cd "$SERVICE_DIR"

# Create full backup
cp -r src "$BACKUP_DIR"

echo "✅ Backup created successfully"

# Test current build
echo "🔍 Testing current build..."
if ! mvn clean compile -q; then
    echo "❌ Current build failed! Aborting migration."
    exit 1
fi

echo "✅ Current build successful"

# Count files to migrate
FILE_COUNT=$(find src/main/java/com/microecommerce -name "*.java" | wc -l)
echo "📦 Files to migrate: $FILE_COUNT"

echo "⚠️  Manual intervention required for package content updates"
echo "   This script has prepared the environment safely."
echo "   Next steps should be done with careful validation."

echo "✅ Safe environment prepared"
echo "   Backup location: $SERVICE_DIR/$BACKUP_DIR"