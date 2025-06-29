/**
 * ProductCard Component
 * A versatile product card component for displaying products in the social commerce platform.
 * Designed for Team Alpha's customer-facing interfaces with social features.
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Button = require('../Button');

/**
 * ProductCard component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the product card
 */
function ProductCard({
  id,
  title,
  description,
  price,
  currency = 'USD',
  discountPercentage = 0,
  rating = 0,
  image = '',
  reviewCount = 0,
  likes = 0,
  shares = 0,
  isWishlisted = false,
  inStock = true,
  badges = [],
  locale = localization.DEFAULT_LOCALE,
  size = 'md',
  onAddToCart,
  onWishlist,
  onShare,
  onClick,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Format the price according to locale and currency
  const formattedPrice = localization.formatLocalizedCurrency(price, currency, locale);
  
  // Calculate discount price if applicable
  const discountedPrice = discountPercentage > 0 
    ? price - (price * (discountPercentage / 100)) 
    : null;
  
  // Format the discounted price if applicable
  const formattedDiscountedPrice = discountedPrice 
    ? localization.formatLocalizedCurrency(discountedPrice, currency, locale) 
    : null;
  
  // Get appropriate styles based on props
  const sizeStyles = getSizeStyles(size);
  
  // Generate star rating HTML
  const ratingHtml = generateRatingStars(rating);
  
  // Generate badges HTML
  const badgesHtml = generateBadges(badges, dir);
  
  // Create wishlist button
  const wishlistButton = Button({
    children: `<i class="icon-heart${isWishlisted ? '-filled' : ''}"></i>`,
    variant: 'secondary',
    size: 'sm',
    locale,
    onClick: `handleWishlist('${id}')`,
    className: 'wishlist-button'
  });
  
  // Create share button
  const shareButton = Button({
    children: `<i class="icon-share"></i>`,
    variant: 'secondary',
    size: 'sm',
    locale,
    onClick: `handleShare('${id}')`,
    className: 'share-button'
  });
  
  // Create add to cart button
  const addToCartButton = Button({
    children: inStock 
      ? (dir === 'rtl' ? 'إضافة إلى السلة' : 'Add to Cart')
      : (dir === 'rtl' ? 'غير متوفر' : 'Out of Stock'),
    variant: inStock ? 'primary' : 'secondary',
    size: 'md',
    disabled: !inStock,
    locale,
    onClick: inStock ? `handleAddToCart('${id}')` : '',
    className: 'add-to-cart-button'
  });
  
  // Build product card HTML
  return `
    <div 
      class="product-card ${className}" 
      dir="${dir}"
      data-product-id="${id}"
      data-locale="${locale}"
      style="
        position: relative;
        display: flex;
        flex-direction: column;
        width: 100%;
        ${sizeStyles}
        border: ${tokens.borders.presets.card.default};
        border-radius: ${tokens.borders.borderRadius.md};
        background-color: ${tokens.colors.neutral.white};
        box-shadow: ${tokens.shadows.presets.card.default};
        transition: transform 0.2s ease, box-shadow 0.2s ease;
        overflow: hidden;
        
        &:hover {
          transform: translateY(-4px);
          box-shadow: ${tokens.shadows.presets.card.hover};
        }
      "
      onclick="${onClick ? `${onClick}('${id}')` : ''}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <!-- Image Container -->
      <div class="product-image-container" style="
        position: relative;
        aspect-ratio: 1;
        width: 100%;
        overflow: hidden;
        background-color: ${tokens.colors.neutral.gray[100]};
      ">
        <img 
          src="${image}" 
          alt="${title}"
          style="
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.3s ease;
            
            &:hover {
              transform: scale(1.05);
            }
          "
        />
        
        <!-- Badges -->
        ${badgesHtml}
        
        <!-- Actions -->
        <div class="product-actions" style="
          position: absolute;
          top: ${tokens.spacing.sm};
          ${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.sm};
          display: flex;
          flex-direction: column;
          gap: ${tokens.spacing.xs};
        ">
          ${wishlistButton}
          ${shareButton}
        </div>
      </div>
      
      <!-- Content -->
      <div class="product-content" style="
        padding: ${tokens.spacing.md};
        display: flex;
        flex-direction: column;
        flex-grow: 1;
      ">
        <!-- Title -->
        <h3 style="
          margin: 0 0 ${tokens.spacing.xs} 0;
          font-family: ${localization.getLocalizedFontFamily(locale, tokens.typography)};
          font-size: ${tokens.typography.textStyles.h6.fontSize};
          font-weight: ${tokens.typography.textStyles.h6.fontWeight};
          line-height: ${tokens.typography.textStyles.h6.lineHeight};
          color: ${tokens.colors.neutral.gray[900]};
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        ">
          ${title}
        </h3>
        
        <!-- Rating and Reviews -->
        <div class="product-rating" style="
          display: flex;
          align-items: center;
          margin-bottom: ${tokens.spacing.xs};
          color: ${tokens.colors.neutral.gray[600]};
          font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        ">
          ${ratingHtml}
          <span style="margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xs};">
            (${reviewCount})
          </span>
        </div>
        
        <!-- Description -->
        <p style="
          margin: 0 0 ${tokens.spacing.sm} 0;
          color: ${tokens.colors.neutral.gray[700]};
          font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
          line-height: ${tokens.typography.lineHeights.normal};
          flex-grow: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        ">
          ${description}
        </p>
        
        <!-- Price -->
        <div class="product-price" style="
          margin-bottom: ${tokens.spacing.md};
          display: flex;
          align-items: baseline;
          flex-wrap: wrap;
        ">
          ${discountedPrice ? `
            <span style="
              color: ${tokens.colors.semantic.error.standard};
              font-size: ${tokens.typography.textStyles.bodyLarge.fontSize};
              font-weight: ${tokens.typography.fontWeights.bold};
              margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
            ">
              ${formattedDiscountedPrice}
            </span>
            
            <span style="
              color: ${tokens.colors.neutral.gray[500]};
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              text-decoration: line-through;
            ">
              ${formattedPrice}
            </span>
            
            <span style="
              margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xs};
              background-color: ${tokens.colors.semantic.error.light};
              color: ${tokens.colors.semantic.error.standard};
              padding: ${tokens.spacing[1]} ${tokens.spacing[2]};
              border-radius: ${tokens.borders.borderRadius.sm};
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              font-weight: ${tokens.typography.fontWeights.medium};
            ">
              -${discountPercentage}%
            </span>
          ` : `
            <span style="
              color: ${tokens.colors.neutral.gray[900]};
              font-size: ${tokens.typography.textStyles.bodyLarge.fontSize};
              font-weight: ${tokens.typography.fontWeights.bold};
            ">
              ${formattedPrice}
            </span>
          `}
        </div>
        
        <!-- Social Stats -->
        <div class="product-social-stats" style="
          display: flex;
          gap: ${tokens.spacing.md};
          margin-bottom: ${tokens.spacing.md};
          color: ${tokens.colors.neutral.gray[600]};
          font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        ">
          <div class="likes" style="display: flex; align-items: center;">
            <i class="icon-heart" style="margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[1]};"></i>
            ${localization.formatLocalizedNumber(likes, locale)}
          </div>
          <div class="shares" style="display: flex; align-items: center;">
            <i class="icon-share" style="margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[1]};"></i>
            ${localization.formatLocalizedNumber(shares, locale)}
          </div>
        </div>
        
        <!-- Add to Cart Button -->
        ${addToCartButton}
      </div>
    </div>
  `;
}

