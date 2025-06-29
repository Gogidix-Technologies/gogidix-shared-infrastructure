/**
 * Input Component
 * A versatile input field component with localization support
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');

/**
 * Input component generator
 * @param {object} props - Input properties
 * @returns {string} - HTML for the input element
 */
function Input({
  type = 'text',
  id,
  name,
  value = '',
  placeholder = '',
  label = '',
  helpText = '',
  errorText = '',
  required = false,
  disabled = false,
  readOnly = false,
  size = 'md',
  locale = localization.DEFAULT_LOCALE,
  onChange,
  onFocus,
  onBlur,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const inputId = id || `input-${Math.random().toString(36).substring(2, 9)}`;
  
  // Get appropriate styles based on props
  const sizeStyles = getSizeStyles(size);
  
  // Determine if input is in an error state
  const hasError = !!errorText;
  
  // Get appropriate border based on state
  const borderStyle = hasError 
    ? tokens.borders.presets.input.error 
    : tokens.borders.presets.input.normal;
  
  // Get appropriate shadow based on state
  const boxShadow = hasError 
    ? tokens.shadows.presets.input.error 
    : '';
  
  // Create label HTML if provided
  const labelHtml = label ? `
    <label 
      for="${inputId}" 
      style="
        display: block;
        margin-bottom: ${tokens.spacing[2]};
        font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
        font-weight: ${tokens.typography.fontWeights.medium};
        color: ${tokens.colors.neutral.gray[800]};
      "
    >
      ${label}
      ${required ? `<span style="color: ${tokens.colors.semantic.error.standard}; margin-left: ${tokens.spacing[1]};">*</span>` : ''}
    </label>
  ` : '';
  
  // Create help text HTML if provided
  const helpTextHtml = helpText && !hasError ? `
    <div 
      class="help-text"
      style="
        margin-top: ${tokens.spacing[2]};
        font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        color: ${tokens.colors.neutral.gray[600]};
      "
    >
      ${helpText}
    </div>
  ` : '';
  
  // Create error text HTML if provided
  const errorTextHtml = hasError ? `
    <div 
      class="error-text"
      style="
        margin-top: ${tokens.spacing[2]};
        font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        color: ${tokens.colors.semantic.error.standard};
      "
    >
      ${errorText}
    </div>
  ` : '';
  
  // Build input HTML
  const inputHtml = `
    <input
      type="${type}"
      id="${inputId}"
      name="${name || inputId}"
      value="${value}"
      placeholder="${placeholder}"
      dir="${dir}"
      class="input ${className}"
      style="
        width: 100%;
        box-sizing: border-box;
        ${sizeStyles}
        border: ${borderStyle};
        border-radius: ${tokens.borders.borderRadius.md};
        background-color: ${disabled ? tokens.colors.neutral.gray[100] : tokens.colors.neutral.white};
        color: ${disabled ? tokens.colors.neutral.gray[500] : tokens.colors.neutral.gray[900]};
        ${boxShadow ? `box-shadow: ${boxShadow};` : ''}
        transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
        
        &:focus {
          border: ${tokens.borders.presets.input.focus};
          box-shadow: ${tokens.shadows.presets.input.focus};
          outline: none;
        }
        
        &:hover:not(:disabled) {
          border: ${tokens.borders.presets.input.hover};
        }
        
        ${dir === 'rtl' ? 'text-align: right;' : ''}
      "
      ${required ? 'required' : ''}
      ${disabled ? 'disabled' : ''}
      ${readOnly ? 'readonly' : ''}
      ${onChange ? `onchange="${onChange}"` : ''}
      ${onFocus ? `onfocus="${onFocus}"` : ''}
      ${onBlur ? `onblur="${onBlur}"` : ''}
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    />
  `;
  
  // Combine all elements
  return `
    <div 
      class="input-container"
      style="margin-bottom: ${tokens.spacing.md};"
    >
      ${labelHtml}
      ${inputHtml}
      ${helpTextHtml}
      ${errorTextHtml}
    </div>
  `;
}

/**
 * Get styles based on input size
 */
function getSizeStyles(size) {
  const { spacing } = tokens;
  const { textStyles } = tokens.typography;
  
  const sizes = {
    sm: `
      padding: ${spacing[2]} ${spacing[3]};
      height: 32px;
      font-size: ${textStyles.bodySmall.fontSize};
    `,
    md: `
      padding: ${spacing[3]} ${spacing[4]};
      height: 40px;
      font-size: ${textStyles.bodyMedium.fontSize};
    `,
    lg: `
      padding: ${spacing[4]} ${spacing[5]};
      height: 48px;
      font-size: ${textStyles.bodyLarge.fontSize};
    `
  };
  
  return sizes[size] || sizes.md;
}

// Export the Input component
module.exports = Input;
