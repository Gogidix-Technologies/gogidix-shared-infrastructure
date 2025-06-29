/**
 * Rating Component
 * A versatile star rating component for product reviews and feedback
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');

/**
 * Rating component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the rating component
 */
function Rating({
  id,
  value = 0,
  maxValue = 5,
  precision = 0.5,
  size = 'md',
  readOnly = false,
  showValue = false,
  showCount = false,
  count = 0,
  label = '',
  locale = localization.DEFAULT_LOCALE,
  onChange,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const ratingId = id || `rating-${Math.random().toString(36).substring(2, 9)}`;
  
  // Get appropriate styles based on size
  const sizeStyles = getSizeStyles(size);
  const { starSize, spacing } = sizeStyles;
  
  // Round the value to the nearest precision point
  const roundedValue = Math.round(value / precision) * precision;
  
  // Determine how many full, half, and empty stars to render
  const fullStars = Math.floor(roundedValue);
  const hasHalfStar = (roundedValue % 1) !== 0;
  const emptyStars = maxValue - fullStars - (hasHalfStar ? 1 : 0);
  
  // Format the value based on locale
  const formattedValue = localization.formatLocalizedNumber(roundedValue, locale, {
    minimumFractionDigits: precision < 1 ? 1 : 0,
    maximumFractionDigits: precision < 1 ? 1 : 0
  });
  
  // Format the review count based on locale
  const formattedCount = localization.formatLocalizedNumber(count, locale);
  
  // Get translations
  const translations = getTranslations(locale);
  
  // Create stars HTML
  let starsHtml = '';
  
  // Add interactive stars container if not readOnly
  if (!readOnly) {
    starsHtml = `
      <div class="rating-interactive" style="
        display: flex;
        position: relative;
      ">
        <div class="rating-static" style="
          display: flex;
          position: absolute;
          top: 0;
          left: 0;
          pointer-events: none;
        ">
          ${generateStars(fullStars, hasHalfStar, emptyStars, starSize, spacing, dir)}
        </div>
        
        <div class="rating-input" style="
          display: flex;
          position: relative;
          z-index: 1;
        ">
          ${Array.from({ length: maxValue }).map((_, index) => {
            const starValue = index + 1;
            
            return `
              <label
                for="${ratingId}-star-${starValue}"
                style="
                  cursor: pointer;
                  width: ${starSize};
                  height: ${starSize};
                  margin-${dir === 'rtl' ? 'left' : 'right'}: ${spacing};
                  position: relative;
                  
                  &:last-child {
                    margin-${dir === 'rtl' ? 'left' : 'right'}: 0;
                  }
                  
                  &:hover ~ label {
                    color: ${tokens.colors.neutral.gray[400]};
                  }
                "
              >
                <input
                  type="radio"
                  id="${ratingId}-star-${starValue}"
                  name="${ratingId}"
                  value="${starValue}"
                  ${Math.abs(roundedValue - starValue) < 0.1 ? 'checked' : ''}
                  style="
                    position: absolute;
                    opacity: 0;
                    width: 0;
                    height: 0;
                  "
                  ${onChange ? `onchange="${onChange}(${starValue})"` : ''}
                />
                <span style="
                  opacity: 0;
                  position: absolute;
                  top: 0;
                  left: 0;
                  width: 100%;
                  height: 100%;
                ">
                  <svg 
                    viewBox="0 0 24 24" 
                    fill="currentColor" 
                    style="width: 100%; height: 100%;"
                  >
                    <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
                  </svg>
                </span>
              </label>
            `;
          }).join('')}
        </div>
      </div>
    `;
  } else {
    // Static stars for readOnly mode
    starsHtml = `
      <div class="rating-static" style="display: flex;">
        ${generateStars(fullStars, hasHalfStar, emptyStars, starSize, spacing, dir)}
      </div>
    `;
  }
  
  // Create label HTML if provided
  const labelHtml = label ? `
    <label 
      for="${ratingId}" 
      style="
        display: block;
        margin-bottom: ${tokens.spacing[2]};
        font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
        font-weight: ${tokens.typography.fontWeights.medium};
        color: ${tokens.colors.neutral.gray[800]};
      "
    >
      ${label}
    </label>
  ` : '';
  
  // Create rating value HTML if showValue is true
  const valueHtml = showValue ? `
    <span class="rating-value" style="
      margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing[2]};
      font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
      color: ${tokens.colors.neutral.gray[700]};
    ">
      ${formattedValue}
    </span>
  ` : '';
  
  // Create rating count HTML if showCount is true
  const countHtml = showCount && count > 0 ? `
    <span class="rating-count" style="
      margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing[2]};
      font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
      color: ${tokens.colors.neutral.gray[600]};
    ">
      (${formattedCount} ${count === 1 ? translations.review : translations.reviews})
    </span>
  ` : '';
  
  // Build rating component HTML
  return `
    <div 
      class="rating-container ${className}" 
      id="${ratingId}"
      dir="${dir}"
      style="margin-bottom: ${tokens.spacing.sm};"
      data-rating-value="${value}"
      data-rating-max="${maxValue}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${labelHtml}
      
      <div style="display: flex; align-items: center;">
        ${starsHtml}
        ${valueHtml}
        ${countHtml}
      </div>
    </div>
  `;
}

/**
 * Generate stars HTML
 */
function generateStars(fullStars, hasHalfStar, emptyStars, size, spacing, dir) {
  const starsHtml = [];
  
  // Add full stars
  for (let i = 0; i < fullStars; i++) {
    starsHtml.push(`
      <span 
        class="star star-full" 
        style="
          color: ${tokens.colors.semantic.rating.filled};
          width: ${size};
          height: ${size};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${spacing};
          display: inline-block;
          
          &:last-child {
            margin-${dir === 'rtl' ? 'left' : 'right'}: 0;
          }
        "
      >
        <svg 
          viewBox="0 0 24 24" 
          fill="currentColor" 
          style="width: 100%; height: 100%;"
        >
          <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
        </svg>
      </span>
    `);
  }
  
  // Add half star if needed
  if (hasHalfStar) {
    starsHtml.push(`
      <span 
        class="star star-half" 
        style="
          position: relative;
          width: ${size};
          height: ${size};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${spacing};
          display: inline-block;
          
          &:last-child {
            margin-${dir === 'rtl' ? 'left' : 'right'}: 0;
          }
        "
      >
        <span style="
          position: absolute;
          top: 0;
          ${dir === 'rtl' ? 'right' : 'left'}: 0;
          width: 50%;
          height: 100%;
          overflow: hidden;
          color: ${tokens.colors.semantic.rating.filled};
        ">
          <svg 
            viewBox="0 0 24 24" 
            fill="currentColor" 
            style="width: 200%; height: 100%;"
          >
            <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
          </svg>
        </span>
        <span style="
          color: ${tokens.colors.semantic.rating.empty};
        ">
          <svg 
            viewBox="0 0 24 24" 
            fill="currentColor" 
            style="width: 100%; height: 100%;"
          >
            <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
          </svg>
        </span>
      </span>
    `);
  }
  
  // Add empty stars
  for (let i = 0; i < emptyStars; i++) {
    starsHtml.push(`
      <span 
        class="star star-empty" 
        style="
          color: ${tokens.colors.semantic.rating.empty};
          width: ${size};
          height: ${size};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${spacing};
          display: inline-block;
          
          &:last-child {
            margin-${dir === 'rtl' ? 'left' : 'right'}: 0;
          }
        "
      >
        <svg 
          viewBox="0 0 24 24" 
          fill="currentColor" 
          style="width: 100%; height: 100%;"
        >
          <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
        </svg>
      </span>
    `);
  }
  
  return starsHtml.join('');
}

/**
 * Get styles based on rating size
 */
function getSizeStyles(size) {
  const styles = {
    sm: {
      starSize: '16px',
      spacing: '2px'
    },
    md: {
      starSize: '24px',
      spacing: '4px'
    },
    lg: {
      starSize: '32px',
      spacing: '6px'
    }
  };
  
  return styles[size] || styles.md;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      review: 'review',
      reviews: 'reviews'
    },
    ar: {
      review: 'تقييم',
      reviews: 'تقييمات'
    },
    fr: {
      review: 'avis',
      reviews: 'avis'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

module.exports = Rating;
