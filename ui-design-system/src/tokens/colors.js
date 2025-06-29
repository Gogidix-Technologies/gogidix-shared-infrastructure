/**
 * Color tokens for the Social Commerce Ecosystem UI Design System
 * These color definitions serve as the foundation for all UI components
 * across Team Alpha (customer-facing) and Team Omega (operations) interfaces.
 */

// Primary brand colors
const brandColors = {
  primary: {
    50: '#E6F7FF',
    100: '#BAE7FF',
    200: '#91D5FF',
    300: '#69C0FF',
    400: '#40A9FF',
    500: '#1890FF', // Primary brand color
    600: '#096DD9',
    700: '#0050B3',
    800: '#003A8C',
    900: '#002766',
  },
  secondary: {
    50: '#F0FFF4',
    100: '#D3F9D8',
    200: '#A6EDB7',
    300: '#74DE8B',
    400: '#49C66E',
    500: '#2EAA55', // Secondary brand color
    600: '#208B44',
    700: '#0F6C33',
    800: '#074D26',
    900: '#033A1C',
  },
  accent: {
    50: '#FFF0F6',
    100: '#FFDEEB',
    200: '#FFC1D7',
    300: '#FFA3C2',
    400: '#FF85AD',
    500: '#FF5C8A', // Accent color for highlights
    600: '#E6336F',
    700: '#CC0C56',
    800: '#A8003E',
    900: '#86002F',
  },
};

// Neutral colors for backgrounds, text, borders
const neutralColors = {
  gray: {
    50: '#FAFAFA',
    100: '#F5F5F5',
    200: '#E8E8E8',
    300: '#D9D9D9',
    400: '#BFBFBF',
    500: '#8C8C8C',
    600: '#595959',
    700: '#434343',
    800: '#262626',
    900: '#141414',
  },
  white: '#FFFFFF',
  black: '#000000',
};

// Semantic colors for status indications
const semanticColors = {
  success: {
    light: '#F6FFED',
    standard: '#52C41A',
    dark: '#389E0D',
  },
  warning: {
    light: '#FFFBE6',
    standard: '#FAAD14',
    dark: '#D48806',
  },
  error: {
    light: '#FFF1F0',
    standard: '#F5222D',
    dark: '#CF1322',
  },
  info: {
    light: '#E6F7FF',
    standard: '#1890FF',
    dark: '#096DD9',
  },
};

// Specific colors for different regions/locales
// Can be extended based on regional preferences
const regionalColors = {
  europe: {
    accent: '#004494', // EU blue
  },
  asia: {
    accent: '#DE2910', // Common red in many Asian markets
  },
  middleEast: {
    accent: '#078930', // Green common in Middle Eastern markets
  },
  africa: {
    accent: '#FDB914', // Gold/yellow often used in African contexts
  },
  northAmerica: {
    accent: '#3C3B6E', // Deep blue
  },
  southAmerica: {
    accent: '#009B3A', // Green common in South American markets
  },
};

// Export all color tokens
module.exports = {
  brand: brandColors,
  neutral: neutralColors,
  semantic: semanticColors,
  regional: regionalColors,
  
  // Theme-specific color mapping
  // Can be expanded for light/dark mode and regional themes
  theme: {
    light: {
      background: {
        primary: neutralColors.white,
        secondary: neutralColors.gray[50],
        tertiary: neutralColors.gray[100],
      },
      text: {
        primary: neutralColors.gray[900],
        secondary: neutralColors.gray[700],
        tertiary: neutralColors.gray[500],
        inverse: neutralColors.white,
      },
      border: {
        light: neutralColors.gray[200],
        medium: neutralColors.gray[300],
        heavy: neutralColors.gray[400],
      },
    },
    dark: {
      background: {
        primary: neutralColors.gray[900],
        secondary: neutralColors.gray[800],
        tertiary: neutralColors.gray[700],
      },
      text: {
        primary: neutralColors.gray[50],
        secondary: neutralColors.gray[200],
        tertiary: neutralColors.gray[400],
        inverse: neutralColors.gray[900],
      },
      border: {
        light: neutralColors.gray[700],
        medium: neutralColors.gray[600],
        heavy: neutralColors.gray[500],
      },
    },
  },
};
