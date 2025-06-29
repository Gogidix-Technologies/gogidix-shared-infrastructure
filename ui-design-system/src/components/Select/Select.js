/**
 * Select Component
 * A versatile dropdown/select component with localization support
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');

/**
 * Select component generator
 * @param {object} props - Select component properties
 * @returns {string} - HTML for the select element
 */
function Select({
  id,
  name,
  options = [],
  value = '',
  placeholder = '',
  label = '',
  helpText = '',
  errorText = '',
  required = false,
  disabled = false,
  multiple = false,
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
  const selectId = id || `select-${Math.random().toString(36).substring(2, 9)}`;
  
  // Get appropriate styles based on props
  const sizeStyles = getSizeStyles(size);
  
  // Determine if select is in an error state
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
      for="${selectId}" 
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
  
  // Generate options HTML
  let optionsHtml = '';
  
  // Add placeholder option if provided
  if (placeholder && !multiple) {
    optionsHtml += `
      <option value="" disabled ${!value ? 'selected' : ''}>
        ${placeholder}
      </option>
    `;
  }
  
  // Add options from array
  options.forEach(option => {
    const isSelected = Array.isArray(value) 
      ? value.includes(option.value) 
      : value === option.value;
      
    optionsHtml += `
      <option 
        value="${option.value}" 
        ${isSelected ? 'selected' : ''}
        ${option.disabled ? 'disabled' : ''}
      >
        ${option.label}
      </option>
    `;
  });
  
  // Create custom arrow icon for the select
  const arrowIcon = `
    <div class="select-arrow" style="
      position: absolute;
      ${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[3]};
      top: 50%;
      transform: translateY(-50%);
      pointer-events: none;
      width: 10px;
      height: 10px;
      border-${dir === 'rtl' ? 'right' : 'left'}: 2px solid ${tokens.colors.neutral.gray[600]};
      border-bottom: 2px solid ${tokens.colors.neutral.gray[600]};
      transform: ${dir === 'rtl' ? 'translateY(-50%) rotate(135deg)' : 'translateY(-50%) rotate(-45deg)'};
      ${disabled ? `opacity: 0.5;` : ''}
    ">
    </div>
  `;
  
  // Build select HTML
  const selectHtml = `
    <div class="select-wrapper" style="position: relative;">
      <select
        id="${selectId}"
        name="${name || selectId}"
        dir="${dir}"
        class="select ${className}"
        style="
          width: 100%;
          box-sizing: border-box;
          ${sizeStyles}
          border: ${borderStyle};
          border-radius: ${tokens.borders.borderRadius.md};
          background-color: ${disabled ? tokens.colors.neutral.gray[100] : tokens.colors.neutral.white};
          color: ${disabled ? tokens.colors.neutral.gray[500] : tokens.colors.neutral.gray[900]};
          ${boxShadow ? `box-shadow: ${boxShadow};` : ''}
          transition: ${tokens.animations.transitions.all};
          appearance: none;
          padding-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[8]};
          
          &:focus {
            border: ${tokens.borders.presets.input.focus};
            box-shadow: ${tokens.shadows.presets.input.focus};
            outline: none;
          }
          
          &:hover:not(:disabled) {
            border: ${tokens.borders.presets.input.hover};
          }
        "
        ${multiple ? 'multiple' : ''}
        ${required ? 'required' : ''}
        ${disabled ? 'disabled' : ''}
        ${onChange ? `onchange="${onChange}"` : ''}
        ${onFocus ? `onfocus="${onFocus}"` : ''}
        ${onBlur ? `onblur="${onBlur}"` : ''}
        ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
      >
        ${optionsHtml}
      </select>
      ${!multiple ? arrowIcon : ''}
    </div>
  `;
  
  // Combine all elements
  return `
    <div 
      class="select-container" 
      style="margin-bottom: ${tokens.spacing.md};"
    >
      ${labelHtml}
      ${selectHtml}
      ${helpTextHtml}
      ${errorTextHtml}
    </div>
  `;
}

/**
 * Get styles based on select size
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

// Export the Select component
module.exports = Select;
