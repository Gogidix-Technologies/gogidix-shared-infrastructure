/**
 * ProductDetail Template
 * 
 * A comprehensive product detail page template that combines product information,
 * variants, purchasing options, and social components (reviews, comments).
 * 
 * This template is designed to be used for the main product display in the Social Commerce domain.
 * It integrates with the ProductList, ReviewsSection, and CommentsSection components.
 * 
 * Features:
 * - Product image gallery with thumbnails
 * - Product information display (name, description, price)
 * - Variant selection (size, color, etc.)
 * - Quantity selection
 * - Add to cart functionality
 * - Social metrics display (likes, shares)
 * - Wishlist/favorites functionality
 * - Related products display
 * - Product reviews section
 * - Product comments section
 * - Vendor information
 * 
 * @module templates/ProductDetail
 * @requires components/Button
 * @requires components/Rating
 * @requires components/ProductCard
 * @requires components/QuantitySelector
 * @requires templates/ReviewsSection
 * @requires templates/CommentsSection
 * @requires utilities/localization
 * @requires utilities/design-tokens
 */

// Import required dependencies
const { getLocaleDirection, getTranslation } = require('../../utilities/localization');
const { colors, spacing, typography, borders, shadows, animations } = require('../../utilities/design-tokens');
const Button = require('../../components/Button');
const Rating = require('../../components/Rating');
const ProductCard = require('../../components/ProductCard');
const QuantitySelector = require('../../components/QuantitySelector');
const ReviewsSection = require('../ReviewsSection');
const CommentsSection = require('../CommentsSection');

/**
 * Image Gallery Component
 * 
 * Renders a product image gallery with main image and thumbnails
 * 
 * @param {Object} props - Component props
 * @param {Array} props.images - Array of image objects {src, alt}
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the image gallery
 */
