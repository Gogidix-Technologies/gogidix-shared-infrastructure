/**
 * Button Component
 * A versatile button component that supports various sizes, variants, and localization.
 * For use by Team Alpha in customer-facing interfaces and Team Omega in operational interfaces.
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');

/**
 * Button component generator
 * @param {object} props - Button properties
 * @returns {string} - HTML for the button
 */
function Button({
  children,
  variant = 'primary',
  size = 'md',
  disabled = false,
  fullWidth = false,
  iconLeft = null,
  iconRight = null,
  onClick,
  locale = localization.DEFAULT_LOCALE,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Get appropriate styles based on props
  const variantStyles = getVariantStyles(variant);
  const sizeStyles = getSizeStyles(size);
  const disabledStyles = disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer';
  const widthStyles = fullWidth ? 'w-full' : '';
  
  // Get icon markup if provided
  const leftIconMarkup = iconLeft ? `<span class="icon-left">${iconLeft}</span>` : '';
  const rightIconMarkup = iconRight ? `<span class="icon-right">${iconRight}</span>` : '';
  
  // Generate final CSS classes
  const buttonClasses = [
    'button',
    variantStyles,
    sizeStyles,
    disabledStyles,
    widthStyles,
    className
  ].filter(Boolean).join(' ');
  
  // Build button with appropriate direction attribute for RTL support
  return `
    <button 
      class="${buttonClasses}" 
      dir="${dir}"
      ${disabled ? 'disabled' : ''}
      ${onClick ? `onclick="${onClick}"` : ''}
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${dir === 'rtl' 
        ? `${rightIconMarkup}<span class="button-text">${children}</span>${leftIconMarkup}`
        : `${leftIconMarkup}<span class="button-text">${children}</span>${rightIconMarkup}`
      }
    </button>
  `;
}

/**
 * Get styles based on button variant
 */
function getVariantStyles(variant) {
  const { brand, neutral } = tokens.colors;
  
  const variants = {
    primary: `
      bg-[${brand.primary[500]}]
      text-white
      hover:bg-[${brand.primary[600]}]
      active:bg-[${brand.primary[700]}]
      border border-[${brand.primary[500]}]
    `,
    secondary: `
      bg-[${neutral.white}]
      text-[${brand.primary[500]}]
      hover:bg-[${brand.primary[50]}]
      active:bg-[${brand.primary[100]}]
      border border-[${brand.primary[500]}]
    `,
    tertiary: `
      bg-transparent
      text-[${brand.primary[500]}]
      hover:text-[${brand.primary[700]}]
      active:text-[${brand.primary[800]}]
      border-none
      underline
    `,
    success: `
      bg-[${tokens.colors.semantic.success.standard}]
      text-white
      hover:bg-[${tokens.colors.semantic.success.dark}]
      active:bg-[${tokens.colors.semantic.success.dark}]
      border border-[${tokens.colors.semantic.success.standard}]
    `,
    danger: `
      bg-[${tokens.colors.semantic.error.standard}]
      text-white
      hover:bg-[${tokens.colors.semantic.error.dark}]
      active:bg-[${tokens.colors.semantic.error.dark}]
      border border-[${tokens.colors.semantic.error.standard}]
    `,
  };
  
  return variants[variant] || variants.primary;
}

/**
 * Get styles based on button size
 */
function getSizeStyles(size) {
  const { spacing } = tokens;
  const { textStyles } = tokens.typography;
  
  const sizes = {
    xs: `
      py-[${spacing[1]}]
      px-[${spacing[2]}]
      text-[${textStyles.caption.fontSize}]
      font-[${textStyles.caption.fontWeight}]
    `,
    sm: `
      py-[${spacing[2]}]
      px-[${spacing[3]}]
      text-[${textStyles.bodySmall.fontSize}]
      font-[${textStyles.bodySmall.fontWeight}]
    `,
    md: `
      py-[${spacing[3]}]
      px-[${spacing[4]}]
      text-[${textStyles.bodyMedium.fontSize}]
      font-[${textStyles.button.fontWeight}]
    `,
    lg: `
      py-[${spacing[4]}]
      px-[${spacing[5]}]
      text-[${textStyles.bodyLarge.fontSize}]
      font-[${textStyles.button.fontWeight}]
    `,
    xl: `
      py-[${spacing[5]}]
      px-[${spacing[6]}]
      text-[${textStyles.h6.fontSize}]
      font-[${textStyles.button.fontWeight}]
    `,
  };
  
  return sizes[size] || sizes.md;
}

module.exports = Button;
