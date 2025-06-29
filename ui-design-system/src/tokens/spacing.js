/**
 * Spacing tokens for the Social Commerce Ecosystem UI Design System
 * These spacing definitions ensure consistent layout and component spacing
 * across Team Alpha (customer-facing) and Team Omega (operations) interfaces.
 */

// Base spacing unit in pixels
const baseUnit = 4;

// Spacing scale based on the base unit
const spacing = {
  // Core spacing scale (multiples of baseUnit)
  0: '0',
  1: `${baseUnit * 0.25}px`,  // 1px
  2: `${baseUnit * 0.5}px`,   // 2px
  3: `${baseUnit * 0.75}px`,  // 3px
  4: `${baseUnit}px`,         // 4px
  5: `${baseUnit * 1.25}px`,  // 5px
  6: `${baseUnit * 1.5}px`,   // 6px
  8: `${baseUnit * 2}px`,     // 8px
  10: `${baseUnit * 2.5}px`,  // 10px
  12: `${baseUnit * 3}px`,    // 12px
  16: `${baseUnit * 4}px`,    // 16px
  20: `${baseUnit * 5}px`,    // 20px
  24: `${baseUnit * 6}px`,    // 24px
  32: `${baseUnit * 8}px`,    // 32px
  40: `${baseUnit * 10}px`,   // 40px
  48: `${baseUnit * 12}px`,   // 48px
  56: `${baseUnit * 14}px`,   // 56px
  64: `${baseUnit * 16}px`,   // 64px
  80: `${baseUnit * 20}px`,   // 80px
  96: `${baseUnit * 24}px`,   // 96px
  128: `${baseUnit * 32}px`,  // 128px
  160: `${baseUnit * 40}px`,  // 160px
  192: `${baseUnit * 48}px`,  // 192px
  224: `${baseUnit * 56}px`,  // 224px
  256: `${baseUnit * 64}px`,  // 256px
  
  // Named aliases for common use cases
  xs: `${baseUnit}px`,        // 4px
  sm: `${baseUnit * 2}px`,    // 8px
  md: `${baseUnit * 4}px`,    // 16px
  lg: `${baseUnit * 6}px`,    // 24px
  xl: `${baseUnit * 8}px`,    // 32px
  '2xl': `${baseUnit * 12}px`,// 48px
  '3xl': `${baseUnit * 16}px`,// 64px
  '4xl': `${baseUnit * 24}px`,// 96px
};

// Inset spacing for padding
const inset = {
  // Symmetrical insets (same padding on all sides)
  xs: spacing.xs,
  sm: spacing.sm,
  md: spacing.md,
  lg: spacing.lg,
  xl: spacing.xl,
  
  // Squish insets (less padding on top/bottom than sides)
  squishXs: `${parseInt(spacing.xs) / 2}px ${spacing.xs}`,
  squishSm: `${parseInt(spacing.sm) / 2}px ${spacing.sm}`,
  squishMd: `${parseInt(spacing.md) / 2}px ${spacing.md}`,
  squishLg: `${parseInt(spacing.lg) / 2}px ${spacing.lg}`,
  
  // Stretch insets (more padding on top/bottom than sides)
  stretchXs: `${parseInt(spacing.xs) * 1.5}px ${spacing.xs}`,
  stretchSm: `${parseInt(spacing.sm) * 1.5}px ${spacing.sm}`,
  stretchMd: `${parseInt(spacing.md) * 1.5}px ${spacing.md}`,
  stretchLg: `${parseInt(spacing.lg) * 1.5}px ${spacing.lg}`,
};

// Layout spacing for larger layout elements
const layout = {
  gutter: spacing['16'],       // Standard gutter between grid columns
  margin: spacing['16'],       // Page margins on small screens
  marginMd: spacing['32'],     // Page margins on medium screens
  marginLg: spacing['64'],     // Page margins on large screens
  marginXl: spacing['96'],     // Page margins on extra large screens
  
  // Containers
  containerSm: '640px',        // Small container max width
  containerMd: '768px',        // Medium container max width
  containerLg: '1024px',       // Large container max width
  containerXl: '1280px',       // Extra large container max width
  container2xl: '1536px',      // 2X extra large container max width
  
  // Section spacing
  sectionSm: spacing['64'],    // Small section spacing
  sectionMd: spacing['96'],    // Medium section spacing
  sectionLg: spacing['128'],   // Large section spacing
  sectionXl: spacing['192'],   // Extra large section spacing
};

// Export all spacing tokens
module.exports = {
  baseUnit,
  spacing,
  inset,
  layout,
};