function ImageGallery({ images, locale }) {
  if (!images || images.length === 0) {
    return `
      <div class="product-image-gallery product-image-gallery--empty">
        <div class="product-image-gallery__placeholder">
          ${getTranslation('productDetail.noImagesAvailable', locale)}
        </div>
      </div>
    `;
  }

  const mainImage = images[0];
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'product-image-gallery--rtl' : '';

  return `
    <div class="product-image-gallery ${rtlClass}">
      <div class="product-image-gallery__main">
        <img 
          src="${mainImage.src}" 
          alt="${mainImage.alt || ''}" 
          class="product-image-gallery__main-image" 
          id="main-product-image"
        />
      </div>
      <div class="product-image-gallery__thumbnails">
        ${images.map((image, index) => `
          <div class="product-image-gallery__thumbnail-container ${index === 0 ? 'product-image-gallery__thumbnail-container--active' : ''}">
            <img 
              src="${image.src}" 
              alt="${image.alt || ''}" 
              class="product-image-gallery__thumbnail" 
              data-image-index="${index}"
              aria-label="${getTranslation('productDetail.viewImage', locale)} ${index + 1}"
            />
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

/**
 * Product Info Component
 * 
 * Renders product details including name, vendor, rating, price, and description
 * 
 * @param {Object} props - Component props
 * @param {Object} props.product - Product data object
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the product info section
 */
function ProductInfo({ product, locale }) {
  const { 
    name, 
    vendor, 
    rating, 
    reviewCount, 
    price, 
    compareAtPrice, 
    description,
    inStock,
    sku
  } = product;

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'product-info--rtl' : '';
  
  // Calculate discount percentage if there's a compare-at price
  let discountBadge = '';
  if (compareAtPrice && compareAtPrice > price.amount) {
    const discountPercent = Math.round((1 - (price.amount / compareAtPrice)) * 100);
    discountBadge = `
      <span class="product-info__discount-badge">
        -${discountPercent}%
      </span>
    `;
  }

  // Generate stock status message
  const stockStatusClass = inStock ? 'product-info__stock--in-stock' : 'product-info__stock--out-of-stock';
  const stockStatusMessage = inStock 
    ? getTranslation('productDetail.inStock', locale)
    : getTranslation('productDetail.outOfStock', locale);

  return `
    <div class="product-info ${rtlClass}">
      <div class="product-info__header">
        ${vendor ? `<div class="product-info__vendor">${vendor.name}</div>` : ''}
        <h1 class="product-info__name">${name}</h1>
        
        <div class="product-info__rating-container">
          ${Rating({ value: rating, max: 5, locale })}
          <a href="#reviews" class="product-info__review-count">
            ${reviewCount} ${getTranslation('productDetail.reviews', locale)}
          </a>
        </div>
      </div>
      
      <div class="product-info__price-container">
        <span class="product-info__price">
          ${price.currencySymbol}${price.amount.toFixed(2)}
        </span>
        ${compareAtPrice ? `
          <span class="product-info__compare-price">
            ${price.currencySymbol}${compareAtPrice.toFixed(2)}
          </span>
        ` : ''}
        ${discountBadge}
      </div>

      <div class="product-info__stock ${stockStatusClass}">
        ${stockStatusMessage}
      </div>
      
      <div class="product-info__description">
        ${description}
      </div>
      
      ${sku ? `<div class="product-info__sku">SKU: ${sku}</div>` : ''}
    </div>
  `;
}

/**
 * Product Variants Component
 * 
 * Renders variant selectors (color, size, etc.)
 * 
 * @param {Object} props - Component props
 * @param {Array} props.variants - Array of variant option types
 * @param {Array} props.selectedVariants - Array of currently selected variant values
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the variant selector section
 */
function ProductVariants({ variants, selectedVariants = {}, locale }) {
  if (!variants || variants.length === 0) {
    return '';
  }

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'product-variants--rtl' : '';

  return `
    <div class="product-variants ${rtlClass}">
      ${variants.map(variant => `
        <div class="product-variants__group">
          <label class="product-variants__label">
            ${getTranslation(`productDetail.variant.${variant.type}`, locale) || variant.name}:
          </label>
          <div class="product-variants__options">
            ${renderVariantOptions(variant, selectedVariants[variant.type], locale)}
          </div>
        </div>
      `).join('')}
    </div>
  `;
}
/**
 * Helper function to render variant options based on variant type
 * 
 * @param {Object} variant - Variant data
 * @param {string} selectedValue - Currently selected variant value
 * @param {string} locale - Current locale code
 * @returns {string} HTML for the appropriate variant selector
 */
function renderVariantOptions(variant, selectedValue, locale) {
  const { type, values } = variant;

  switch (type) {
    case 'color':
      return values.map(value => `
        <button 
          class="product-variants__color-option ${selectedValue === value.value ? 'product-variants__color-option--selected' : ''}" 
          data-variant-type="${type}"
          data-variant-value="${value.value}"
          aria-label="${value.name}"
          aria-pressed="${selectedValue === value.value ? 'true' : 'false'}"
          style="background-color: ${value.hex || value.value};"
        >
          ${selectedValue === value.value ? '<span class="product-variants__color-checkmark"></span>' : ''}
        </button>
      `).join('');

    case 'size':
      return values.map(value => `
        <button 
          class="product-variants__size-option ${selectedValue === value.value ? 'product-variants__size-option--selected' : ''}" 
          data-variant-type="${type}"
          data-variant-value="${value.value}"
          aria-pressed="${selectedValue === value.value ? 'true' : 'false'}"
          ${value.disabled ? 'disabled aria-disabled="true"' : ''}
        >
          ${value.name}
        </button>
      `).join('');

    default:
      return `
        <select class="product-variants__select" data-variant-type="${type}">
          <option value="">${getTranslation('productDetail.selectOption', locale)}</option>
          ${values.map(value => `
            <option 
              value="${value.value}" 
              ${selectedValue === value.value ? 'selected' : ''}
              ${value.disabled ? 'disabled' : ''}
            >
              ${value.name}
            </option>
          `).join('')}
        </select>
      `;
  }
}

/**
 * Purchase Options Component
 * 
 * Renders quantity selector and action buttons (add to cart, buy now)
 * 
 * @param {Object} props - Component props
 * @param {Object} props.product - Product data
 * @param {number} props.quantity - Current quantity selected
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the purchase options section
 */
function PurchaseOptions({ product, quantity = 1, locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'purchase-options--rtl' : '';

  return `
    <div class="purchase-options ${rtlClass}">
      <div class="purchase-options__quantity-container">
        <label class="purchase-options__quantity-label">
          ${getTranslation('productDetail.quantity', locale)}:
        </label>
        ${QuantitySelector({ 
          value: quantity,
          min: 1,
          max: product.inventory?.quantity || 10,
          disabled: !product.inStock,
          locale
        })}
      </div>
      
      <div class="purchase-options__buttons">
        ${Button({
          text: getTranslation('productDetail.addToCart', locale),
          variant: 'primary',
          size: 'large',
          disabled: !product.inStock,
          fullWidth: true,
          attributes: 'data-action="add-to-cart"',
          locale
        })}
        
        ${Button({
          text: getTranslation('productDetail.buyNow', locale),
          variant: 'secondary',
          size: 'large',
          disabled: !product.inStock,
          fullWidth: true,
          attributes: 'data-action="buy-now"',
          locale
        })}
      </div>
      
      <div class="purchase-options__wishlist">
        ${Button({
          text: getTranslation('productDetail.addToWishlist', locale),
          variant: 'text',
          icon: 'heart',
          iconPosition: 'left',
          attributes: 'data-action="add-to-wishlist"',
          locale
        })}
      </div>
    </div>
  `;
}

/**
 * Social Sharing Component
 * 
 * Renders social sharing buttons and metrics
 * 
 * @param {Object} props - Component props
 * @param {Object} props.product - Product data with social metrics
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the social sharing section
 */
function SocialSharing({ product, locale }) {
  const { socialMetrics = {} } = product;
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'social-sharing--rtl' : '';

  return `
    <div class="social-sharing ${rtlClass}">
      <div class="social-sharing__metrics">
        ${socialMetrics.likes ? `
          <span class="social-sharing__metric">
            <i class="social-sharing__icon social-sharing__icon--like"></i>
            ${socialMetrics.likes}
          </span>
        ` : ''}
        ${socialMetrics.shares ? `
          <span class="social-sharing__metric">
            <i class="social-sharing__icon social-sharing__icon--share"></i>
            ${socialMetrics.shares}
          </span>
        ` : ''}
      </div>
      
      <div class="social-sharing__buttons">
        <span class="social-sharing__label">${getTranslation('productDetail.shareOn', locale)}:</span>
        <button class="social-sharing__button social-sharing__button--facebook" aria-label="Facebook" data-share="facebook">
          <i class="social-sharing__platform-icon social-sharing__platform-icon--facebook"></i>
        </button>
        <button class="social-sharing__button social-sharing__button--twitter" aria-label="Twitter" data-share="twitter">
          <i class="social-sharing__platform-icon social-sharing__platform-icon--twitter"></i>
        </button>
        <button class="social-sharing__button social-sharing__button--pinterest" aria-label="Pinterest" data-share="pinterest">
          <i class="social-sharing__platform-icon social-sharing__platform-icon--pinterest"></i>
        </button>
        <button class="social-sharing__button social-sharing__button--copy" aria-label="Copy Link" data-share="copy">
          <i class="social-sharing__platform-icon social-sharing__platform-icon--link"></i>
        </button>
      </div>
    </div>
  `;
}

/**
 * Vendor Information Component
 * 
 * Renders vendor details and store link
 * 
 * @param {Object} props - Component props
 * @param {Object} props.vendor - Vendor data
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the vendor information section
 */
function VendorInformation({ vendor, locale }) {
  if (!vendor) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'vendor-information--rtl' : '';

  return `
    <div class="vendor-information ${rtlClass}">
      <div class="vendor-information__header">
        ${vendor.logo ? `
          <img 
            src="${vendor.logo}" 
            alt="${vendor.name}" 
            class="vendor-information__logo"
          />
        ` : ''}
        <div class="vendor-information__details">
          <h3 class="vendor-information__name">${vendor.name}</h3>
          ${vendor.rating ? `
            <div class="vendor-information__rating">
              ${Rating({ value: vendor.rating, max: 5, size: 'small', locale })}
              ${vendor.reviewCount ? `<span class="vendor-information__review-count">(${vendor.reviewCount})</span>` : ''}
            </div>
          ` : ''}
        </div>
      </div>
      
      ${vendor.description ? `
        <p class="vendor-information__description">${vendor.description}</p>
      ` : ''}
      
      <div class="vendor-information__actions">
        ${Button({
          text: getTranslation('productDetail.visitStore', locale),
          variant: 'outline',
          size: 'small',
          attributes: `data-vendor-id="${vendor.id}"`,
          locale
        })}
      </div>
    </div>
  `;
}
/**
 * Related Products Component
 * 
 * Renders a carousel of related product cards
 * 
 * @param {Object} props - Component props
 * @param {Array} props.products - Array of related product data
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the related products carousel
 */
function RelatedProducts({ products, locale }) {
  if (!products || products.length === 0) {
    return '';
  }

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'related-products--rtl' : '';
  
  return `
    <div class="related-products ${rtlClass}">
      <h2 class="related-products__title">
        ${getTranslation('productDetail.relatedProducts', locale)}
      </h2>
      
      <div class="related-products__carousel">
        <button 
          class="related-products__nav related-products__nav--prev" 
          aria-label="${getTranslation('productDetail.previousProducts', locale)}"
          data-action="prev-related"
        >
          <i class="related-products__nav-icon related-products__nav-icon--prev"></i>
        </button>
        
        <div class="related-products__slider">
          ${products.map(product => `
            <div class="related-products__item">
              ${ProductCard({ 
                product, 
                layout: 'compact',
                locale
              })}
            </div>
          `).join('')}
        </div>
        
        <button 
          class="related-products__nav related-products__nav--next" 
          aria-label="${getTranslation('productDetail.nextProducts', locale)}"
          data-action="next-related"
        >
          <i class="related-products__nav-icon related-products__nav-icon--next"></i>
        </button>
      </div>
    </div>
  `;
}

/**
 * Product Tabs Component
 * 
 * Renders tabbed content for additional product information
 * 
 * @param {Object} props - Component props
 * @param {Object} props.product - Product data
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the tabbed content section
 */
function ProductTabs({ product, locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'product-tabs--rtl' : '';
  
  // Define tabs and their content
  const tabs = [
    {
      id: 'details',
      label: getTranslation('productDetail.tabs.details', locale),
      content: product.detailedDescription || product.description
    },
    {
      id: 'specifications',
      label: getTranslation('productDetail.tabs.specifications', locale),
      content: renderSpecifications(product.specifications, locale)
    },
    {
      id: 'shipping',
      label: getTranslation('productDetail.tabs.shipping', locale),
      content: product.shippingInfo || getTranslation('productDetail.shippingDefaultInfo', locale)
    },
    {
      id: 'returns',
      label: getTranslation('productDetail.tabs.returns', locale),
      content: product.returnPolicy || getTranslation('productDetail.returnsDefaultInfo', locale)
    }
  ];
  
  return `
    <div class="product-tabs ${rtlClass}" role="tablist">
      <div class="product-tabs__headers">
        ${tabs.map((tab, index) => `
          <button 
            class="product-tabs__header ${index === 0 ? 'product-tabs__header--active' : ''}" 
            role="tab"
            id="tab-${tab.id}"
            aria-selected="${index === 0 ? 'true' : 'false'}"
            aria-controls="tab-panel-${tab.id}"
            data-tab="${tab.id}"
          >
            ${tab.label}
          </button>
        `).join('')}
      </div>
      
      <div class="product-tabs__panels">
        ${tabs.map((tab, index) => `
          <div 
            class="product-tabs__panel ${index === 0 ? 'product-tabs__panel--active' : ''}" 
            id="tab-panel-${tab.id}"
            role="tabpanel"
            aria-labelledby="tab-${tab.id}"
            data-tab-panel="${tab.id}"
            ${index !== 0 ? 'hidden' : ''}
          >
            ${tab.content}
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

/**
 * Helper function to render product specifications
 * 
 * @param {Array} specifications - Array of specification objects
 * @param {string} locale - Current locale code
 * @returns {string} HTML for the specifications table
 */
function renderSpecifications(specifications, locale) {
  if (!specifications || specifications.length === 0) {
    return `<p>${getTranslation('productDetail.noSpecifications', locale)}</p>`;
  }
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'specifications--rtl' : '';
  
  return `
    <div class="specifications ${rtlClass}">
      <table class="specifications__table">
        <tbody>
          ${specifications.map(spec => `
            <tr class="specifications__row">
              <th class="specifications__label">${spec.name}</th>
              <td class="specifications__value">${spec.value}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

/**
 * Main ProductDetail Template
 * 
 * Assembles all product detail components into the complete template
 * 
 * @param {Object} props - Component props
 * @param {Object} props.product - Complete product data object
 * @param {Array} props.relatedProducts - Array of related product objects
 * @param {Object} props.selectedVariants - Currently selected variant values
 * @param {number} props.quantity - Currently selected quantity
 * @param {string} props.locale - Current locale code
 * @returns {string} Complete HTML for the product detail template
 */
function ProductDetail(props = {}) {
  const {
    product = {},
    relatedProducts = [],
    selectedVariants = {},
    quantity = 1,
    locale = 'en-US'
  } = props;
  
  // Ensure required data is present
  if (!product || !product.id) {
    return `
      <div class="product-detail product-detail--error">
        <div class="product-detail__error-message">
          ${getTranslation('productDetail.productNotFound', locale)}
        </div>
      </div>
    `;
  }
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'product-detail--rtl' : '';
  
  return `
    <div class="product-detail ${rtlClass}" data-product-id="${product.id}">
      <div class="product-detail__container">
        <!-- Product Main Section -->
        <div class="product-detail__main">
          <!-- Left Column: Image Gallery -->
          <div class="product-detail__gallery-container">
            ${ImageGallery({ images: product.images, locale })}
          </div>
          
          <!-- Right Column: Product Information -->
          <div class="product-detail__info-container">
            ${ProductInfo({ product, locale })}
            
            ${product.variants && product.variants.length > 0 ? 
              ProductVariants({ variants: product.variants, selectedVariants, locale }) : ''}
            
            ${PurchaseOptions({ product, quantity, locale })}
            
            ${SocialSharing({ product, locale })}
            
            ${product.vendor ? VendorInformation({ vendor: product.vendor, locale }) : ''}
          </div>
        </div>
        
        <!-- Product Tabs Section -->
        <div class="product-detail__tabs-container">
          ${ProductTabs({ product, locale })}
        </div>
        
        <!-- Reviews Section -->
        <div class="product-detail__reviews-container" id="reviews">
          ${ReviewsSection({ 
            productId: product.id,
            initialRating: product.rating,
            reviewCount: product.reviewCount,
            locale
          })}
        </div>
        
        <!-- Comments Section -->
        <div class="product-detail__comments-container" id="comments">
          ${CommentsSection({
            productId: product.id,
            locale
          })}
        </div>
        
        <!-- Related Products Section -->
        ${relatedProducts && relatedProducts.length > 0 ? `
          <div class="product-detail__related-container">
            ${RelatedProducts({ products: relatedProducts, locale })}
          </div>
        ` : ''}
      </div>
      
      <!-- Embedded Scripts -->
      <script>
        (function() {
          // Image Gallery Functionality
          document.addEventListener('DOMContentLoaded', function() {
            const galleryThumbnails = document.querySelectorAll('.product-image-gallery__thumbnail');
            const mainImage = document.getElementById('main-product-image');
            
            galleryThumbnails.forEach(thumbnail => {
              thumbnail.addEventListener('click', function() {
                const imageIndex = this.getAttribute('data-image-index');
                const imageUrl = this.getAttribute('src');
                const imageAlt = this.getAttribute('alt');
                
                // Update main image
                mainImage.setAttribute('src', imageUrl);
                mainImage.setAttribute('alt', imageAlt);
                
                // Update active thumbnail state
                document.querySelectorAll('.product-image-gallery__thumbnail-container').forEach(container => {
                  container.classList.remove('product-image-gallery__thumbnail-container--active');
                });
                this.parentElement.classList.add('product-image-gallery__thumbnail-container--active');
              });
            });
            
            // Variant Selection
            const variantButtons = document.querySelectorAll('[data-variant-type]');
            variantButtons.forEach(button => {
              button.addEventListener('click', function() {
                const variantType = this.getAttribute('data-variant-type');
                const variantValue = this.getAttribute('data-variant-value');
                
                // Update selection state
                document.querySelectorAll(`[data-variant-type="${variantType}"]`).forEach(btn => {
                  if (btn.tagName === 'BUTTON') {
                    btn.classList.remove(`product-variants__${variantType}-option--selected`);
                    btn.setAttribute('aria-pressed', 'false');
                  }
                });
                
                this.classList.add(`product-variants__${variantType}-option--selected`);
                this.setAttribute('aria-pressed', 'true');
                
                // Dispatch variant change event
                const event = new CustomEvent('variantChange', {
                  detail: {
                    productId: document.querySelector('.product-detail').getAttribute('data-product-id'),
                    variantType,
                    variantValue
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Quantity Selector
            const quantityButtons = document.querySelectorAll('.quantity-selector__button');
            quantityButtons.forEach(button => {
              button.addEventListener('click', function() {
                const input = this.parentElement.querySelector('.quantity-selector__input');
                const currentValue = parseInt(input.value, 10);
                const minValue = parseInt(input.getAttribute('min'), 10) || 1;
                const maxValue = parseInt(input.getAttribute('max'), 10) || 99;
                const action = this.getAttribute('data-action');
                
                let newValue = currentValue;
                if (action === 'decrease' && currentValue > minValue) {
                  newValue = currentValue - 1;
                } else if (action === 'increase' && currentValue < maxValue) {
                  newValue = currentValue + 1;
                }
                
                input.value = newValue;
                
                // Dispatch quantity change event
                const event = new CustomEvent('quantityChange', {
                  detail: {
                    productId: document.querySelector('.product-detail').getAttribute('data-product-id'),
                    quantity: newValue
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Tab Navigation
            const tabButtons = document.querySelectorAll('.product-tabs__header');
            tabButtons.forEach(button => {
              button.addEventListener('click', function() {
                const tabId = this.getAttribute('data-tab');
                
                // Update tab button states
                tabButtons.forEach(btn => {
                  btn.classList.remove('product-tabs__header--active');
                  btn.setAttribute('aria-selected', 'false');
                });
                this.classList.add('product-tabs__header--active');
                this.setAttribute('aria-selected', 'true');
                
                // Show selected tab panel
                document.querySelectorAll('.product-tabs__panel').forEach(panel => {
                  panel.classList.remove('product-tabs__panel--active');
                  panel.hidden = true;
                });
                const targetPanel = document.querySelector(`[data-tab-panel="${tabId}"]`);
                targetPanel.classList.add('product-tabs__panel--active');
                targetPanel.hidden = false;
              });
            });
            
            // Related Products Carousel
            const prevButton = document.querySelector('.related-products__nav--prev');
            const nextButton = document.querySelector('.related-products__nav--next');
            const slider = document.querySelector('.related-products__slider');
            
            if (prevButton && nextButton && slider) {
              let scrollAmount = 0;
              const scrollStep = slider.offsetWidth / 2;
              
              prevButton.addEventListener('click', function() {
                scrollAmount = Math.max(0, scrollAmount - scrollStep);
                slider.scrollTo({
                  left: scrollAmount,
                  behavior: 'smooth'
                });
              });
              
              nextButton.addEventListener('click', function() {
                scrollAmount = Math.min(
                  slider.scrollWidth - slider.offsetWidth, 
                  scrollAmount + scrollStep
                );
                slider.scrollTo({
                  left: scrollAmount,
                  behavior: 'smooth'
                });
              });
            }
            
            // Add to Cart & Buy Now actions
            const addToCartButton = document.querySelector('[data-action="add-to-cart"]');
            const buyNowButton = document.querySelector('[data-action="buy-now"]');
            
            if (addToCartButton) {
              addToCartButton.addEventListener('click', function() {
                const productId = document.querySelector('.product-detail').getAttribute('data-product-id');
                const quantity = document.querySelector('.quantity-selector__input').value;
                const selectedVariants = {};
                
                document.querySelectorAll('[data-variant-type].product-variants__color-option--selected, [data-variant-type].product-variants__size-option--selected').forEach(element => {
                  const type = element.getAttribute('data-variant-type');
                  const value = element.getAttribute('data-variant-value');
                  selectedVariants[type] = value;
                });
                
                document.querySelectorAll('select[data-variant-type]').forEach(select => {
                  const type = select.getAttribute('data-variant-type');
                  const value = select.value;
                  if (value) {
                    selectedVariants[type] = value;
                  }
                });
                
                // Dispatch add to cart event
                const event = new CustomEvent('addToCart', {
                  detail: {
                    productId,
                    quantity: parseInt(quantity, 10),
                    variants: selectedVariants
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            }
            
            if (buyNowButton) {
              buyNowButton.addEventListener('click', function() {
                // Simulate adding to cart first
                addToCartButton.click();
                
                // Then dispatch buy now event to redirect to checkout
                const event = new CustomEvent('buyNow', {
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            }
            
            // Social Sharing
            const shareButtons = document.querySelectorAll('[data-share]');
            shareButtons.forEach(button => {
              button.addEventListener('click', function() {
                const platform = this.getAttribute('data-share');
                const productUrl = window.location.href;
                const productName = document.querySelector('.product-info__name').textContent;
                
                let shareUrl;
                
                switch (platform) {
                  case 'facebook':
                    shareUrl = `https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(productUrl)}`;
                    break;
                  case 'twitter':
                    shareUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(productName)}&url=${encodeURIComponent(productUrl)}`;
                    break;
                  case 'pinterest':
                    const imageUrl = document.getElementById('main-product-image').getAttribute('src');
                    shareUrl = `https://pinterest.com/pin/create/button/?url=${encodeURIComponent(productUrl)}&media=${encodeURIComponent(imageUrl)}&description=${encodeURIComponent(productName)}`;
                    break;
                  case 'copy':
                    // Copy to clipboard
                    navigator.clipboard.writeText(productUrl).then(() => {
                      alert('Link copied to clipboard!');
                    }).catch(err => {
                      console.error('Failed to copy link: ', err);
                    });
                    return;
                  default:
                    return;
                }
                
                // Open share dialog
                window.open(shareUrl, '_blank', 'width=600,height=400');
              });
            });
            
            // Wishlist functionality
            const wishlistButton = document.querySelector('[data-action="add-to-wishlist"]');
            if (wishlistButton) {
              wishlistButton.addEventListener('click', function() {
                const productId = document.querySelector('.product-detail').getAttribute('data-product-id');
                
                // Dispatch wishlist event
                const event = new CustomEvent('addToWishlist', {
                  detail: {
                    productId
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            }
          });
        })();
      </script>
    </div>
  `;
}

module.exports = ProductDetail;
