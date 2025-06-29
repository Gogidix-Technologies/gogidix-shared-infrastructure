/**
 * OrderSummary Template
 * 
 * A comprehensive order summary template that displays ordered items,
 * pricing details, shipping information, payment details, and order status.
 * 
 * This template is designed for use in checkout flows, order confirmation pages,
 * and order history within the Social Commerce domain.
 * 
 * Features:
 * - Order header with order number, date, and status
 * - Detailed line items with product info, quantity, and price
 * - Price breakdown (subtotal, taxes, shipping, discounts)
 * - Shipping information display
 * - Billing information display
 * - Payment method details
 * - Order actions (print, reorder, track)
 * - Responsive design for all screen sizes
 * - Localization support
 * 
 * @module templates/OrderSummary
 * @requires utilities/localization
 * @requires utilities/design-tokens
 */

// Import required dependencies
const { getLocaleDirection, getTranslation } = require('../../utilities/localization');
const { colors, spacing, typography } = require('../../utilities/design-tokens');
const Button = require('../../components/Button');

/**
 * OrderHeader Component
 * 
 * Renders the order header with order number, date, and status
 * 
 * @param {Object} props - Component props
 * @param {Object} props.order - Order data object
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the order header
 */
function OrderHeader({ order, locale }) {
  if (!order) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'order-header--rtl' : '';
  
  // Format order date
  const formattedDate = new Date(order.date).toLocaleDateString(locale, {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  
  return `
    <div class="order-header ${rtlClass}">
      <div class="order-header__main">
        <h1 class="order-header__title">${getTranslation('order.summary', locale)}</h1>
        
        <div class="order-header__meta">
          <div class="order-header__item">
            <span class="order-header__label">${getTranslation('order.number', locale)}:</span>
            <span class="order-header__value">${order.number}</span>
          </div>
          
          <div class="order-header__item">
            <span class="order-header__label">${getTranslation('order.date', locale)}:</span>
            <span class="order-header__value">${formattedDate}</span>
          </div>
        </div>
      </div>
      
      <div class="order-header__status order-header__status--${order.status.toLowerCase()}">
        ${getTranslation(`order.status.${order.status.toLowerCase()}`, locale) || order.status}
      </div>
    </div>
  `;
}

/**
 * OrderItems Component
 * 
 * Renders the ordered items with product details, quantity, and price
 * 
 * @param {Object} props - Component props
 * @param {Array} props.items - Order line items
 * @param {string} props.currencySymbol - Currency symbol to display
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the order items
 */
function OrderItems({ items = [], currencySymbol = '$', locale }) {
  if (!items || items.length === 0) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'order-items--rtl' : '';
  
  return `
    <div class="order-items ${rtlClass}">
      <h2 class="order-items__title">${getTranslation('order.items', locale)}</h2>
      
      <div class="order-items__list">
        ${items.map(item => `
          <div class="order-items__item" data-product-id="${item.product.id}">
            <div class="order-items__image-container">
              <img 
                src="${item.product.image || item.product.thumbnail}" 
                alt="${item.product.name}"
                class="order-items__image"
              />
            </div>
            
            <div class="order-items__details">
              <div class="order-items__name">${item.product.name}</div>
              
              ${item.variant && Object.keys(item.variant).length > 0 ? `
                <div class="order-items__variants">
                  ${Object.entries(item.variant).map(([key, value]) => 
                    `<span class="order-items__variant">${key}: ${value}</span>`
                  ).join(', ')}
                </div>
              ` : ''}
              
              ${item.product.sku ? `
                <div class="order-items__sku">
                  ${getTranslation('order.sku', locale)}: ${item.product.sku}
                </div>
              ` : ''}
            </div>
            
            <div class="order-items__quantity">
              <span class="order-items__quantity-label">${getTranslation('order.quantity', locale)}:</span>
              <span class="order-items__quantity-value">${item.quantity}</span>
            </div>
            
            <div class="order-items__price">
              ${currencySymbol}${(item.price * item.quantity).toFixed(2)}
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

/**
 * PriceSummary Component
 * 
 * Renders the price breakdown including subtotal, taxes, shipping, and discounts
 * 
 * @param {Object} props - Component props
 * @param {Object} props.summary - Price summary data
 * @param {string} props.currencySymbol - Currency symbol to display
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the price summary
 */
function PriceSummary({ summary = {}, currencySymbol = '$', locale }) {
  if (!summary) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'price-summary--rtl' : '';
  
  // Default values
  const {
    subtotal = 0,
    tax = 0,
    shipping = 0,
    discount = 0,
    total = 0
  } = summary;
  
  return `
    <div class="price-summary ${rtlClass}">
      <h2 class="price-summary__title">${getTranslation('order.summary', locale)}</h2>
      
      <div class="price-summary__list">
        <div class="price-summary__item">
          <span class="price-summary__label">${getTranslation('order.subtotal', locale)}</span>
          <span class="price-summary__value">${currencySymbol}${subtotal.toFixed(2)}</span>
        </div>
        
        ${shipping > 0 ? `
          <div class="price-summary__item">
            <span class="price-summary__label">${getTranslation('order.shipping', locale)}</span>
            <span class="price-summary__value">${currencySymbol}${shipping.toFixed(2)}</span>
          </div>
        ` : ''}
        
        ${tax > 0 ? `
          <div class="price-summary__item">
            <span class="price-summary__label">${getTranslation('order.tax', locale)}</span>
            <span class="price-summary__value">${currencySymbol}${tax.toFixed(2)}</span>
          </div>
        ` : ''}
        
        ${discount > 0 ? `
          <div class="price-summary__item price-summary__item--discount">
            <span class="price-summary__label">${getTranslation('order.discount', locale)}</span>
            <span class="price-summary__value">-${currencySymbol}${discount.toFixed(2)}</span>
          </div>
        ` : ''}
        
        <div class="price-summary__item price-summary__item--total">
          <span class="price-summary__label">${getTranslation('order.total', locale)}</span>
          <span class="price-summary__value">${currencySymbol}${total.toFixed(2)}</span>
        </div>
      </div>
    </div>
  `;
}

/**
 * ShippingInfo Component
 * 
 * Renders the shipping information for the order
 * 
 * @param {Object} props - Component props
 * @param {Object} props.shipping - Shipping information
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the shipping information
 */
function ShippingInfo({ shipping, locale }) {
  if (!shipping) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'shipping-info--rtl' : '';
  
  return `
    <div class="shipping-info ${rtlClass}">
      <h2 class="shipping-info__title">${getTranslation('order.shippingInformation', locale)}</h2>
      
      <div class="shipping-info__content">
        <div class="shipping-info__method">
          <span class="shipping-info__label">${getTranslation('order.shippingMethod', locale)}:</span>
          <span class="shipping-info__value">${shipping.method}</span>
        </div>
        
        <div class="shipping-info__address">
          <span class="shipping-info__label">${getTranslation('order.shippingAddress', locale)}:</span>
          <div class="shipping-info__address-details">
            <div class="shipping-info__name">${shipping.address.name}</div>
            <div class="shipping-info__address-line">${shipping.address.line1}</div>
            ${shipping.address.line2 ? `<div class="shipping-info__address-line">${shipping.address.line2}</div>` : ''}
            <div class="shipping-info__address-line">
              ${shipping.address.city}, ${shipping.address.state || ''} ${shipping.address.postalCode}
            </div>
            <div class="shipping-info__address-line">
              ${getTranslation(`order.countries.${shipping.address.country}`, locale) || shipping.address.country}
            </div>
            ${shipping.address.phone ? `<div class="shipping-info__phone">${shipping.address.phone}</div>` : ''}
          </div>
        </div>
        
        ${shipping.trackingNumber ? `
          <div class="shipping-info__tracking">
            <span class="shipping-info__label">${getTranslation('order.trackingNumber', locale)}:</span>
            <span class="shipping-info__value">${shipping.trackingNumber}</span>
            ${shipping.trackingUrl ? `
              <a href="${shipping.trackingUrl}" class="shipping-info__tracking-link" target="_blank">
                ${getTranslation('order.trackPackage', locale)}
              </a>
            ` : ''}
          </div>
        ` : ''}
        
        ${shipping.estimatedDelivery ? `
          <div class="shipping-info__delivery">
            <span class="shipping-info__label">${getTranslation('order.estimatedDelivery', locale)}:</span>
            <span class="shipping-info__value">${shipping.estimatedDelivery}</span>
          </div>
        ` : ''}
      </div>
    </div>
  `;
}

/**
 * BillingInfo Component
 * 
 * Renders the billing information for the order
 * 
 * @param {Object} props - Component props
 * @param {Object} props.billing - Billing information
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the billing information
 */
function BillingInfo({ billing, locale }) {
  if (!billing) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'billing-info--rtl' : '';
  
  return `
    <div class="billing-info ${rtlClass}">
      <h2 class="billing-info__title">${getTranslation('order.billingInformation', locale)}</h2>
      
      <div class="billing-info__content">
        <div class="billing-info__payment">
          <span class="billing-info__label">${getTranslation('order.paymentMethod', locale)}:</span>
          <div class="billing-info__payment-details">
            <div class="billing-info__payment-method">
              <span class="billing-info__card-type billing-info__card-type--${billing.payment.type.toLowerCase()}"></span>
              <span class="billing-info__card-number">•••• •••• •••• ${billing.payment.last4}</span>
            </div>
          </div>
        </div>
        
        <div class="billing-info__address">
          <span class="billing-info__label">${getTranslation('order.billingAddress', locale)}:</span>
          <div class="billing-info__address-details">
            <div class="billing-info__name">${billing.address.name}</div>
            <div class="billing-info__address-line">${billing.address.line1}</div>
            ${billing.address.line2 ? `<div class="billing-info__address-line">${billing.address.line2}</div>` : ''}
            <div class="billing-info__address-line">
              ${billing.address.city}, ${billing.address.state || ''} ${billing.address.postalCode}
            </div>
            <div class="billing-info__address-line">
              ${getTranslation(`order.countries.${billing.address.country}`, locale) || billing.address.country}
            </div>
            ${billing.address.phone ? `<div class="billing-info__phone">${billing.address.phone}</div>` : ''}
          </div>
        </div>
      </div>
    </div>
  `;
}

/**
 * OrderActions Component
 * 
 * Renders action buttons for the order (print, reorder, track)
 * 
 * @param {Object} props - Component props
 * @param {Object} props.order - Order data object
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the order actions
 */
function OrderActions({ order, locale }) {
  if (!order) return '';
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'order-actions--rtl' : '';
  
  // Define available actions based on order status
  const canReorder = ['Delivered', 'Completed', 'Cancelled'].includes(order.status);
  const canTrack = ['Shipped', 'OutForDelivery', 'Processing'].includes(order.status);
  const canCancel = ['Processing', 'Pending'].includes(order.status);
  
  return `
    <div class="order-actions ${rtlClass}">
      ${Button({
        text: getTranslation('order.printOrder', locale),
        variant: 'outline',
        icon: 'printer',
        attributes: `data-action="print-order" data-order-id="${order.id}"`,
        locale
      })}
      
      ${canReorder ? `
        ${Button({
          text: getTranslation('order.reorder', locale),
          variant: 'outline',
          icon: 'refresh',
          attributes: `data-action="reorder" data-order-id="${order.id}"`,
          locale
        })}
      ` : ''}
      
      ${canTrack && order.shipping && order.shipping.trackingUrl ? `
        ${Button({
          text: getTranslation('order.trackOrder', locale),
          variant: 'outline',
          icon: 'map-pin',
          attributes: `data-action="track-order" data-order-id="${order.id}" data-tracking-url="${order.shipping.trackingUrl}"`,
          locale
        })}
      ` : ''}
      
      ${canCancel ? `
        ${Button({
          text: getTranslation('order.cancelOrder', locale),
          variant: 'outline',
          icon: 'x',
          attributes: `data-action="cancel-order" data-order-id="${order.id}"`,
          locale
        })}
      ` : ''}
    </div>
  `;
}

/**
 * OrderSummary Template Component
 * 
 * A comprehensive template for displaying order summaries in social commerce applications
 * 
 * @param {Object} props - Component props
 * @param {Object} props.order - Order data object
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the order summary template
 */
function OrderSummary({ order, locale = 'en-US' }) {
  if (!order) {
    return `
      <div class="order-summary order-summary--not-found">
        <div class="order-summary__not-found">
          <h1>${getTranslation('order.notFound', locale)}</h1>
          <p>${getTranslation('order.notFoundMessage', locale)}</p>
          ${Button({
            text: getTranslation('order.backToOrders', locale),
            variant: 'primary',
            attributes: 'data-action="back-to-orders"',
            locale
          })}
        </div>
      </div>
    `;
  }
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'order-summary--rtl' : '';
  
  return `
    <div class="order-summary ${rtlClass}" id="order-summary" data-order-id="${order.id}">
      ${OrderHeader({ order, locale })}
      
      <div class="order-summary__container">
        <div class="order-summary__main">
          ${OrderItems({ items: order.items, currencySymbol: order.currencySymbol, locale })}
        </div>
        
        <div class="order-summary__sidebar">
          ${PriceSummary({ summary: order.summary, currencySymbol: order.currencySymbol, locale })}
        </div>
      </div>
      
      <div class="order-summary__info">
        <div class="order-summary__info-column">
          ${ShippingInfo({ shipping: order.shipping, locale })}
        </div>
        
        <div class="order-summary__info-column">
          ${BillingInfo({ billing: order.billing, locale })}
        </div>
      </div>
      
      <div class="order-summary__actions">
        ${OrderActions({ order, locale })}
      </div>
    </div>
    
    <!-- Order Summary Scripts -->
    <script>
      (function() {
        // DOM elements
        const orderSummary = document.getElementById('order-summary');
        if (!orderSummary) return;
        
        // Print order functionality
        const printOrderBtn = orderSummary.querySelector('[data-action="print-order"]');
        if (printOrderBtn) {
          printOrderBtn.addEventListener('click', () => {
            window.print();
          });
        }
        
        // Reorder functionality
        const reorderBtn = orderSummary.querySelector('[data-action="reorder"]');
        if (reorderBtn) {
          reorderBtn.addEventListener('click', () => {
            const orderId = reorderBtn.getAttribute('data-order-id');
            const event = new CustomEvent('orderReorder', {
              detail: { orderId }
            });
            orderSummary.dispatchEvent(event);
          });
        }
        
        // Track order functionality
        const trackOrderBtn = orderSummary.querySelector('[data-action="track-order"]');
        if (trackOrderBtn) {
          trackOrderBtn.addEventListener('click', () => {
            const trackingUrl = trackOrderBtn.getAttribute('data-tracking-url');
            if (trackingUrl) {
              window.open(trackingUrl, '_blank');
            }
          });
        }
        
        // Cancel order functionality
        const cancelOrderBtn = orderSummary.querySelector('[data-action="cancel-order"]');
        if (cancelOrderBtn) {
          cancelOrderBtn.addEventListener('click', () => {
            const orderId = cancelOrderBtn.getAttribute('data-order-id');
            if (confirm('Are you sure you want to cancel this order?')) {
              const event = new CustomEvent('orderCancel', {
                detail: { orderId }
              });
              orderSummary.dispatchEvent(event);
            }
          });
        }
        
        // Back to orders functionality
        const backToOrdersBtn = orderSummary.querySelector('[data-action="back-to-orders"]');
        if (backToOrdersBtn) {
          backToOrdersBtn.addEventListener('click', () => {
            const event = new CustomEvent('orderBackToOrders');
            orderSummary.dispatchEvent(event);
          });
        }
      })();
    </script>
  `;
}

module.exports = OrderSummary;
