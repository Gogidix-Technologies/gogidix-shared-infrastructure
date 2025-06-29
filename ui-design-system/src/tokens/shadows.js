/**
 * Shadow tokens for the Social Commerce Ecosystem UI Design System
 * These shadow definitions create consistent elevation and depth
 * across Team Alpha (customer-facing) and Team Omega (operations) interfaces.
 */

// Base shadow values with increasing elevation levels
const elevation = {
  none: 'none',
  xs: '0px 1px 2px rgba(0, 0, 0, 0.05)',
  sm: '0px 1px 3px rgba(0, 0, 0, 0.1), 0px 1px 2px rgba(0, 0, 0, 0.06)',
  md: '0px 4px 6px -1px rgba(0, 0, 0, 0.1), 0px 2px 4px -1px rgba(0, 0, 0, 0.06)',
  lg: '0px 10px 15px -3px rgba(0, 0, 0, 0.1), 0px 4px 6px -2px rgba(0, 0, 0, 0.05)',
  xl: '0px 20px 25px -5px rgba(0, 0, 0, 0.1), 0px 10px 10px -5px rgba(0, 0, 0, 0.04)',
  '2xl': '0px 25px 50px -12px rgba(0, 0, 0, 0.25)',
  inner: 'inset 0px 2px 4px rgba(0, 0, 0, 0.06)'
};

// Functional shadow presets for different component types
const presets = {
  // Card shadows
  card: {
    default: elevation.sm,
    hover: elevation.md,
    active: elevation.lg,
    featured: elevation.xl
  },
  
  // Modal/dialog shadows
  modal: {
    default: elevation.xl,
    overlay: '0px 0px 0px 100vw rgba(0, 0, 0, 0.4)'
  },
  
  // Dropdown/popover shadows
  dropdown: {
    default: '0px 2px 8px rgba(0, 0, 0, 0.15)'
  },
  
  // Button shadows
  button: {
    default: elevation.sm,
    hover: elevation.md,
    active: elevation.xs,
    focus: `0px 0px 0px 3px rgba(24, 144, 255, 0.2)` // For focus rings
  },
  
  // Form input shadows
  input: {
    focus: `0px 0px 0px 3px rgba(24, 144, 255, 0.2)`,
    error: `0px 0px 0px 3px rgba(245, 34, 45, 0.2)`
  },
  
  // Tooltip shadows
  tooltip: {
    default: '0px 3px 6px -4px rgba(0, 0, 0, 0.48), 0px 6px 16px rgba(0, 0, 0, 0.08)'
  }
};

// Theme-specific shadows (light vs dark mode)
const themeShadows = {
  light: {
    focusRing: `0px 0px 0px 3px rgba(24, 144, 255, 0.4)`,
    successFocusRing: `0px 0px 0px 3px rgba(82, 196, 26, 0.4)`,
    warningFocusRing: `0px 0px 0px 3px rgba(250, 173, 20, 0.4)`,
    errorFocusRing: `0px 0px 0px 3px rgba(245, 34, 45, 0.4)`,
    infoFocusRing: `0px 0px 0px 3px rgba(24, 144, 255, 0.4)`
  },
  dark: {
    // Dark theme shadows have higher opacity for better visibility
    focusRing: `0px 0px 0px 3px rgba(24, 144, 255, 0.6)`,
    successFocusRing: `0px 0px 0px 3px rgba(82, 196, 26, 0.6)`,
    warningFocusRing: `0px 0px 0px 3px rgba(250, 173, 20, 0.6)`,
    errorFocusRing: `0px 0px 0px 3px rgba(245, 34, 45, 0.6)`,
    infoFocusRing: `0px 0px 0px 3px rgba(24, 144, 255, 0.6)`
  }
};

// Mobile-specific shadows (often more pronounced for better visual hierarchy on small screens)
const mobileShadows = {
  header: '0px 2px 8px rgba(0, 0, 0, 0.15)',
  floatingAction: '0px 6px 16px rgba(0, 0, 0, 0.12)',
  bottomNav: '0px -2px 8px rgba(0, 0, 0, 0.08)'
};

// Export all shadow tokens
module.exports = {
  elevation,
  presets,
  themeShadows,
  mobileShadows
};
