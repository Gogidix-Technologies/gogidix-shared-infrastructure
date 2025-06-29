/**
 * Border tokens for the Social Commerce Ecosystem UI Design System
 * These border definitions ensure consistent styling of containers and interactive elements
 * across Team Alpha (customer-facing) and Team Omega (operations) interfaces.
 */

const { neutral, brand } = require('./colors');

// Border widths
const borderWidths = {
  none: '0',
  xs: '1px',
  sm: '2px',
  md: '3px',
  lg: '4px',
  xl: '6px',
  focus: '2px'
};

// Border styles
const borderStyles = {
  solid: 'solid',
  dashed: 'dashed',
  dotted: 'dotted',
  double: 'double',
  none: 'none'
};

// Border radius for different component sizes
const borderRadius = {
  none: '0',
  xs: '2px',
  sm: '4px',
  md: '6px',
  lg: '8px',
  xl: '12px',
  '2xl': '16px',
  '3xl': '24px',
  full: '9999px' // For circular elements
};

// Border color presets
const borderColors = {
  // Basic borders
  default: neutral.gray[300],
  light: neutral.gray[200],
  dark: neutral.gray[400],
  
  // Interactive element borders
  focus: brand.primary[500],
  hover: brand.primary[400],
  active: brand.primary[600],
  disabled: neutral.gray[300],
  
  // Semantic borders
  success: {
    default: '#52C41A',
    light: '#B7EB8F',
  },
  warning: {
    default: '#FAAD14',
    light: '#FFE58F',
  },
  error: {
    default: '#F5222D',
    light: '#FFA39E',
  },
  info: {
    default: '#1890FF',
    light: '#91D5FF',
  }
};

// Common border presets for different component types
const presets = {
  input: {
    normal: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.default}`,
    focus: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.focus}`,
    error: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.error.default}`,
    hover: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.hover}`
  },
  card: {
    default: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.light}`,
    hover: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.hover}`,
    featured: `${borderWidths.sm} ${borderStyles.solid} ${borderColors.focus}`
  },
  button: {
    default: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.default}`,
    primary: `${borderWidths.xs} ${borderStyles.solid} ${brand.primary[500]}`,
    focus: `${borderWidths.focus} ${borderStyles.solid} ${borderColors.focus}`
  },
  divider: {
    default: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.light}`,
    dark: `${borderWidths.xs} ${borderStyles.solid} ${borderColors.dark}`,
    dashed: `${borderWidths.xs} ${borderStyles.dashed} ${borderColors.light}`
  }
};

// Regional border styles that can be applied for specific markets
const regionalBorders = {
  asia: {
    decorative: {
      borderWidth: borderWidths.sm,
      borderStyle: borderStyles.solid,
      borderColor: '#DE2910', // Common red in many Asian markets
      borderRadius: borderRadius.sm
    }
  },
  middleEast: {
    decorative: {
      borderWidth: borderWidths.sm,
      borderStyle: borderStyles.solid,
      borderColor: '#078930', // Green common in Middle Eastern markets
      borderRadius: borderRadius.lg
    }
  },
  europe: {
    decorative: {
      borderWidth: borderWidths.sm,
      borderStyle: borderStyles.solid,
      borderColor: '#004494', // EU blue
      borderRadius: borderRadius.md
    }
  }
};

// Export all border tokens
module.exports = {
  borderWidths,
  borderStyles,
  borderRadius,
  borderColors,
  presets,
  regionalBorders
};
