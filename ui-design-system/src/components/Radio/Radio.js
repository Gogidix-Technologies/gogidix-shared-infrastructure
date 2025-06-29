/**
 * Radio Component
 * A versatile radio input component with localization support
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');

/**
 * Radio component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the radio element
 */
function Radio({
  id,
  name,
  label = '',
  helpText = '',
  errorText = '',
  checked = false,
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
  const radioId = id || `radio-${Math.random().toString(36).substring(2, 9)}`;
  
  // Determine if radio is in an error state
  const hasError = !!errorText;
  
  // Custom radio appearance
  const radioStyle = `
    position: relative;
    display: inline-block;
    width: 16px;
    height: 16px;
    margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[2]};
    border: ${hasError 
      ? tokens.borders.presets.radio.error 
      : disabled 
        ? tokens.borders.presets.radio.disabled 
        : tokens.borders.presets.radio.normal
    };
    border-radius: 50%;
    background-color: ${
      disabled
        ? tokens.colors.neutral.gray[100]
        : tokens.colors.neutral.white
    };
    transition: ${tokens.animations.transitions.all};
    flex-shrink: 0;
  `;
  
  // Custom dot indicator
  const dotStyle = `
    position: absolute;
    top: 4px;
    left: 4px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background-color: ${
      disabled
        ? tokens.colors.brand.primary[300]
        : tokens.colors.brand.primary[500]
    };
    display: ${checked ? 'block' : 'none'};
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
  
  // Build radio HTML
  return `
    <div 
      class="radio-container ${className}"
      style="
        margin-bottom: ${tokens.spacing.sm};
        ${hasError ? `color: ${tokens.colors.semantic.error.standard};` : ''}
      "
      dir="${dir}"
    >
      <div 
        class="radio-wrapper"
        style="
          display: flex;
          align-items: center;
          ${disabled ? `opacity: 0.6;` : ''}
        "
      >
        <div style="position: relative; display: flex; align-items: center;">
          <input
            type="radio"
            id="${radioId}"
            name="${name || radioId}"
            value="${value}"
            class="radio-input"
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
              &:focus + .radio-custom {
                box-shadow: ${tokens.shadows.presets.radio.focus};
              }
              &:hover:not(:disabled) + .radio-custom {
                border: ${tokens.borders.presets.radio.hover};
              }
            "
          />
          <span 
            class="radio-custom" 
            style="${radioStyle}"
            ${disabled ? 'aria-disabled="true"' : ''}
          >
            <span class="radio-dot" style="${dotStyle}"></span>
          </span>
          
          <label 
            for="${radioId}" 
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

/**
 * RadioGroup component for generating a group of related radio buttons
 * @param {object} props - Component properties
 * @returns {string} - HTML for the radio group
 */
function RadioGroup({
  id,
  name,
  legend = '',
  options = [],
  value = '',
  layout = 'vertical', // vertical or horizontal
  required = false,
  disabled = false,
  helpText = '',
  errorText = '',
  locale = localization.DEFAULT_LOCALE,
  onChange,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const groupId = id || `radio-group-${Math.random().toString(36).substring(2, 9)}`;
  
  // Determine if group is in an error state
  const hasError = !!errorText;
  
  // Create fieldset legend if provided
  const legendHtml = legend ? `
    <legend 
      style="
        margin-bottom: ${tokens.spacing[3]};
        font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
        font-weight: ${tokens.typography.fontWeights.medium};
        color: ${hasError ? tokens.colors.semantic.error.standard : tokens.colors.neutral.gray[800]};
      "
    >
      ${legend}
      ${required ? `<span style="color: ${tokens.colors.semantic.error.standard}; margin-left: ${tokens.spacing[1]};">*</span>` : ''}
    </legend>
  ` : '';
  
  // Generate radio buttons from options
  const radiosHtml = options.map(option => {
    return Radio({
      id: `${groupId}-${option.value}`,
      name: name || groupId,
      label: option.label,
      value: option.value,
      checked: value === option.value,
      disabled: disabled || option.disabled,
      locale,
      onChange,
      ...otherProps
    });
  }).join('');
  
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
  
  // Build radio group HTML
  return `
    <fieldset 
      id="${groupId}"
      class="radio-group ${className}" 
      dir="${dir}"
      style="
        border: none;
        padding: 0;
        margin: 0 0 ${tokens.spacing.md} 0;
        ${disabled ? `opacity: 0.6;` : ''}
      "
    >
      ${legendHtml}
      
      <div 
        class="radio-options"
        style="
          display: flex;
          flex-direction: ${layout === 'horizontal' ? 'row' : 'column'};
          ${layout === 'horizontal' ? `gap: ${tokens.spacing.md};` : ''}
        "
      >
        ${radiosHtml}
      </div>
      
      ${helpTextHtml}
      ${errorTextHtml}
    </fieldset>
  `;
}

module.exports = { Radio, RadioGroup };
