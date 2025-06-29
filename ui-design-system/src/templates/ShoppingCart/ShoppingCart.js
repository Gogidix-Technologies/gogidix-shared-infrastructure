/**
 * ShoppingCart Template
 * 
 * A comprehensive shopping cart template that displays cart items,
 * calculates totals, applies promotions, and handles checkout.
 * 
 * This template is designed for the Social Commerce domain checkout flow.
 * 
 * Features:
 * - Cart item display with image, details, and quantity controls
 * - Price breakdown with subtotal, tax, shipping
 * - Promotional code application
 * - Shipping method selection
 * - Responsive design for all screen sizes
 * - Localization support
 * - Empty cart state
 * 
 * @module templates/ShoppingCart
 * @requires components/Button
 * @requires components/QuantitySelector
 * @requires utilities/localization
 * @requires utilities/design-tokens
 */

// Import required dependencies
const { getLocaleDirection, getTranslation } = require('../../utilities/localization');
const { colors, spacing, typography, borders, shadows } = require('../../utilities/design-tokens');
const Button = require('../../components/Button');
const QuantitySelector = require('../../components/QuantitySelector');

/**
 * CartItem Component
 * 
 * Renders a single item in the shopping cart
 * 
 * @param {Object} props - Component props
 * @param {Object} props.item - Cart item data
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the cart item
 */
function CartItem({ item, locale }) {
  if (!item || !item.product) {
    return '';
  }

  const { product, quantity, selectedVariants = {} } = item;
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'cart-item--rtl' : '';

  // Format variant selections for display
  const variantText = Object.entries(selectedVariants)
    .map(([type, value]) => {
      const typeLabel = getTranslation(`product.variant.${type}`, locale) || type;
      return `${typeLabel}: ${value}`;
    })
    .join(', ');

  // Calculate line item total
  const itemTotal = product.price.amount * quantity;
  const formattedTotal = `${product.price.currencySymbol}${itemTotal.toFixed(2)}`;

  return `
    <div class="cart-item ${rtlClass}" data-item-id="${item.id}">
      <!-- Item Image -->
      <div class="cart-item__image-container">
        <img 
          src="${product.thumbnail || product.images[0].src}" 
          alt="${product.name}" 
          class="cart-item__image"
        />
      </div>
      
      <!-- Item Details -->
      <div class="cart-item__details">
        <div class="cart-item__info">
          <h3 class="cart-item__name">${product.name}</h3>
          
          ${variantText ? `
            <div class="cart-item__variants">${variantText}</div>
          ` : ''}
          
          ${product.sku ? `
            <div class="cart-item__sku">SKU: ${product.sku}</div>
          ` : ''}
          
          ${!item.inStock ? `
            <div class="cart-item__stock-warning">
              ${getTranslation('cart.outOfStock', locale)}
            </div>
          ` : ''}
        </div>
        
        <div class="cart-item__price-container">
          <div class="cart-item__unit-price">
            ${product.price.currencySymbol}${product.price.amount.toFixed(2)} ${getTranslation('cart.each', locale)}
          </div>
        </div>
      </div>
      
      <!-- Quantity and Actions -->
      <div class="cart-item__actions">
        <div class="cart-item__quantity">
          ${QuantitySelector({
            value: quantity,
            min: 1,
            max: product.inventory?.quantity || 10,
            disabled: !item.inStock,
            small: true,
            locale
          })}
        </div>
        
        <div class="cart-item__subtotal">
          <span class="cart-item__subtotal-label">${getTranslation('cart.subtotal', locale)}:</span>
          <span class="cart-item__subtotal-value">${formattedTotal}</span>
        </div>
        
        <div class="cart-item__remove">
          ${Button({
            text: getTranslation('cart.remove', locale),
            variant: 'text',
            size: 'small',
            attributes: `data-action="remove" data-item-id="${item.id}"`,
            locale
          })}
        </div>
      </div>
    </div>
  `;
}

/**
 * EmptyCart Component
 * 
 * Renders an empty cart state with a CTA to continue shopping
 * 
 * @param {Object} props - Component props
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the empty cart state
 */
