/**
 * ProductDetail Component
 * A comprehensive product detail page template integrating product information,
 * social features, and commerce functionality
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Button = require('../Button');
const Rating = require('../Rating');
const { Comment, CommentForm } = require('../Comment');
const Review = require('../Review');

/**
 * ProductDetail component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the product detail element
 */
function ProductDetail({
  id,
  product = {},
  locale = localization.DEFAULT_LOCALE,
  onAddToCart,
  onShare,
  onSaveForLater,
  onReviewSubmit,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const productDetailId = id || `product-detail-${Math.random().toString(36).substring(2, 9)}`;
  
  // Get translations
  const translations = getTranslations(locale);
  
  // Parse product information
  const {
    title = '',
    description = '',
    price = 0,
    compareAtPrice,
    discount,
    currency = 'USD',
    images = [],
    variants = [],
    options = [],
    ratings = { average: 0, count: 0 },
    reviews = [],
    inStock = true,
    stockLevel,
    sku = '',
    brand = {},
    categories = [],
    tags = [],
    shipping = {},
    returnsPolicy = '',
    socialMetrics = { likes: 0, shares: 0, views: 0 }
  } = product;
  // Format prices based on locale and currency
  const formattedPrice = localization.formatLocalizedCurrency(price, currency, locale);
  const formattedCompareAtPrice = compareAtPrice ? localization.formatLocalizedCurrency(compareAtPrice, currency, locale) : '';
  const formattedDiscount = discount ? `${localization.formatLocalizedNumber(discount, locale)}%` : '';
  
  // Create rating component
  const ratingComponent = Rating({
    value: ratings.average,
    maxValue: 5,
    readOnly: true,
    showValue: true,
    showCount: true,
    count: ratings.count,
    locale
  });
  
  // Create image gallery with thumbnails
  const mainImageUrl = images && images.length > 0 ? images[0].large || images[0].url : '';
  let imageGalleryHtml = '';
  
  if (images && images.length > 0) {
    const thumbnailsHtml = images.map((image, index) => `
      <div 
        class="product-thumbnail ${index === 0 ? 'active' : ''}" 
        onclick="switchProductImage('${productDetailId}', ${index})"
        data-image-index="${index}"
        style="
          width: 60px;
          height: 60px;
          border: 2px solid ${index === 0 ? tokens.colors.brand.primary[500] : tokens.colors.neutral.gray[300]};
          border-radius: ${tokens.borders.borderRadius.md};
          margin-bottom: ${tokens.spacing.sm};
          cursor: pointer;
          overflow: hidden;
          
          &:hover {
            border-color: ${tokens.colors.brand.primary[300]};
          }
        "
      >
        <img 
          src="${image.thumbnail || image.url}" 
          alt="${title} ${index + 1}"
          loading="${index < 4 ? 'eager' : 'lazy'}"
          style="
            width: 100%;
            height: 100%;
            object-fit: cover;
          "
        />
      </div>
    `).join('');
    
    imageGalleryHtml = `
      <div class="product-image-gallery" style="
        display: flex;
        margin-bottom: ${tokens.spacing.lg};
      ">
        <div class="product-thumbnails" style="
          display: flex;
          flex-direction: column;
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.md};
        ">
          ${thumbnailsHtml}
        </div>
        
        <div class="product-main-image" style="
          flex-grow: 1;
          position: relative;
          min-height: 400px;
          border-radius: ${tokens.borders.borderRadius.lg};
          overflow: hidden;
          background-color: ${tokens.colors.neutral.gray[100]};
        ">
          <img 
            id="${productDetailId}-main-image"
            src="${mainImageUrl}" 
            alt="${title}"
            style="
              width: 100%;
              height: 100%;
              object-fit: contain;
            "
          />
          
          ${!inStock ? `
            <div class="out-of-stock-overlay" style="
              position: absolute;
              top: ${tokens.spacing.md};
              ${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.md};
              background-color: ${tokens.colors.semantic.danger.dark};
              color: white;
              padding: ${tokens.spacing.xs} ${tokens.spacing.md};
              border-radius: ${tokens.borders.borderRadius.md};
              font-weight: ${tokens.typography.fontWeights.medium};
            ">
              ${translations.outOfStock}
            </div>
          ` : ''}
          
          ${discount ? `
            <div class="discount-badge" style="
              position: absolute;
              top: ${tokens.spacing.md};
              ${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.md};
              background-color: ${tokens.colors.semantic.success.main};
              color: white;
              padding: ${tokens.spacing.xs} ${tokens.spacing.md};
              border-radius: ${tokens.borders.borderRadius.md};
              font-weight: ${tokens.typography.fontWeights.medium};
            ">
              -${formattedDiscount}
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }
  // Create product options/variants selectors
  let optionsHtml = '';
  if (options && options.length > 0) {
    optionsHtml = options.map(option => {
      const optionId = `${productDetailId}-option-${option.name.toLowerCase().replace(/\s+/g, '-')}`;
      
      let optionControlsHtml = '';
      if (option.type === 'color' || option.type === 'swatch') {
        // Color/swatch selector
        optionControlsHtml = `
          <div class="option-swatches" style="
            display: flex;
            flex-wrap: wrap;
            gap: ${tokens.spacing.sm};
            margin-top: ${tokens.spacing.sm};
          ">
            ${option.values.map((value, idx) => `
              <label 
                for="${optionId}-${idx}"
                class="color-swatch ${value.available === false ? 'disabled' : ''}"
                style="
                  width: 40px;
                  height: 40px;
                  border-radius: 50%;
                  background-color: ${value.color || tokens.colors.neutral.gray[300]};
                  cursor: ${value.available === false ? 'not-allowed' : 'pointer'};
                  position: relative;
                  overflow: hidden;
                  border: 2px solid ${value.selected ? tokens.colors.brand.primary[500] : 'transparent'};
                  opacity: ${value.available === false ? '0.4' : '1'};
                  
                  &:hover {
                    border-color: ${value.available === false ? 'transparent' : tokens.colors.brand.primary[300]};
                  }
                "
              >
                <input 
                  type="radio" 
                  id="${optionId}-${idx}" 
                  name="${optionId}" 
                  value="${value.value}"
                  ${value.selected ? 'checked' : ''}
                  ${value.available === false ? 'disabled' : ''}
                  onchange="updateProductVariant('${productDetailId}', '${option.name}', '${value.value}')"
                  style="
                    opacity: 0;
                    position: absolute;
                    width: 0;
                    height: 0;
                  "
                />
                <span class="swatch-inner" style="
                  display: block;
                  width: 100%;
                  height: 100%;
                  background-image: ${value.image ? `url(${value.image})` : 'none'};
                  background-size: cover;
                  background-position: center;
                "></span>
                <span class="swatch-tooltip" style="
                  position: absolute;
                  bottom: -30px;
                  left: 50%;
                  transform: translateX(-50%);
                  background: ${tokens.colors.neutral.gray[900]};
                  color: white;
                  padding: ${tokens.spacing.xs} ${tokens.spacing.sm};
                  border-radius: ${tokens.borders.borderRadius.sm};
                  font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
                  white-space: nowrap;
                  opacity: 0;
                  visibility: hidden;
                  transition: all 0.2s ease;
                  z-index: 1;
                  pointer-events: none;
                  
                  &::before {
                    content: '';
                    position: absolute;
                    top: -4px;
                    left: 50%;
                    transform: translateX(-50%) rotate(45deg);
                    width: 8px;
                    height: 8px;
                    background: ${tokens.colors.neutral.gray[900]};
                  }
                ">
                  ${value.value}
                </span>
              </label>
            `).join('')}
          </div>
        `;
      } else {
        // Default select dropdown
        optionControlsHtml = `
          <select 
            id="${optionId}"
            class="option-select"
            onchange="updateProductVariant('${productDetailId}', '${option.name}', this.value)"
            style="
              width: 100%;
              padding: ${tokens.spacing.sm} ${tokens.spacing.md};
              border: ${tokens.borders.presets.input.normal};
              border-radius: ${tokens.borders.borderRadius.md};
              margin-top: ${tokens.spacing.sm};
              font-family: inherit;
              appearance: none;
              background-image: url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><polyline points=\"6 9 12 15 18 9\"></polyline></svg>');
              background-repeat: no-repeat;
              background-position: right ${tokens.spacing.md} center;
              background-size: 16px;
            "
          >
            ${option.values.map(value => `
              <option 
                value="${value.value}" 
                ${value.selected ? 'selected' : ''}
                ${value.available === false ? 'disabled' : ''}
              >
                ${value.value} ${value.available === false ? `(${translations.outOfStock})` : ''}
              </option>
            `).join('')}
          </select>
        `;
      }
      
      return `
        <div class="product-option" style="margin-bottom: ${tokens.spacing.md};">
          <label style="
            display: block;
            font-weight: ${tokens.typography.fontWeights.medium};
            margin-bottom: ${tokens.spacing.xs};
          ">
            ${option.name}
          </label>
          ${optionControlsHtml}
        </div>
      `;
    }).join('');
  }
  
  // Create stock indicator
  let stockIndicatorHtml = '';
  if (stockLevel !== undefined) {
    let stockColor = tokens.colors.semantic.success.main;
    let stockText = translations.inStock;
    
    if (stockLevel <= 0) {
      stockColor = tokens.colors.semantic.danger.main;
      stockText = translations.outOfStock;
    } else if (stockLevel < 5) {
      stockColor = tokens.colors.semantic.warning.main;
      stockText = translations.lowStock.replace('{count}', stockLevel);
    }
    
    stockIndicatorHtml = `
      <div class="stock-indicator" style="
        display: flex;
        align-items: center;
        margin-bottom: ${tokens.spacing.md};
      ">
        <div class="stock-dot" style="
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: ${stockColor};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
        "></div>
        <span style="
          font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
          color: ${stockColor};
        ">
          ${stockText}
        </span>
      </div>
    `;
  } else if (inStock !== undefined) {
    stockIndicatorHtml = `
      <div class="stock-indicator" style="
        display: flex;
        align-items: center;
        margin-bottom: ${tokens.spacing.md};
      ">
        <div class="stock-dot" style="
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: ${inStock ? tokens.colors.semantic.success.main : tokens.colors.semantic.danger.main};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
        "></div>
        <span style="
          font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
          color: ${inStock ? tokens.colors.semantic.success.main : tokens.colors.semantic.danger.main};
        ">
          ${inStock ? translations.inStock : translations.outOfStock}
        </span>
      </div>
    `;
  }
  // Create social interactions section
  const socialMetricsHtml = `
    <div class="social-metrics" style="
      display: flex;
      gap: ${tokens.spacing.lg};
      margin-top: ${tokens.spacing.md};
      padding-top: ${tokens.spacing.md};
      border-top: 1px solid ${tokens.colors.neutral.gray[200]};
    ">
      <div class="metric" style="display: flex; align-items: center;">
        <i class="icon-heart" style="
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
          color: ${tokens.colors.neutral.gray[600]};
        "></i>
        <span style="font-size: ${tokens.typography.textStyles.bodySmall.fontSize};">
          ${localization.formatLocalizedNumber(socialMetrics.likes || 0, locale)}
        </span>
      </div>
      
      <div class="metric" style="display: flex; align-items: center;">
        <i class="icon-share" style="
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
          color: ${tokens.colors.neutral.gray[600]};
        "></i>
        <span style="font-size: ${tokens.typography.textStyles.bodySmall.fontSize};">
          ${localization.formatLocalizedNumber(socialMetrics.shares || 0, locale)}
        </span>
      </div>
      
      <div class="metric" style="display: flex; align-items: center;">
        <i class="icon-eye" style="
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
          color: ${tokens.colors.neutral.gray[600]};
        "></i>
        <span style="font-size: ${tokens.typography.textStyles.bodySmall.fontSize};">
          ${localization.formatLocalizedNumber(socialMetrics.views || 0, locale)}
        </span>
      </div>
    </div>
  `;
  
  // Create action buttons
  const addToCartButton = Button({
    children: translations.addToCart,
    variant: 'primary',
    size: 'lg',
    onClick: onAddToCart ? `addToCart('${productDetailId}')` : '',
    disabled: !inStock,
    className: 'add-to-cart-button',
    style: 'width: 100%;',
    locale
  });
  
  const saveForLaterButton = Button({
    children: `<i class="icon-bookmark"></i> ${translations.saveForLater}`,
    variant: 'secondary',
    size: 'lg',
    onClick: onSaveForLater ? `saveForLater('${productDetailId}')` : '',
    className: 'save-for-later-button',
    style: 'width: 100%; margin-top: ' + tokens.spacing.md,
    locale
  });
  
  const shareButton = Button({
    children: `<i class="icon-share"></i> ${translations.share}`,
    variant: 'text',
    size: 'md',
    onClick: onShare ? `shareProduct('${productDetailId}')` : '',
    className: 'share-button',
    style: 'width: 100%; margin-top: ' + tokens.spacing.md,
    locale
  });
  
  // Create reviews section
  let reviewsHtml = '';
  if (reviews && reviews.length > 0) {
    const reviewsList = reviews.map(review => Review({
      ...review,
      locale
    })).join('');
    
    reviewsHtml = `
      <div class="reviews-section" style="margin-top: ${tokens.spacing.xl};">
        <h2 style="
          font-size: ${tokens.typography.textStyles.heading3.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
          margin-bottom: ${tokens.spacing.md};
        ">
          ${translations.customerReviews}
          <span style="color: ${tokens.colors.neutral.gray[600]}; font-size: 0.9em;">
            (${localization.formatLocalizedNumber(reviews.length, locale)})
          </span>
        </h2>
        
        <div class="rating-summary" style="
          display: flex;
          align-items: center;
          margin-bottom: ${tokens.spacing.lg};
        ">
          <div style="
            font-size: 3rem;
            font-weight: ${tokens.typography.fontWeights.semiBold};
            margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.lg};
            line-height: 1;
          ">
            ${localization.formatLocalizedNumber(ratings.average, locale, { minimumFractionDigits: 1, maximumFractionDigits: 1 })}
          </div>
          
          <div>
            ${Rating({
              value: ratings.average,
              maxValue: 5,
              readOnly: true,
              size: 'lg',
              locale
            })}
            <div style="
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              color: ${tokens.colors.neutral.gray[600]};
              margin-top: ${tokens.spacing.xs};
            ">
              ${localization.formatLocalizedNumber(ratings.count, locale)} ${ratings.count === 1 ? translations.review : translations.reviews}
            </div>
          </div>
        </div>
        
        <div class="reviews-list">
          ${reviewsList}
        </div>
        
        <div class="write-review" style="margin-top: ${tokens.spacing.xl};">
          <h3 style="
            font-size: ${tokens.typography.textStyles.heading4.fontSize};
            font-weight: ${tokens.typography.fontWeights.semiBold};
            margin-bottom: ${tokens.spacing.md};
          ">
            ${translations.writeReview}
          </h3>
          
          ${onReviewSubmit ? `
            <div>
              <div style="margin-bottom: ${tokens.spacing.md};">
                <label style="
                  display: block;
                  font-weight: ${tokens.typography.fontWeights.medium};
                  margin-bottom: ${tokens.spacing.xs};
                ">
                  ${translations.yourRating}
                </label>
                
                ${Rating({
                  value: 0,
                  maxValue: 5,
                  readOnly: false,
                  size: 'md',
                  onChange: `setReviewRating('${productDetailId}')`,
                  locale
                })}
              </div>
              
              <div style="margin-bottom: ${tokens.spacing.md};">
                <label style="
                  display: block;
                  font-weight: ${tokens.typography.fontWeights.medium};
                  margin-bottom: ${tokens.spacing.xs};
                " for="${productDetailId}-review-title">
                  ${translations.reviewTitle}
                </label>
                
                <input 
                  type="text" 
                  id="${productDetailId}-review-title"
                  placeholder="${translations.reviewTitlePlaceholder}"
                  style="
                    width: 100%;
                    padding: ${tokens.spacing.sm} ${tokens.spacing.md};
                    border: ${tokens.borders.presets.input.normal};
                    border-radius: ${tokens.borders.borderRadius.md};
                    font-family: inherit;
                    font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
                    
                    &:focus {
                      border: ${tokens.borders.presets.input.focus};
                      box-shadow: ${tokens.shadows.presets.input.focus};
                      outline: none;
                    }
                  "
                />
              </div>
              
              <div style="margin-bottom: ${tokens.spacing.md};">
                <label style="
                  display: block;
                  font-weight: ${tokens.typography.fontWeights.medium};
                  margin-bottom: ${tokens.spacing.xs};
                " for="${productDetailId}-review-content">
                  ${translations.reviewContent}
                </label>
                
                <textarea 
                  id="${productDetailId}-review-content"
                  placeholder="${translations.reviewContentPlaceholder}"
                  style="
                    width: 100%;
                    min-height: 120px;
                    padding: ${tokens.spacing.sm} ${tokens.spacing.md};
                    border: ${tokens.borders.presets.input.normal};
                    border-radius: ${tokens.borders.borderRadius.md};
                    font-family: inherit;
                    font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
                    resize: vertical;
                    
                    &:focus {
                      border: ${tokens.borders.presets.input.focus};
                      box-shadow: ${tokens.shadows.presets.input.focus};
                      outline: none;
                    }
                  "
                ></textarea>
              </div>
              
              <div style="text-align: ${dir === 'rtl' ? 'left' : 'right'};">
                ${Button({
                  children: translations.submitReview,
                  variant: 'primary',
                  size: 'md',
                  onClick: `submitReview('${productDetailId}')`,
                  locale
                })}
              </div>
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }
  // Build product detail HTML
  return `
    <div 
      id="${productDetailId}" 
      class="product-detail ${className}" 
      dir="${dir}"
      data-product-id="${id}"
      style="
        max-width: 1200px;
        margin: 0 auto;
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div style="
        display: flex;
        flex-wrap: wrap;
        gap: ${tokens.spacing.xl};
      ">
        <div class="product-images" style="
          flex: 1 1 500px;
        ">
          ${imageGalleryHtml}
        </div>
        
        <div class="product-info" style="
          flex: 1 1 400px;
        ">
          <div>
            ${brand.name ? `
              <div class="product-brand" style="
                margin-bottom: ${tokens.spacing.sm};
                font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
                color: ${tokens.colors.brand.primary[600]};
                font-weight: ${tokens.typography.fontWeights.medium};
              ">
                ${brand.name}
              </div>
            ` : ''}
            
            <h1 class="product-title" style="
              margin-top: 0;
              margin-bottom: ${tokens.spacing.sm};
              font-size: ${tokens.typography.textStyles.heading1.fontSize};
              font-weight: ${tokens.typography.fontWeights.semiBold};
              line-height: ${tokens.typography.lineHeights.tight};
            ">
              ${title}
            </h1>
            
            <div style="margin-bottom: ${tokens.spacing.md};">
              ${ratingComponent}
            </div>
            
            <div class="product-price" style="
              margin-bottom: ${tokens.spacing.md};
              font-size: ${tokens.typography.textStyles.heading3.fontSize};
              font-weight: ${tokens.typography.fontWeights.semiBold};
              display: flex;
              align-items: center;
              flex-wrap: wrap;
              gap: ${tokens.spacing.sm};
            ">
              <span style="
                color: ${discount ? tokens.colors.semantic.success.dark : tokens.colors.neutral.gray[900]};
              ">
                ${formattedPrice}
              </span>
              
              ${compareAtPrice ? `
                <span style="
                  text-decoration: line-through;
                  color: ${tokens.colors.neutral.gray[500]};
                  font-size: 0.8em;
                ">
                  ${formattedCompareAtPrice}
                </span>
              ` : ''}
            </div>
            
            ${stockIndicatorHtml}
            
            <div class="product-description" style="
              margin-bottom: ${tokens.spacing.lg};
              line-height: ${tokens.typography.lineHeights.relaxed};
              color: ${tokens.colors.neutral.gray[800]};
            ">
              ${description}
            </div>
            
            <div class="product-options">
              ${optionsHtml}
            </div>
            
            <div class="product-actions" style="
              margin-top: ${tokens.spacing.lg};
            ">
              ${addToCartButton}
              ${saveForLaterButton}
              ${shareButton}
            </div>
            
            ${socialMetricsHtml}
            
            ${sku ? `
              <div class="product-sku" style="
                margin-top: ${tokens.spacing.lg};
                font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
                color: ${tokens.colors.neutral.gray[600]};
              ">
                ${translations.sku}: ${sku}
              </div>
            ` : ''}
            
            ${categories && categories.length > 0 ? `
              <div class="product-categories" style="
                margin-top: ${tokens.spacing.sm};
                font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
                color: ${tokens.colors.neutral.gray[600]};
              ">
                ${translations.categories}: ${categories.map(category => `
                  <a 
                    href="#" 
                    onclick="navigateToCategory('${category.id || category}')"
                    style="
                      color: ${tokens.colors.brand.primary[600]};
                      text-decoration: none;
                      
                      &:hover {
                        text-decoration: underline;
                      }
                    "
                  >
                    ${category.name || category}
                  </a>
                `).join(', ')}
              </div>
            ` : ''}
            
            ${tags && tags.length > 0 ? `
              <div class="product-tags" style="
                margin-top: ${tokens.spacing.sm};
                font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              ">
                ${tags.map(tag => `
                  <a 
                    href="#" 
                    onclick="navigateToTag('${tag.id || tag}')"
                    style="
                      display: inline-block;
                      padding: ${tokens.spacing.xs} ${tokens.spacing.sm};
                      background-color: ${tokens.colors.neutral.gray[100]};
                      color: ${tokens.colors.neutral.gray[700]};
                      border-radius: ${tokens.borders.borderRadius.full};
                      margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
                      margin-bottom: ${tokens.spacing.xs};
                      text-decoration: none;
                      
                      &:hover {
                        background-color: ${tokens.colors.neutral.gray[200]};
                      }
                    "
                  >
                    ${tag.name || tag}
                  </a>
                `).join('')}
              </div>
            ` : ''}
          </div>
        </div>
      </div>
      
      ${shipping || returnsPolicy ? `
        <div class="product-details" style="
          margin-top: ${tokens.spacing.xl};
          padding-top: ${tokens.spacing.lg};
          border-top: 1px solid ${tokens.colors.neutral.gray[200]};
        ">
          <h2 style="
            font-size: ${tokens.typography.textStyles.heading3.fontSize};
            font-weight: ${tokens.typography.fontWeights.semiBold};
            margin-bottom: ${tokens.spacing.md};
          ">
            ${translations.shippingInfo}
          </h2>
          
          ${shipping ? `
            <div class="shipping-info" style="
              margin-bottom: ${tokens.spacing.lg};
            ">
              <div style="
                display: flex;
                align-items: flex-start;
                margin-bottom: ${tokens.spacing.sm};
                gap: ${tokens.spacing.md};
              ">
                <i class="icon-truck" style="
                  font-size: 1.5em;
                  color: ${tokens.colors.neutral.gray[600]};
                  margin-top: 0.2em;
                "></i>
                <div>
                  ${shipping.freeShipping ? `
                    <div style="font-weight: ${tokens.typography.fontWeights.medium};">
                      ${translations.freeShipping}
                    </div>
                  ` : ''}
                  
                  ${shipping.deliveryEstimate ? `
                    <div style="margin-top: ${tokens.spacing.xs};">
                      ${translations.deliveryEstimate}: ${shipping.deliveryEstimate}
                    </div>
                  ` : ''}
                  
                  ${shipping.details ? `
                    <div style="
                      margin-top: ${tokens.spacing.xs};
                      color: ${tokens.colors.neutral.gray[700]};
                    ">
                      ${shipping.details}
                    </div>
                  ` : ''}
                </div>
              </div>
            </div>
          ` : ''}
          
          ${returnsPolicy ? `
            <div class="returns-policy" style="
              margin-bottom: ${tokens.spacing.lg};
            ">
              <div style="
                display: flex;
                align-items: flex-start;
                gap: ${tokens.spacing.md};
              ">
                <i class="icon-refresh" style="
                  font-size: 1.5em;
                  color: ${tokens.colors.neutral.gray[600]};
                  margin-top: 0.2em;
                "></i>
                <div>
                  <div style="font-weight: ${tokens.typography.fontWeights.medium};">
                    ${translations.returnsPolicy}
                  </div>
                  <div style="
                    margin-top: ${tokens.spacing.xs};
                    color: ${tokens.colors.neutral.gray[700]};
                  ">
                    ${returnsPolicy}
                  </div>
                </div>
              </div>
            </div>
          ` : ''}
        </div>
      ` : ''}
      
      ${reviewsHtml}
    </div>
    
    <script>
      // Product detail interactions
      function switchProductImage(productId, imageIndex) {
        const thumbnails = document.querySelectorAll(\`#\${productId} .product-thumbnail\`);
        const mainImage = document.getElementById(\`\${productId}-main-image\`);
        const images = ${JSON.stringify(images)};
        
        // Update main image
        if (images && images[imageIndex]) {
          mainImage.src = images[imageIndex].large || images[imageIndex].url;
        }
        
        // Update thumbnails active state
        thumbnails.forEach(thumbnail => {
          if (parseInt(thumbnail.dataset.imageIndex) === imageIndex) {
            thumbnail.style.borderColor = '${tokens.colors.brand.primary[500]}';
          } else {
            thumbnail.style.borderColor = '${tokens.colors.neutral.gray[300]}';
          }
        });
      }
      
      function updateProductVariant(productId, optionName, value) {
        // This would typically update the UI and selected variant
        console.log('Update variant', productId, optionName, value);
        
        // Here you would normally:
        // 1. Find the matching variant based on all currently selected options
        // 2. Update the price, availability, and product images
        // 3. Enable/disable the add to cart button based on availability
      }
      
      // Hook up event handlers
      ${onAddToCart ? `
        function addToCart(productId) {
          // Custom handler would be injected here
          console.log('Add to cart', productId);
        }
      ` : ''}
      
      ${onShare ? `
        function shareProduct(productId) {
          // Custom handler would be injected here
          console.log('Share product', productId);
        }
      ` : ''}
      
      ${onSaveForLater ? `
        function saveForLater(productId) {
          // Custom handler would be injected here
          console.log('Save for later', productId);
        }
      ` : ''}
      
      ${onReviewSubmit ? `
        function setReviewRating(productId, rating) {
          // Custom handler would be injected here
          console.log('Set review rating', productId, rating);
        }
        
        function submitReview(productId) {
          const title = document.getElementById(\`\${productId}-review-title\`).value;
          const content = document.getElementById(\`\${productId}-review-content\`).value;
          // Get rating from the UI (implementation depends on the Rating component)
          
          // Custom handler would be injected here
          console.log('Submit review', productId, title, content);
        }
      ` : ''}
      
      function navigateToCategory(categoryId) {
        // Implementation for category navigation
        console.log('Navigate to category', categoryId);
        return false; // Prevent default link behavior
      }
      
      function navigateToTag(tagId) {
        // Implementation for tag navigation
        console.log('Navigate to tag', tagId);
        return false; // Prevent default link behavior
      }
    </script>
  `;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      outOfStock: 'Out of Stock',
      inStock: 'In Stock',
      lowStock: 'Only {count} left',
      addToCart: 'Add to Cart',
      saveForLater: 'Save for Later',
      share: 'Share',
      customerReviews: 'Customer Reviews',
      review: 'review',
      reviews: 'reviews',
      writeReview: 'Write a Review',
      yourRating: 'Your Rating',
      reviewTitle: 'Review Title',
      reviewTitlePlaceholder: 'Give your review a title',
      reviewContent: 'Review',
      reviewContentPlaceholder: 'Share your experience with this product',
      submitReview: 'Submit Review',
      sku: 'SKU',
      categories: 'Categories',
      shippingInfo: 'Shipping & Returns',
      freeShipping: 'Free Shipping',
      deliveryEstimate: 'Delivery estimate',
      returnsPolicy: 'Returns Policy'
    },
    ar: {
      outOfStock: 'غير متوفر',
      inStock: 'متوفر في المخزون',
      lowStock: 'بقي {count} فقط',
      addToCart: 'أضف إلى السلة',
      saveForLater: 'احفظ لوقت لاحق',
      share: 'مشاركة',
      customerReviews: 'آراء العملاء',
      review: 'رأي',
      reviews: 'آراء',
      writeReview: 'اكتب رأيك',
      yourRating: 'تقييمك',
      reviewTitle: 'عنوان التقييم',
      reviewTitlePlaceholder: 'أعط تقييمك عنوانًا',
      reviewContent: 'المراجعة',
      reviewContentPlaceholder: 'شارك تجربتك مع هذا المنتج',
      submitReview: 'إرسال المراجعة',
      sku: 'رقم المنتج',
      categories: 'الفئات',
      shippingInfo: 'الشحن والإرجاع',
      freeShipping: 'شحن مجاني',
      deliveryEstimate: 'موعد التسليم المقدر',
      returnsPolicy: 'سياسة الإرجاع'
    },
    fr: {
      outOfStock: 'En rupture de stock',
      inStock: 'En stock',
      lowStock: 'Seulement {count} restants',
      addToCart: 'Ajouter au panier',
      saveForLater: 'Sauvegarder pour plus tard',
      share: 'Partager',
      customerReviews: 'Avis clients',
      review: 'avis',
      reviews: 'avis',
      writeReview: 'Écrire un avis',
      yourRating: 'Votre évaluation',
      reviewTitle: 'Titre de l\'avis',
      reviewTitlePlaceholder: 'Donnez un titre à votre avis',
      reviewContent: 'Avis',
      reviewContentPlaceholder: 'Partagez votre expérience avec ce produit',
      submitReview: 'Soumettre l\'avis',
      sku: 'Référence',
      categories: 'Catégories',
      shippingInfo: 'Livraison et retours',
      freeShipping: 'Livraison gratuite',
      deliveryEstimate: 'Estimation de livraison',
      returnsPolicy: 'Politique de retour'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

module.exports = ProductDetail;
