/**
 * Checkbox Component
 * A versatile checkbox component with localization support
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');

/**
 * Checkbox component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the checkbox element
 */
function Checkbox({
  id,
  name,
  label = '',
  helpText = '',
  errorText = '',
  checked = false,
  indeterminate = false,
  value = '',
  required = false,
  disabled = false,
  locale = localization.DEFAULT_LOCALE,
  onChange,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const checkboxId = id || `checkbox-${Math.random().toString(36).substring(2, 9)}`;
  
  // Determine if checkbox is in an error state
  const hasError = !!errorText;
  
  // Custom checkbox appearance
  const checkboxStyle = `
    position: relative;
    display: inline-block;
    width: 16px;
    height: 16px;
    margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[2]};
    border: ${hasError 
      ? tokens.borders.presets.checkbox.error 
      : disabled 
        ? tokens.borders.presets.checkbox.disabled 
        : tokens.borders.presets.checkbox.normal
    };
    border-radius: ${tokens.borders.borderRadius.sm};
    background-color: ${
      checked 
        ? disabled 
          ? tokens.colors.brand.primary[300]
          : tokens.colors.brand.primary[500]
        : disabled
          ? tokens.colors.neutral.gray[100]
          : tokens.colors.neutral.white
    };
    transition: ${tokens.animations.transitions.all};
    flex-shrink: 0;
  `;
  
  // Custom checkmark icon
  const checkmarkStyle = `
    position: absolute;
    top: 2px;
    left: 5px;
    width: 6px;
    height: 10px;
    border: solid white;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
    display: ${checked ? 'block' : 'none'};
  `;
  
  // Custom indeterminate icon
  const indeterminateStyle = `
    position: absolute;
    top: 7px;
    left: 3px;
    width: 10px;
    height: 2px;
    background-color: white;
    display: ${indeterminate && !checked ? 'block' : 'none'};
  `;
  
  // Create help text HTML if provided
  const helpTextHtml = helpText && !hasError ? `
    <div 
      class="help-text"
      style="
        margin-top: ${tokens.spacing[2]};
        margin-left: ${tokens.spacing[6]};
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
        margin-left: ${tokens.spacing[6]};
        font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        color: ${tokens.colors.semantic.error.standard};
      "
    >
      ${errorText}
    </div>
  ` : '';
  
  // Build checkbox HTML
  return `
    <div 
      class="checkbox-container ${className}"
      style="
        margin-bottom: ${tokens.spacing.sm};
        ${hasError ? `color: ${tokens.colors.semantic.error.standard};` : ''}
      "
      dir="${dir}"
    >
      <div 
        class="checkbox-wrapper"
        style="
          display: flex;
          align-items: center;
          ${disabled ? `opacity: 0.6;` : ''}
        "
      >
        <div style="position: relative; display: flex; align-items: center;">
          <input
            type="checkbox"
            id="${checkboxId}"
            name="${name || checkboxId}"
            value="${value}"
            class="checkbox-input"
            ${checked ? 'checked' : ''}
            ${required ? 'required' : ''}
            ${disabled ? 'disabled' : ''}
            ${onChange ? `onchange="${onChange}"` : ''}
            ${Object.entries(otherProps).map(([key, val]) => `${key}="${val}"`).join(' ')}
            style="
              position: absolute;
              opacity: 0;
              width: 0;
              height: 0;
              &:focus + .checkbox-custom {
                box-shadow: ${tokens.shadows.presets.checkbox.focus};
              }
              &:hover:not(:disabled) + .checkbox-custom {
                border: ${tokens.borders.presets.checkbox.hover};
              }
            "
            ${indeterminate ? 'data-indeterminate="true"' : ''}
          />
          <span 
            class="checkbox-custom" 
            style="${checkboxStyle}"
            ${disabled ? 'aria-disabled="true"' : ''}
          >
            <span class="checkmark" style="${checkmarkStyle}"></span>
            <span class="indeterminate" style="${indeterminateStyle}"></span>
          </span>
          
          <label 
            for="${checkboxId}" 
            style="
              font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
              font-weight: ${tokens.typography.fontWeights.regular};
              color: ${tokens.colors.neutral.gray[800]};
              cursor: ${disabled ? 'not-allowed' : 'pointer'};
              ${required ? 'position: relative;' : ''}
            "
          >
            ${label}
            ${required ? `<span style="color: ${tokens.colors.semantic.error.standard}; margin-left: ${tokens.spacing[1]};">*</span>` : ''}
          </label>
        </div>
      </div>
      
      ${helpTextHtml}
      ${errorTextHtml}
    </div>
  `;
}

// Export the Checkbox component
module.exports = Checkbox;
