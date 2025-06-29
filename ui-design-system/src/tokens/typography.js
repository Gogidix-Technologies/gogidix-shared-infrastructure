/**
 * Typography tokens for the Social Commerce Ecosystem UI Design System
 * These typography definitions ensure consistent text styling
 * across Team Alpha (customer-facing) and Team Omega (operations) interfaces.
 */

const fontFamilies = {
  // Primary font for most UI elements
  primary: {
    regular: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    monospace: "'IBM Plex Mono', 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace"
  },
  
  // Regional font stacks for better localization support
  regional: {
    arabic: "'IBM Plex Arabic', 'Noto Sans Arabic', sans-serif",
    chinese: "'Noto Sans SC', 'PingFang SC', 'Microsoft YaHei', sans-serif",
    japanese: "'Noto Sans JP', 'Hiragino Sans', 'Hiragino Kaku Gothic Pro', sans-serif",
    korean: "'Noto Sans KR', 'Apple SD Gothic Neo', 'Malgun Gothic', sans-serif",
    thai: "'Noto Sans Thai', 'Thonburi', 'Leelawadee', sans-serif",
    devanagari: "'Noto Sans Devanagari', sans-serif"
  }
};

const fontWeights = {
  thin: 100,
  extraLight: 200,
  light: 300,
  regular: 400,
  medium: 500,
  semiBold: 600,
  bold: 700,
  extraBold: 800,
  black: 900
};

const fontSizes = {
  // Base sizes in px
  xs: '12px',
  sm: '14px',
  md: '16px',
  lg: '18px',
  xl: '20px',
  '2xl': '24px',
  '3xl': '30px',
  '4xl': '36px',
  '5xl': '48px',
  '6xl': '60px',
  
  // Responsive font sizes with clamp for better responsiveness
  // Format: min size, preferred size (with viewport width), max size
  responsive: {
    heading1: 'clamp(36px, 5vw, 60px)',
    heading2: 'clamp(30px, 4vw, 48px)',
    heading3: 'clamp(24px, 3vw, 36px)',
    heading4: 'clamp(20px, 2.5vw, 30px)',
    heading5: 'clamp(18px, 2vw, 24px)',
    heading6: 'clamp(16px, 1.5vw, 20px)',
    bodyLarge: 'clamp(16px, 1.2vw, 18px)',
    bodyMedium: 'clamp(14px, 1vw, 16px)',
    bodySmall: 'clamp(12px, 0.9vw, 14px)'
  }
};

const lineHeights = {
  tight: 1.1,    // Headings
  snug: 1.25,    // Subheadings
  normal: 1.5,   // Body text
  relaxed: 1.625,// Long-form content
  loose: 2       // Extra spacious content
};

const letterSpacing = {
  tighter: '-0.05em',
  tight: '-0.025em',
  normal: '0',
  wide: '0.025em',
  wider: '0.05em',
  widest: '0.1em'
};

// Text style combinations for common use cases
const textStyles = {
  // Headings
  h1: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.heading1,
    fontWeight: fontWeights.bold,
    lineHeight: lineHeights.tight,
    letterSpacing: letterSpacing.tight
  },
  h2: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.heading2,
    fontWeight: fontWeights.bold,
    lineHeight: lineHeights.tight,
    letterSpacing: letterSpacing.tight
  },
  h3: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.heading3,
    fontWeight: fontWeights.semiBold,
    lineHeight: lineHeights.snug,
    letterSpacing: letterSpacing.normal
  },
  h4: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.heading4,
    fontWeight: fontWeights.semiBold,
    lineHeight: lineHeights.snug,
    letterSpacing: letterSpacing.normal
  },
  h5: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.heading5,
    fontWeight: fontWeights.medium,
    lineHeight: lineHeights.snug,
    letterSpacing: letterSpacing.normal
  },
  h6: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.heading6,
    fontWeight: fontWeights.medium,
    lineHeight: lineHeights.snug,
    letterSpacing: letterSpacing.normal
  },
  
  // Body text
  bodyLarge: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.bodyLarge,
    fontWeight: fontWeights.regular,
    lineHeight: lineHeights.normal,
    letterSpacing: letterSpacing.normal
  },
  bodyMedium: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.bodyMedium,
    fontWeight: fontWeights.regular,
    lineHeight: lineHeights.normal,
    letterSpacing: letterSpacing.normal
  },
  bodySmall: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.responsive.bodySmall,
    fontWeight: fontWeights.regular,
    lineHeight: lineHeights.normal,
    letterSpacing: letterSpacing.normal
  },
  
  // Special text styles
  caption: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.xs,
    fontWeight: fontWeights.regular,
    lineHeight: lineHeights.normal,
    letterSpacing: letterSpacing.wide
  },
  button: {
    fontFamily: fontFamilies.primary.regular,
    fontSize: fontSizes.md,
    fontWeight: fontWeights.medium,
    lineHeight: lineHeights.snug,
    letterSpacing: letterSpacing.wide
  },
  code: {
    fontFamily: fontFamilies.primary.monospace,
    fontSize: fontSizes.sm,
    fontWeight: fontWeights.regular,
    lineHeight: lineHeights.normal,
    letterSpacing: letterSpacing.normal
  }
};

// Directional text support for RTL languages
const direction = {
  ltr: 'ltr',
  rtl: 'rtl'
};

// Export all typography tokens
module.exports = {
  fontFamilies,
  fontWeights,
  fontSizes,
  lineHeights,
  letterSpacing,
  textStyles,
  direction
};
