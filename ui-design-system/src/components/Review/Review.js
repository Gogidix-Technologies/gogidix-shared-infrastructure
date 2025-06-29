/**
 * Review Component
 * A comprehensive product review component that combines rating and comment functionality
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Rating = require('../Rating');
const { Comment } = require('../Comment');

/**
 * Review component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the review element
 */
function Review({
  id,
  author = {},
  title = '',
  content = '',
  rating = 0,
  maxRating = 5,
  helpfulCount = 0,
  isHelpful = false,
  timestamp = new Date(),
  verifiedPurchase = false,
  images = [],
  locale = localization.DEFAULT_LOCALE,
  onHelpfulClick,
  onReportClick,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const reviewId = id || `review-${Math.random().toString(36).substring(2, 9)}`;
  
  // Format timestamp based on locale
  const formattedTime = localization.formatLocalizedDate(timestamp, locale, {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });

  // Get translations
  const translations = getTranslations(locale);
  
  // Create rating component
  const ratingComponent = Rating({
    value: rating,
    maxValue: maxRating,
    readOnly: true,
    size: 'md',
    locale
  });
  
  // Create verified badge if applicable
  const verifiedBadge = verifiedPurchase ? `
    <span class="verified-badge" style="
      display: inline-block;
      margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.sm};
      padding: ${tokens.spacing.xs} ${tokens.spacing.sm};
      background-color: ${tokens.colors.brand.primary[50]};
      color: ${tokens.colors.brand.primary[700]};
      border-radius: ${tokens.borders.borderRadius.sm};
      font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
      font-weight: ${tokens.typography.fontWeights.medium};
    ">
      <i class="icon-check-circle" style="margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};"></i>
      ${translations.verifiedPurchase}
    </span>
  ` : '';
  
  // Create image gallery if provided
  let imagesHtml = '';
  if (images && images.length > 0) {
    imagesHtml = `
      <div class="review-images" style="
        display: flex;
        flex-wrap: wrap;
        gap: ${tokens.spacing.sm};
        margin-top: ${tokens.spacing.md};
        margin-bottom: ${tokens.spacing.md};
      ">
        ${images.map((image, index) => `
          <div class="review-image" style="
            width: 80px;
            height: 80px;
            border-radius: ${tokens.borders.borderRadius.sm};
            overflow: hidden;
            cursor: pointer;
          " onclick="openImageGallery('${reviewId}', ${index})">
            <img 
              src="${image.thumbnail || image.url}" 
              alt="${translations.reviewImage} ${index + 1}"
              loading="lazy"
              style="
                width: 100%;
                height: 100%;
                object-fit: cover;
              "
            />
          </div>
        `).join('')}
      </div>
    `;
  }
  
  // Create helpful button
  const helpfulButton = `
    <button 
      class="helpful-button ${isHelpful ? 'is-helpful' : ''}" 
      onclick="${onHelpfulClick ? `handleHelpful('${reviewId}')` : ''}"
      style="
        display: inline-flex;
        align-items: center;
        background: none;
        border: 1px solid ${tokens.colors.neutral.gray[300]};
        padding: ${tokens.spacing.xs} ${tokens.spacing.md};
        border-radius: ${tokens.borders.borderRadius.md};
        cursor: pointer;
        font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        color: ${isHelpful ? tokens.colors.brand.primary[600] : tokens.colors.neutral.gray[700]};
        background-color: ${isHelpful ? tokens.colors.brand.primary[50] : 'transparent'};
        ${!onHelpfulClick ? 'pointer-events: none;' : ''}
        
        &:hover {
          background-color: ${isHelpful ? tokens.colors.brand.primary[100] : tokens.colors.neutral.gray[50]};
        }
        
        &:focus {
          outline: none;
          box-shadow: ${tokens.shadows.presets.button.focus};
        }
      "
    >
      <i class="icon-thumb-up" style="margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};"></i>
      ${translations.helpful}
      <span style="
        margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xs};
        font-weight: ${tokens.typography.fontWeights.medium};
        ${helpfulCount === 0 ? 'display: none;' : ''}
      ">
        (${localization.formatLocalizedNumber(helpfulCount, locale)})
      </span>
    </button>
  `;
  
  // Create report button
  const reportButton = onReportClick ? `
    <button 
      class="report-button" 
      onclick="handleReport('${reviewId}')"
      style="
        background: none;
        border: none;
        padding: ${tokens.spacing.xs};
        cursor: pointer;
        font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        color: ${tokens.colors.neutral.gray[600]};
        
        &:hover {
          color: ${tokens.colors.semantic.danger.main};
          text-decoration: underline;
        }
      "
    >
      ${translations.report}
    </button>
  ` : '';
  
  // Build review HTML
  return `
    <div 
      id="${reviewId}" 
      class="review ${className}" 
      dir="${dir}"
      data-review-id="${id}"
      style="
        margin-bottom: ${tokens.spacing.lg};
        padding-bottom: ${tokens.spacing.lg};
        border-bottom: 1px solid ${tokens.colors.neutral.gray[200]};
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div class="review-header" style="
        display: flex;
        align-items: flex-start;
        margin-bottom: ${tokens.spacing.md};
      ">
        <div class="author-avatar" style="
          width: 48px;
          height: 48px;
          border-radius: 50%;
          background-color: ${tokens.colors.neutral.gray[200]};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.md};
          flex-shrink: 0;
          overflow: hidden;
        ">
          <img 
            src="${author.avatarUrl || ''}" 
            alt="${author.name || ''}"
            style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: ${author.avatarUrl ? 'block' : 'none'};
            "
          />
        </div>
        
        <div style="flex-grow: 1;">
          <div style="margin-bottom: ${tokens.spacing.xs};">
            <span class="author-name" style="
              font-weight: ${tokens.typography.fontWeights.medium};
              font-size: ${tokens.typography.textStyles.bodyLarge.fontSize};
            ">
              ${author.name || ''}
            </span>
          </div>
          
          <div style="
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: ${tokens.spacing.sm} ${tokens.spacing.md};
          ">
            ${ratingComponent}
            <span class="review-date" style="
              color: ${tokens.colors.neutral.gray[600]};
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            ">
              ${formattedTime}
            </span>
            ${verifiedBadge}
          </div>
        </div>
      </div>
      
      ${title ? `
        <h3 class="review-title" style="
          margin-top: 0;
          margin-bottom: ${tokens.spacing.sm};
          font-size: ${tokens.typography.textStyles.heading4.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
        ">
          ${title}
        </h3>
      ` : ''}
      
      <div class="review-content" style="
        margin-bottom: ${tokens.spacing.md};
        line-height: ${tokens.typography.lineHeights.relaxed};
      ">
        ${content}
      </div>
      
      ${imagesHtml}
      
      <div class="review-actions" style="
        display: flex;
        align-items: center;
        justify-content: space-between;
        flex-wrap: wrap;
        gap: ${tokens.spacing.sm};
      ">
        <div>
          ${helpfulButton}
        </div>
        <div>
          ${reportButton}
        </div>
      </div>
    </div>
  `;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      verifiedPurchase: 'Verified Purchase',
      helpful: 'Helpful',
      report: 'Report',
      reviewImage: 'Review Image'
    },
    ar: {
      verifiedPurchase: 'شراء موثق',
      helpful: 'مفيد',
      report: 'إبلاغ',
      reviewImage: 'صورة المراجعة'
    },
    fr: {
      verifiedPurchase: 'Achat Vérifié',
      helpful: 'Utile',
      report: 'Signaler',
      reviewImage: 'Image d\'avis'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

module.exports = Review;
