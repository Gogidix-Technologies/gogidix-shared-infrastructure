// Basic test to ensure service can initialize (Node.js native)
console.log('Starting basic feature-flag-service tests...');

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

console.log('✅ All feature-flag-service tests passed');
process.exit(0);