/**
 * Get styles based on card size
 */
function getSizeStyles(size) {
  const sizes = {
    sm: `
      max-width: 200px;
    `,
    md: `
      max-width: 280px;
    `,
    lg: `
      max-width: 350px;
    `,
    fluid: `
      max-width: none;
    `
  };
  
  return sizes[size] || sizes.md;
}

/**
 * Generate HTML for star ratings
 */
function generateRatingStars(rating) {
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.5;
  const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
  
  let starsHtml = '';
  
  // Add full stars
  for (let i = 0; i < fullStars; i++) {
    starsHtml += '<i class="icon-star-filled" style="color: #faad14;"></i>';
  }
  
  // Add half star if applicable
  if (hasHalfStar) {
    starsHtml += '<i class="icon-star-half" style="color: #faad14;"></i>';
  }
  
  // Add empty stars
  for (let i = 0; i < emptyStars; i++) {
    starsHtml += '<i class="icon-star-empty" style="color: #d9d9d9;"></i>';
  }
  
  return starsHtml;
}

/**
 * Generate HTML for product badges
 */
function generateBadges(badges, dir) {
  if (!badges || badges.length === 0) return '';
  
  const badgesHtml = badges.map(badge => {
    let bgColor = tokens.colors.neutral.gray[800];
    let textColor = tokens.colors.neutral.white;
    
    // Set colors based on badge type
    if (badge.type === 'new') {
      bgColor = tokens.colors.semantic.info.standard;
    } else if (badge.type === 'sale') {
      bgColor = tokens.colors.semantic.error.standard;
    } else if (badge.type === 'featured') {
      bgColor = tokens.colors.brand.accent[500];
    } else if (badge.type === 'limited') {
      bgColor = tokens.colors.brand.secondary[500];
    }
    
    return `
      <div style="
        background-color: ${bgColor};
        color: ${textColor};
        padding: ${tokens.spacing[1]} ${tokens.spacing[2]};
        border-radius: ${tokens.borders.borderRadius.sm};
        font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
        font-weight: ${tokens.typography.fontWeights.medium};
        margin-bottom: ${tokens.spacing.xs};
      ">
        ${badge.label}
      </div>
    `;
  }).join('');
  
  return `
    <div class="product-badges" style="
      position: absolute;
      top: ${tokens.spacing.sm};
      ${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.sm};
      display: flex;
      flex-direction: column;
      align-items: flex-start;
    ">
      ${badgesHtml}
    </div>
  `;
}

module.exports = ProductCard;