function EmptyCart({ locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'empty-cart--rtl' : '';

  return `
    <div class="empty-cart ${rtlClass}">
      <div class="empty-cart__icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M20 22C20 22.552 19.552 23 19 23C18.448 23 18 22.552 18 22C18 21.448 18.448 21 19 21C19.552 21 20 21.448 20 22Z" fill="currentColor"/>
          <path d="M10 22C10 22.552 9.552 23 9 23C8.448 23 8 22.552 8 22C8 21.448 8.448 21 9 21C9.552 21 10 21.448 10 22Z" fill="currentColor"/>
          <path d="M3 2H5.379C5.72074 2 6.05466 2.14819 6.29033 2.40981C6.52601 2.67143 6.64283 3.02186 6.614 3.371L6.266 7.371C6.21145 7.99071 6.68145 8.53153 7.3 8.585C7.35768 8.58849 7.41551 8.59001 7.473 8.589H19.723C20.2749 8.58526 20.7682 8.20891 20.908 7.673L21.894 3.842C21.9699 3.54956 21.9176 3.23501 21.7485 2.98265C21.5793 2.73029 21.3106 2.57021 21.015 2.547C20.9949 2.54559 20.9747 2.54492 20.9545 2.545H6.791" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M14 12H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M7.473 8.589L7.863 15.575C7.90411 16.246 8.46911 16.759 9.131 16.759H17.569C18.2182 16.7584 18.7801 16.2545 18.837 15.607L19.287 9.639" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h2 class="empty-cart__title">${getTranslation('cart.emptyTitle', locale)}</h2>
      <p class="empty-cart__message">${getTranslation('cart.emptyMessage', locale)}</p>
      ${Button({
        text: getTranslation('cart.continueShopping', locale),
        variant: 'primary',
        size: 'large',
        attributes: 'data-action="continue-shopping"',
        locale
      })}
    </div>
  `;
}

/**
 * SavedForLater Component
 * 
 * Renders a list of items saved for later purchase
 * 
 * @param {Object} props - Component props
 * @param {Array} props.savedItems - Array of saved items
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the saved items section
 */
function SavedForLater({ savedItems = [], locale }) {
  if (!savedItems || savedItems.length === 0) {
    return '';
  }

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'saved-items--rtl' : '';

  return `
    <div class="saved-items ${rtlClass}">
      <h3 class="saved-items__title">${getTranslation('cart.savedForLater', locale)}</h3>
      <div class="saved-items__list">
        ${savedItems.map(item => `
          <div class="saved-item" data-item-id="${item.id}">
            <div class="saved-item__image-container">
              <img 
                src="${item.product.thumbnail || item.product.images[0].src}" 
                alt="${item.product.name}" 
                class="saved-item__image"
              />
            </div>
            
            <div class="saved-item__details">
              <h4 class="saved-item__name">${item.product.name}</h4>
              <div class="saved-item__price">
                ${item.product.price.currencySymbol}${item.product.price.amount.toFixed(2)}
              </div>
            </div>
            
            <div class="saved-item__actions">
              ${Button({
                text: getTranslation('cart.moveToCart', locale),
                variant: 'outline',
                size: 'small',
                attributes: `data-action="move-to-cart" data-item-id="${item.id}"`,
                locale
              })}
              ${Button({
                text: getTranslation('cart.remove', locale),
                variant: 'text',
                size: 'small',
                attributes: `data-action="remove-saved" data-item-id="${item.id}"`,
                locale
              })}
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}
/**
 * CartSummary Component
 * 
 * Renders the order summary with price breakdown
 * 
 * @param {Object} props - Component props
 * @param {Object} props.summary - Cart summary data
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the cart summary
 */
function CartSummary({ summary, locale }) {
  if (!summary) return '';

  const { 
    subtotal, 
    tax, 
    shipping, 
    discount, 
    total,
    currencySymbol
  } = summary;

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'cart-summary--rtl' : '';

  return `
    <div class="cart-summary ${rtlClass}">
      <h3 class="cart-summary__title">${getTranslation('cart.orderSummary', locale)}</h3>
      
      <div class="cart-summary__breakdown">
        <div class="cart-summary__row">
          <span class="cart-summary__label">${getTranslation('cart.subtotal', locale)}</span>
          <span class="cart-summary__value">${currencySymbol}${subtotal.toFixed(2)}</span>
        </div>
        
        ${shipping !== undefined ? `
          <div class="cart-summary__row">
            <span class="cart-summary__label">${getTranslation('cart.shipping', locale)}</span>
            <span class="cart-summary__value">
              ${shipping === 0 
                ? getTranslation('cart.free', locale) 
                : `${currencySymbol}${shipping.toFixed(2)}`}
            </span>
          </div>
        ` : ''}
        
        ${tax !== undefined ? `
          <div class="cart-summary__row">
            <span class="cart-summary__label">${getTranslation('cart.tax', locale)}</span>
            <span class="cart-summary__value">${currencySymbol}${tax.toFixed(2)}</span>
          </div>
        ` : ''}
        
        ${discount ? `
          <div class="cart-summary__row cart-summary__row--discount">
            <span class="cart-summary__label">${getTranslation('cart.discount', locale)}</span>
            <span class="cart-summary__value">-${currencySymbol}${discount.toFixed(2)}</span>
          </div>
        ` : ''}
        
        <div class="cart-summary__row cart-summary__row--total">
          <span class="cart-summary__label">${getTranslation('cart.total', locale)}</span>
          <span class="cart-summary__value">${currencySymbol}${total.toFixed(2)}</span>
        </div>
      </div>
      
      <div class="cart-summary__actions">
        ${Button({
          text: getTranslation('cart.proceedToCheckout', locale),
          variant: 'primary',
          size: 'large',
          fullWidth: true,
          attributes: 'data-action="checkout"',
          locale
        })}
        
        ${Button({
          text: getTranslation('cart.continueShopping', locale),
          variant: 'outline',
          size: 'large',
          fullWidth: true,
          attributes: 'data-action="continue-shopping"',
          locale
        })}
      </div>
    </div>
  `;
}

/**
 * PromotionCode Component
 * 
 * Renders a form for applying promotion codes
 * 
 * @param {Object} props - Component props
 * @param {string} props.appliedCode - Currently applied promotion code
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the promotion code form
 */
function PromotionCode({ appliedCode = '', locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'promotion-code--rtl' : '';
  
  // If a promotion code is applied, show it with option to remove
  if (appliedCode) {
    return `
      <div class="promotion-code promotion-code--applied ${rtlClass}">
        <h3 class="promotion-code__title">${getTranslation('cart.appliedPromotion', locale)}</h3>
        <div class="promotion-code__applied">
          <span class="promotion-code__code">${appliedCode}</span>
          ${Button({
            text: getTranslation('cart.remove', locale),
            variant: 'text',
            size: 'small',
            attributes: 'data-action="remove-promo"',
            locale
          })}
        </div>
      </div>
    `;
  }
  
  // Otherwise, show the form to enter a promotion code
  return `
    <div class="promotion-code ${rtlClass}">
      <h3 class="promotion-code__title">${getTranslation('cart.promotionCode', locale)}</h3>
      <form class="promotion-code__form" data-action="apply-promo">
        <div class="promotion-code__input-group">
          <input 
            type="text" 
            class="promotion-code__input" 
            placeholder="${getTranslation('cart.enterCode', locale)}"
            aria-label="${getTranslation('cart.promotionCode', locale)}"
            name="promo-code"
          />
          ${Button({
            text: getTranslation('cart.apply', locale),
            variant: 'secondary',
            size: 'small',
            attributes: 'type="submit"',
            locale
          })}
        </div>
      </form>
    </div>
  `;
}

/**
 * ShippingOptions Component
 * 
 * Renders shipping method options
 * 
 * @param {Object} props - Component props
 * @param {Array} props.options - Available shipping options
 * @param {string} props.selectedOption - ID of currently selected option
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the shipping options
 */
function ShippingOptions({ options = [], selectedOption = '', locale }) {
  if (!options || options.length === 0) {
    return '';
  }

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'shipping-options--rtl' : '';

  return `
    <div class="shipping-options ${rtlClass}">
      <h3 class="shipping-options__title">${getTranslation('cart.shippingMethod', locale)}</h3>
      <div class="shipping-options__list">
        ${options.map(option => `
          <div class="shipping-option">
            <input 
              type="radio" 
              class="shipping-option__input" 
              id="shipping-${option.id}" 
              name="shipping-option" 
              value="${option.id}"
              ${option.id === selectedOption ? 'checked' : ''}
              data-action="select-shipping"
              data-price="${option.price}"
            />
            <label class="shipping-option__label" for="shipping-${option.id}">
              <div class="shipping-option__info">
                <span class="shipping-option__name">${option.name}</span>
                <span class="shipping-option__description">${option.description}</span>
                ${option.estimatedDelivery ? `
                  <span class="shipping-option__delivery">
                    ${getTranslation('cart.estimatedDelivery', locale)}: ${option.estimatedDelivery}
                  </span>
                ` : ''}
              </div>
              <span class="shipping-option__price">
                ${option.price === 0 ? 
                  getTranslation('cart.free', locale) : 
                  `${option.currencySymbol || '$'}${option.price.toFixed(2)}`
                }
              </span>
            </label>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

/**
 * CartRecommendations Component
 * 
 * Renders product recommendations based on cart contents
 * 
 * @param {Object} props - Component props
 * @param {Array} props.recommendations - Recommended products
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the recommendations
 */
function CartRecommendations({ recommendations = [], locale }) {
  if (!recommendations || recommendations.length === 0) {
    return '';
  }

  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'cart-recommendations--rtl' : '';

  return `
    <div class="cart-recommendations ${rtlClass}">
      <h3 class="cart-recommendations__title">
        ${getTranslation('cart.recommendations', locale)}
      </h3>
      
      <div class="cart-recommendations__slider">
        ${recommendations.map(product => `
          <div class="cart-recommendations__item" data-product-id="${product.id}">
            <div class="cart-recommendations__image-container">
              <img 
                src="${product.thumbnail || product.images[0].src}" 
                alt="${product.name}" 
                class="cart-recommendations__image"
              />
            </div>
            
            <div class="cart-recommendations__details">
              <h4 class="cart-recommendations__name">${product.name}</h4>
              <div class="cart-recommendations__price">
                ${product.price.currencySymbol}${product.price.amount.toFixed(2)}
              </div>
              
              <div class="cart-recommendations__actions">
                ${Button({
                  text: getTranslation('cart.addToCart', locale),
                  variant: 'outline',
                  size: 'small',
                  attributes: `data-action="add-to-cart" data-product-id="${product.id}"`,
                  locale
                })}
              </div>
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}
/**
 * Main ShoppingCart Template
 * 
 * Assembles all shopping cart components into the complete template
 * 
 * @param {Object} props - Component props
 * @param {Array} props.items - Cart items
 * @param {Object} props.summary - Cart summary data
 * @param {Array} props.savedItems - Items saved for later
 * @param {Array} props.recommendations - Recommended products
 * @param {string} props.appliedPromo - Applied promotion code
 * @param {Array} props.shippingOptions - Available shipping options
 * @param {string} props.selectedShipping - Selected shipping option ID
 * @param {string} props.locale - Current locale code
 * @returns {string} Complete HTML for the shopping cart template
 */
function ShoppingCart(props = {}) {
  const {
    items = [],
    summary = {
      subtotal: 0,
      tax: 0,
      shipping: 0,
      discount: 0,
      total: 0,
      currencySymbol: '$'
    },
    savedItems = [],
    recommendations = [],
    appliedPromo = '',
    shippingOptions = [],
    selectedShipping = '',
    locale = 'en-US'
  } = props;
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'shopping-cart--rtl' : '';
  
  // If cart is empty, render empty state
  if (!items || items.length === 0) {
    return `
      <div class="shopping-cart shopping-cart--empty ${rtlClass}">
        <div class="shopping-cart__header">
          <h1 class="shopping-cart__title">${getTranslation('cart.title', locale)}</h1>
        </div>
        
        <div class="shopping-cart__empty-container">
          ${EmptyCart({ locale })}
        </div>
        
        ${recommendations.length > 0 ? `
          <div class="shopping-cart__recommendations-container">
            ${CartRecommendations({ recommendations, locale })}
          </div>
        ` : ''}
        
        ${savedItems.length > 0 ? `
          <div class="shopping-cart__saved-container">
            ${SavedForLater({ savedItems, locale })}
          </div>
        ` : ''}
      </div>
    `;
  }
  
  // Render populated cart
  return `
    <div class="shopping-cart ${rtlClass}" data-cart-id="${props.cartId || 'cart'}">
      <div class="shopping-cart__header">
        <h1 class="shopping-cart__title">${getTranslation('cart.title', locale)}</h1>
        <span class="shopping-cart__count">
          ${items.length} ${getTranslation(items.length === 1 ? 'cart.item' : 'cart.items', locale)}
        </span>
      </div>
      
      <div class="shopping-cart__container">
        <!-- Cart Items Column -->
        <div class="shopping-cart__items-column">
          <div class="shopping-cart__items">
            ${items.map(item => CartItem({ item, locale })).join('')}
          </div>
          
          ${savedItems.length > 0 ? `
            <div class="shopping-cart__saved-items">
              ${SavedForLater({ savedItems, locale })}
            </div>
          ` : ''}
          
          ${recommendations.length > 0 ? `
            <div class="shopping-cart__recommendations">
              ${CartRecommendations({ recommendations, locale })}
            </div>
          ` : ''}
        </div>
        
        <!-- Summary Column -->
        <div class="shopping-cart__summary-column">
          ${shippingOptions.length > 0 ? `
            <div class="shopping-cart__shipping-section">
              ${ShippingOptions({ options: shippingOptions, selectedOption: selectedShipping, locale })}
            </div>
          ` : ''}
          
          <div class="shopping-cart__promo-section">
            ${PromotionCode({ appliedCode: appliedPromo, locale })}
          </div>
          
          <div class="shopping-cart__summary-section">
            ${CartSummary({ summary, locale })}
          </div>
        </div>
      </div>
      
      <!-- Mobile View Cart Actions -->
      <div class="shopping-cart__mobile-actions">
        <div class="shopping-cart__mobile-summary">
          <span class="shopping-cart__mobile-summary-label">${getTranslation('cart.total', locale)}:</span>
          <span class="shopping-cart__mobile-summary-value">${summary.currencySymbol}${summary.total.toFixed(2)}</span>
        </div>
        ${Button({
          text: getTranslation('cart.proceedToCheckout', locale),
          variant: 'primary',
          size: 'large',
          fullWidth: true,
          attributes: 'data-action="checkout"',
          locale
        })}
      </div>
      
      <!-- Embedded Scripts -->
      <script>
        (function() {
          // Initialize event handlers when DOM is loaded
          document.addEventListener('DOMContentLoaded', function() {
            const cart = document.querySelector('.shopping-cart');
            if (!cart) return;
            
            // Quantity change handlers
            const quantityButtons = document.querySelectorAll('.quantity-selector__button');
            quantityButtons.forEach(button => {
              button.addEventListener('click', function() {
                const input = this.parentElement.querySelector('.quantity-selector__input');
                const currentValue = parseInt(input.value, 10);
                const minValue = parseInt(input.getAttribute('min'), 10) || 1;
                const maxValue = parseInt(input.getAttribute('max'), 10) || 99;
                const action = this.getAttribute('data-action');
                const itemContainer = findAncestor(this, '.cart-item');
                const itemId = itemContainer ? itemContainer.getAttribute('data-item-id') : null;
                
                if (!itemId) return;
                
                let newValue = currentValue;
                if (action === 'decrease' && currentValue > minValue) {
                  newValue = currentValue - 1;
                } else if (action === 'increase' && currentValue < maxValue) {
                  newValue = currentValue + 1;
                }
                
                if (newValue !== currentValue) {
                  input.value = newValue;
                  
                  // Dispatch quantity change event
                  const event = new CustomEvent('cartUpdateQuantity', {
                    detail: {
                      itemId,
                      quantity: newValue
                    },
                    bubbles: true
                  });
                  this.dispatchEvent(event);
                }
              });
            });
            
            // Remove item handlers
            const removeButtons = document.querySelectorAll('[data-action="remove"]');
            removeButtons.forEach(button => {
              button.addEventListener('click', function() {
                const itemId = this.getAttribute('data-item-id');
                if (!itemId) return;
                
                // Dispatch remove event
                const event = new CustomEvent('cartRemoveItem', {
                  detail: {
                    itemId
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Shipping option selection
            const shippingOptions = document.querySelectorAll('[data-action="select-shipping"]');
            shippingOptions.forEach(option => {
              option.addEventListener('change', function() {
                if (!this.checked) return;
                
                const shippingId = this.value;
                const shippingPrice = parseFloat(this.getAttribute('data-price')) || 0;
                
                // Dispatch shipping change event
                const event = new CustomEvent('cartUpdateShipping', {
                  detail: {
                    shippingId,
                    price: shippingPrice
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Promotion code form
            const promoForm = document.querySelector('[data-action="apply-promo"]');
            if (promoForm) {
              promoForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const codeInput = this.querySelector('[name="promo-code"]');
                if (!codeInput || !codeInput.value.trim()) return;
                
                // Dispatch promo code event
                const event = new CustomEvent('cartApplyPromo', {
                  detail: {
                    code: codeInput.value.trim()
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            }
            
            // Remove promotion code
            const removePromoButton = document.querySelector('[data-action="remove-promo"]');
            if (removePromoButton) {
              removePromoButton.addEventListener('click', function() {
                // Dispatch remove promo event
                const event = new CustomEvent('cartRemovePromo', {
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            }
            
            // Saved items - move to cart
            const moveToCartButtons = document.querySelectorAll('[data-action="move-to-cart"]');
            moveToCartButtons.forEach(button => {
              button.addEventListener('click', function() {
                const itemId = this.getAttribute('data-item-id');
                if (!itemId) return;
                
                // Dispatch move to cart event
                const event = new CustomEvent('cartMoveToCart', {
                  detail: {
                    itemId
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Saved items - remove
            const removeSavedButtons = document.querySelectorAll('[data-action="remove-saved"]');
            removeSavedButtons.forEach(button => {
              button.addEventListener('click', function() {
                const itemId = this.getAttribute('data-item-id');
                if (!itemId) return;
                
                // Dispatch remove saved event
                const event = new CustomEvent('cartRemoveSaved', {
                  detail: {
                    itemId
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Recommendations - add to cart
            const recommendAddButtons = document.querySelectorAll('[data-action="add-to-cart"][data-product-id]');
            recommendAddButtons.forEach(button => {
              button.addEventListener('click', function() {
                const productId = this.getAttribute('data-product-id');
                if (!productId) return;
                
                // Dispatch add recommendation event
                const event = new CustomEvent('cartAddRecommendation', {
                  detail: {
                    productId,
                    quantity: 1
                  },
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Checkout button
            const checkoutButtons = document.querySelectorAll('[data-action="checkout"]');
            checkoutButtons.forEach(button => {
              button.addEventListener('click', function() {
                // Dispatch checkout event
                const event = new CustomEvent('cartCheckout', {
                  bubbles: true
                });
                this.dispatchEvent(event);
              });
            });
            
            // Continue shopping
            const continueButtons = document.querySelectorAll('[data-action="continue-shopping"]');
            continueButtons.forEach(button => {
              button.addEventListener('click', function() {
                // This typically redirects to product listing or homepage
                window.location.href = '/products';
              });
            });
            
            // Helper function to find ancestor with a specific class
            function findAncestor(element, selector) {
              while (element && !element.matches(selector)) {
                element = element.parentElement;
              }
              return element;
            }
          });
        })();
      </script>
    </div>
  `;
}

module.exports = ShoppingCart;
