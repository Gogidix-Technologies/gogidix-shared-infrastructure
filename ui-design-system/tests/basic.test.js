// Basic test to ensure UI design system can initialize (Node.js native)
console.log('Starting basic ui-design-system tests...');

// Test 1: Package validation
try {
  const pkg = require('../package.json');
  if (pkg.name && pkg.version) {
    console.log('✅ Package.json validation passed');
  } else {
    throw new Error('Package.json missing required fields');
  }
} catch (error) {
  console.error('❌ Package.json validation failed:', error.message);
  process.exit(1);
}

// Test 2: Component structure validation
try {
  const fs = require('fs');
  const path = require('path');
  
  const srcPath = path.join(__dirname, '../src');
  if (fs.existsSync(srcPath)) {
    const componentsPath = path.join(srcPath, 'components');
    if (fs.existsSync(componentsPath)) {
      console.log('✅ Component structure validation passed');
    } else {
      throw new Error('Components directory not found');
    }
  } else {
    throw new Error('Source directory not found');
  }
} catch (error) {
  console.error('❌ Component structure validation failed:', error.message);
  process.exit(1);
}

console.log('✅ All ui-design-system tests passed');
process.exit(0);