/**
 * UserProfile Template
 * 
 * A comprehensive user profile template that displays user details, account settings,
 * order history, saved addresses, and other user-specific information.
 * 
 * This template is designed for the Social Commerce domain user account management.
 * 
 * Features:
 * - Profile information display and editing
 * - Account settings management
 * - Order history display
 * - Saved addresses management
 * - Wishlist/favorites display
 * - Payment methods management
 * - Security settings
 * - Notification preferences
 * - Social connections
 * - Responsive design for all screen sizes
 * - Localization support
 * 
 * @module templates/UserProfile
 * @requires components/Button
 * @requires components/FormElements
 * @requires utilities/localization
 * @requires utilities/design-tokens
 */

// Import required dependencies
const { getLocaleDirection, getTranslation } = require('../../utilities/localization');
const { colors, spacing, typography, borders, shadows } = require('../../utilities/design-tokens');
const Button = require('../../components/Button');
const { 
  TextField, 
  Select, 
  Checkbox, 
  RadioGroup 
} = require('../../components/FormElements');

/**
 * ProfileHeader Component
 * 
 * Renders the user profile header with avatar, name, and stats
 * 
 * @param {Object} props - Component props
 * @param {Object} props.user - User data object
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the profile header
 */
function ProfileHeader({ user, locale }) {
  if (!user) return '';
  
  const { 
    displayName, 
    avatar, 
    joinDate, 
    stats = {} 
  } = user;
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'profile-header--rtl' : '';
  
  // Format join date
  const formattedDate = new Date(joinDate).toLocaleDateString(locale, {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
  
  return `
    <div class="profile-header ${rtlClass}">
      <div class="profile-header__avatar-container">
        ${avatar ? `
          <img 
            src="${avatar}" 
            alt="${displayName}" 
            class="profile-header__avatar"
          />
        ` : `
          <div class="profile-header__avatar-placeholder">
            ${displayName.charAt(0).toUpperCase()}
          </div>
        `}
        <button class="profile-header__avatar-edit" aria-label="${getTranslation('profile.changeAvatar', locale)}">
          <i class="profile-header__avatar-edit-icon"></i>
        </button>
      </div>
      
      <div class="profile-header__info">
        <h1 class="profile-header__name">${displayName}</h1>
        <div class="profile-header__join-date">
          ${getTranslation('profile.memberSince', locale)}: ${formattedDate}
        </div>
        
        <div class="profile-header__stats">
          ${stats.orders ? `
            <div class="profile-header__stat">
              <span class="profile-header__stat-value">${stats.orders}</span>
              <span class="profile-header__stat-label">${getTranslation('profile.orders', locale)}</span>
            </div>
          ` : ''}
          
          ${stats.reviews ? `
            <div class="profile-header__stat">
              <span class="profile-header__stat-value">${stats.reviews}</span>
              <span class="profile-header__stat-label">${getTranslation('profile.reviews', locale)}</span>
            </div>
          ` : ''}
          
          ${stats.wishlistItems ? `
            <div class="profile-header__stat">
              <span class="profile-header__stat-value">${stats.wishlistItems}</span>
              <span class="profile-header__stat-label">${getTranslation('profile.wishlistItems', locale)}</span>
            </div>
          ` : ''}
        </div>
      </div>
    </div>
  `;
}

/**
 * ProfileNavigation Component
 * 
 * Renders the profile section navigation tabs
 * 
 * @param {Object} props - Component props
 * @param {string} props.activeSection - The currently active section
 * @param {Array} props.sections - Available sections
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the profile navigation
 */
function ProfileNavigation({ activeSection, sections, locale }) {
  if (!sections || sections.length === 0) {
    return '';
  }
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'profile-nav--rtl' : '';
  
  return `
    <nav class="profile-nav ${rtlClass}">
      <ul class="profile-nav__list">
        ${sections.map(section => `
          <li class="profile-nav__item">
            <a 
              href="#${section.id}" 
              class="profile-nav__link ${section.id === activeSection ? 'profile-nav__link--active' : ''}" 
              data-section="${section.id}"
            >
              <i class="profile-nav__icon profile-nav__icon--${section.id}"></i>
              <span class="profile-nav__text">${getTranslation(`profile.sections.${section.id}`, locale)}</span>
            </a>
          </li>
        `).join('')}
      </ul>
    </nav>
  `;
}

/**
 * PersonalInfo Component
 * 
 * Renders the user's personal information form
 * 
 * @param {Object} props - Component props
 * @param {Object} props.user - User data object
 * @param {string} props.locale - Current locale code
 * @param {boolean} props.editing - Whether the section is in edit mode
 * @returns {string} HTML for the personal info section
 */
function PersonalInfo({ user, locale, editing = false }) {
  if (!user) return '';
  
  const { 
    firstName, 
    lastName, 
    email, 
    phone, 
    birthDate, 
    gender 
  } = user;
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'personal-info--rtl' : '';
  
  // Default view mode
  if (!editing) {
    return `
      <div class="personal-info ${rtlClass}">
        <div class="personal-info__header">
          <h2 class="personal-info__title">${getTranslation('profile.personalInfo', locale)}</h2>
          <button 
            class="personal-info__edit-btn" 
            data-action="edit-personal-info"
          >
            ${getTranslation('profile.edit', locale)}
          </button>
        </div>
        
        <div class="personal-info__content">
          <div class="personal-info__field">
            <span class="personal-info__label">${getTranslation('profile.name', locale)}:</span>
            <span class="personal-info__value">${firstName} ${lastName}</span>
          </div>
          
          <div class="personal-info__field">
            <span class="personal-info__label">${getTranslation('profile.email', locale)}:</span>
            <span class="personal-info__value">${email}</span>
          </div>
          
          ${phone ? `
            <div class="personal-info__field">
              <span class="personal-info__label">${getTranslation('profile.phone', locale)}:</span>
              <span class="personal-info__value">${phone}</span>
            </div>
          ` : ''}
          
          ${birthDate ? `
            <div class="personal-info__field">
              <span class="personal-info__label">${getTranslation('profile.birthDate', locale)}:</span>
              <span class="personal-info__value">${new Date(birthDate).toLocaleDateString(locale)}</span>
            </div>
          ` : ''}
          
          ${gender ? `
            <div class="personal-info__field">
              <span class="personal-info__label">${getTranslation('profile.gender', locale)}:</span>
              <span class="personal-info__value">${getTranslation(`profile.gender.${gender}`, locale)}</span>
            </div>
          ` : ''}
        </div>
      </div>
    `;
  }
  
  // Edit mode
  return `
    <div class="personal-info personal-info--editing ${rtlClass}">
      <div class="personal-info__header">
        <h2 class="personal-info__title">${getTranslation('profile.personalInfo', locale)}</h2>
      </div>
      
      <form class="personal-info__form" data-form="personal-info">
        <div class="personal-info__form-grid">
          <div class="personal-info__form-group">
            ${TextField({
              label: getTranslation('profile.firstName', locale),
              id: 'first-name',
              name: 'firstName',
              value: firstName || '',
              required: true,
              placeholder: getTranslation('profile.firstNamePlaceholder', locale),
              locale
            })}
          </div>
          
          <div class="personal-info__form-group">
            ${TextField({
              label: getTranslation('profile.lastName', locale),
              id: 'last-name',
              name: 'lastName',
              value: lastName || '',
              required: true,
              placeholder: getTranslation('profile.lastNamePlaceholder', locale),
              locale
            })}
          </div>
          
          <div class="personal-info__form-group">
            ${TextField({
              label: getTranslation('profile.email', locale),
              id: 'email',
              name: 'email',
              type: 'email',
              value: email || '',
              required: true,
              placeholder: getTranslation('profile.emailPlaceholder', locale),
              locale
            })}
          </div>
          
          <div class="personal-info__form-group">
            ${TextField({
              label: getTranslation('profile.phone', locale),
              id: 'phone',
              name: 'phone',
              type: 'tel',
              value: phone || '',
              placeholder: getTranslation('profile.phonePlaceholder', locale),
              locale
            })}
          </div>
          
          <div class="personal-info__form-group">
            ${TextField({
              label: getTranslation('profile.birthDate', locale),
              id: 'birth-date',
              name: 'birthDate',
              type: 'date',
              value: birthDate || '',
              placeholder: getTranslation('profile.birthDatePlaceholder', locale),
              locale
            })}
          </div>
          
          <div class="personal-info__form-group">
            ${Select({
              label: getTranslation('profile.gender', locale),
              id: 'gender',
              name: 'gender',
              value: gender || '',
              options: [
                { value: '', label: getTranslation('profile.selectGender', locale) },
                { value: 'male', label: getTranslation('profile.gender.male', locale) },
                { value: 'female', label: getTranslation('profile.gender.female', locale) },
                { value: 'other', label: getTranslation('profile.gender.other', locale) },
                { value: 'prefer-not-to-say', label: getTranslation('profile.gender.preferNotToSay', locale) }
              ],
              locale
            })}
          </div>
        </div>
        
        <div class="personal-info__actions">
          ${Button({
            text: getTranslation('profile.save', locale),
            variant: 'primary',
            type: 'submit',
            attributes: 'data-action="save-personal-info"',
            locale
          })}
          
          ${Button({
            text: getTranslation('profile.cancel', locale),
            variant: 'outline',
            attributes: 'data-action="cancel-personal-info"',
            locale
          })}
        </div>
      </form>
    </div>
  `;
}
/**
 * Addresses Component
 * 
 * Renders the user's saved addresses and address management
 * 
 * @param {Object} props - Component props
 * @param {Array} props.addresses - User's saved addresses
 * @param {string} props.locale - Current locale code
 * @param {boolean} props.editing - Whether currently editing an address
 * @param {Object} props.currentAddress - The address being edited
 * @returns {string} HTML for the addresses section
 */
function Addresses({ addresses = [], locale, editing = false, currentAddress = null }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'addresses--rtl' : '';
  
  // If editing mode, show address form
  if (editing) {
    const address = currentAddress || {};
    const isNew = !address.id;
    
    return `
      <div class="addresses addresses--editing ${rtlClass}">
        <div class="addresses__header">
          <h2 class="addresses__title">
            ${isNew 
              ? getTranslation('profile.addNewAddress', locale)
              : getTranslation('profile.editAddress', locale)
            }
          </h2>
          <button 
            class="addresses__back-btn" 
            data-action="cancel-edit-address"
          >
            ${getTranslation('profile.back', locale)}
          </button>
        </div>
        
        <form class="addresses__form" data-form="address">
          ${address.id ? `<input type="hidden" name="addressId" value="${address.id}">` : ''}
          
          <div class="addresses__form-grid">
            <div class="addresses__form-group addresses__form-group--full">
              ${TextField({
                label: getTranslation('profile.addressName', locale),
                id: 'address-name',
                name: 'addressName',
                value: address.name || '',
                placeholder: getTranslation('profile.addressNamePlaceholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group addresses__form-group--full">
              ${TextField({
                label: getTranslation('profile.addressLine1', locale),
                id: 'address-line1',
                name: 'addressLine1',
                value: address.line1 || '',
                required: true,
                placeholder: getTranslation('profile.addressLine1Placeholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group addresses__form-group--full">
              ${TextField({
                label: getTranslation('profile.addressLine2', locale),
                id: 'address-line2',
                name: 'addressLine2',
                value: address.line2 || '',
                placeholder: getTranslation('profile.addressLine2Placeholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group">
              ${TextField({
                label: getTranslation('profile.city', locale),
                id: 'city',
                name: 'city',
                value: address.city || '',
                required: true,
                placeholder: getTranslation('profile.cityPlaceholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group">
              ${TextField({
                label: getTranslation('profile.state', locale),
                id: 'state',
                name: 'state',
                value: address.state || '',
                placeholder: getTranslation('profile.statePlaceholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group">
              ${TextField({
                label: getTranslation('profile.postalCode', locale),
                id: 'postal-code',
                name: 'postalCode',
                value: address.postalCode || '',
                required: true,
                placeholder: getTranslation('profile.postalCodePlaceholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group">
              ${Select({
                label: getTranslation('profile.country', locale),
                id: 'country',
                name: 'country',
                value: address.country || '',
                required: true,
                options: [
                  { value: '', label: getTranslation('profile.selectCountry', locale) },
                  { value: 'US', label: getTranslation('profile.countries.US', locale) },
                  { value: 'UK', label: getTranslation('profile.countries.UK', locale) },
                  { value: 'CA', label: getTranslation('profile.countries.CA', locale) },
                  { value: 'AU', label: getTranslation('profile.countries.AU', locale) }
                  // Add more countries as needed
                ],
                locale
              })}
            </div>
            
            <div class="addresses__form-group addresses__form-group--full">
              ${TextField({
                label: getTranslation('profile.phone', locale),
                id: 'address-phone',
                name: 'addressPhone',
                type: 'tel',
                value: address.phone || '',
                placeholder: getTranslation('profile.phonePlaceholder', locale),
                locale
              })}
            </div>
            
            <div class="addresses__form-group addresses__form-group--checkbox">
              ${Checkbox({
                label: getTranslation('profile.defaultShipping', locale),
                id: 'default-shipping',
                name: 'defaultShipping',
                checked: address.isDefaultShipping || false,
                locale
              })}
            </div>
            
            <div class="addresses__form-group addresses__form-group--checkbox">
              ${Checkbox({
                label: getTranslation('profile.defaultBilling', locale),
                id: 'default-billing',
                name: 'defaultBilling',
                checked: address.isDefaultBilling || false,
                locale
              })}
            </div>
          </div>
          
          <div class="addresses__actions">
            ${Button({
              text: getTranslation('profile.saveAddress', locale),
              variant: 'primary',
              type: 'submit',
              attributes: 'data-action="save-address"',
              locale
            })}
            
            ${Button({
              text: getTranslation('profile.cancel', locale),
              variant: 'outline',
              attributes: 'data-action="cancel-edit-address"',
              locale
            })}
          </div>
        </form>
      </div>
    `;
  }
  
  // Regular view mode
  return `
    <div class="addresses ${rtlClass}">
      <div class="addresses__header">
        <h2 class="addresses__title">${getTranslation('profile.savedAddresses', locale)}</h2>
        ${Button({
          text: getTranslation('profile.addNewAddress', locale),
          variant: 'outline',
          icon: 'plus',
          attributes: 'data-action="add-new-address"',
          locale
        })}
      </div>
      
      <div class="addresses__list">
        ${addresses.length === 0 ? `
          <div class="addresses__empty">
            <p>${getTranslation('profile.noAddresses', locale)}</p>
          </div>
        ` : addresses.map(address => `
          <div class="addresses__item" data-address-id="${address.id}">
            ${address.isDefaultShipping ? `
              <div class="addresses__badge addresses__badge--default-shipping">
                ${getTranslation('profile.defaultShipping', locale)}
              </div>
            ` : ''}
            
            ${address.isDefaultBilling ? `
              <div class="addresses__badge addresses__badge--default-billing">
                ${getTranslation('profile.defaultBilling', locale)}
              </div>
            ` : ''}
            
            <div class="addresses__content">
              ${address.name ? `<strong class="addresses__name">${address.name}</strong>` : ''}
              <p class="addresses__details">
                ${address.line1}<br>
                ${address.line2 ? `${address.line2}<br>` : ''}
                ${address.city}, ${address.state || ''} ${address.postalCode}<br>
                ${getTranslation(`profile.countries.${address.country}`, locale) || address.country}
              </p>
              ${address.phone ? `<p class="addresses__phone">${address.phone}</p>` : ''}
            </div>
            
            <div class="addresses__actions">
              ${Button({
                text: getTranslation('profile.edit', locale),
                variant: 'text',
                size: 'small',
                attributes: `data-action="edit-address" data-address-id="${address.id}"`,
                locale
              })}
              
              ${Button({
                text: getTranslation('profile.delete', locale),
                variant: 'text',
                size: 'small',
                attributes: `data-action="delete-address" data-address-id="${address.id}"`,
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
 * OrderHistory Component
 * 
 * Renders the user's order history
 * 
 * @param {Object} props - Component props
 * @param {Array} props.orders - User's order history
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the order history section
 */
function OrderHistory({ orders = [], locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'order-history--rtl' : '';
  
  return `
    <div class="order-history ${rtlClass}">
      <div class="order-history__header">
        <h2 class="order-history__title">${getTranslation('profile.orderHistory', locale)}</h2>
      </div>
      
      ${orders.length === 0 ? `
        <div class="order-history__empty">
          <p>${getTranslation('profile.noOrders', locale)}</p>
          ${Button({
            text: getTranslation('profile.startShopping', locale),
            variant: 'primary',
            attributes: 'data-action="start-shopping"',
            locale
          })}
        </div>
      ` : `
        <div class="order-history__list">
          ${orders.map(order => `
            <div class="order-history__item" data-order-id="${order.id}">
              <div class="order-history__item-header">
                <div class="order-history__item-info">
                  <div class="order-history__order-number">
                    <span class="order-history__label">${getTranslation('profile.orderNumber', locale)}:</span>
                    <span class="order-history__value">${order.number}</span>
                  </div>
                  
                  <div class="order-history__order-date">
                    <span class="order-history__label">${getTranslation('profile.orderDate', locale)}:</span>
                    <span class="order-history__value">
                      ${new Date(order.date).toLocaleDateString(locale)}
                    </span>
                  </div>
                </div>
                
                <div class="order-history__item-status order-history__item-status--${order.status.toLowerCase()}">
                  ${getTranslation(`profile.orderStatus.${order.status.toLowerCase()}`, locale) || order.status}
                </div>
              </div>
              
              <div class="order-history__item-content">
                <div class="order-history__item-products">
                  ${order.items.slice(0, 3).map(item => `
                    <div class="order-history__product">
                      <img 
                        src="${item.product.thumbnail}" 
                        alt="${item.product.name}" 
                        class="order-history__product-image"
                      />
                    </div>
                  `).join('')}
                  
                  ${order.items.length > 3 ? `
                    <div class="order-history__product order-history__product--more">
                      +${order.items.length - 3}
                    </div>
                  ` : ''}
                </div>
                
                <div class="order-history__item-total">
                  <span class="order-history__total-label">${getTranslation('profile.total', locale)}:</span>
                  <span class="order-history__total-value">
                    ${order.currencySymbol}${order.total.toFixed(2)}
                  </span>
                </div>
              </div>
              
              <div class="order-history__item-actions">
                ${Button({
                  text: getTranslation('profile.viewOrderDetails', locale),
                  variant: 'outline',
                  size: 'small',
                  attributes: `data-action="view-order" data-order-id="${order.id}"`,
                  locale
                })}
                
                ${order.status.toLowerCase() === 'delivered' ? `
                  ${Button({
                    text: getTranslation('profile.writeReview', locale),
                    variant: 'text',
                    size: 'small',
                    attributes: `data-action="write-review" data-order-id="${order.id}"`,
                    locale
                  })}
                ` : ''}
                
                ${order.status.toLowerCase() === 'processing' || order.status.toLowerCase() === 'pending' ? `
                  ${Button({
                    text: getTranslation('profile.cancelOrder', locale),
                    variant: 'text',
                    size: 'small',
                    attributes: `data-action="cancel-order" data-order-id="${order.id}"`,
                    locale
                  })}
                ` : ''}
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}

/**
 * WishlistItems Component
 * 
 * Renders the user's wishlist items
 * 
 * @param {Object} props - Component props
 * @param {Array} props.items - Wishlist items
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the wishlist section
 */
function WishlistItems({ items = [], locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'wishlist--rtl' : '';
  
  return `
    <div class="wishlist ${rtlClass}">
      <div class="wishlist__header">
        <h2 class="wishlist__title">${getTranslation('profile.wishlist', locale)}</h2>
        ${items.length > 0 ? `
          <div class="wishlist__count">${items.length} ${getTranslation(items.length === 1 ? 'profile.item' : 'profile.items', locale)}</div>
        ` : ''}
      </div>
      
      ${items.length === 0 ? `
        <div class="wishlist__empty">
          <p>${getTranslation('profile.noWishlistItems', locale)}</p>
          ${Button({
            text: getTranslation('profile.startShopping', locale),
            variant: 'primary',
            attributes: 'data-action="start-shopping"',
            locale
          })}
        </div>
      ` : `
        <div class="wishlist__grid">
          ${items.map(item => `
            <div class="wishlist__item" data-product-id="${item.product.id}">
              <button 
                class="wishlist__remove" 
                aria-label="${getTranslation('profile.removeFromWishlist', locale)}"
                data-action="remove-from-wishlist" 
                data-product-id="${item.product.id}"
              >
                <i class="wishlist__remove-icon"></i>
              </button>
              
              <div class="wishlist__image-container">
                <a href="/products/${item.product.slug || item.product.id}" class="wishlist__product-link">
                  <img 
                    src="${item.product.image || item.product.thumbnail}" 
                    alt="${item.product.name}" 
                    class="wishlist__image"
                  />
                </a>
              </div>
              
              <div class="wishlist__details">
                <h3 class="wishlist__name">
                  <a href="/products/${item.product.slug || item.product.id}" class="wishlist__product-link">
                    ${item.product.name}
                  </a>
                </h3>
                
                <div class="wishlist__price">
                  ${item.product.compareAtPrice ? `
                    <span class="wishlist__compare-price">
                      ${item.product.currencySymbol}${item.product.compareAtPrice.toFixed(2)}
                    </span>
                  ` : ''}
                  <span class="wishlist__current-price">
                    ${item.product.currencySymbol}${item.product.price.toFixed(2)}
                  </span>
                </div>
                
                ${!item.product.inStock ? `
                  <div class="wishlist__out-of-stock">
                    ${getTranslation('profile.outOfStock', locale)}
                  </div>
                ` : ''}
              </div>
              
              <div class="wishlist__actions">
                ${Button({
                  text: getTranslation('profile.addToCart', locale),
                  variant: 'primary',
                  size: 'small',
                  disabled: !item.product.inStock,
                  attributes: `data-action="add-to-cart" data-product-id="${item.product.id}"`,
                  locale
                })}
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}
/**
 * PaymentMethods Component
 * 
 * Renders the user's saved payment methods
 * 
 * @param {Object} props - Component props
 * @param {Array} props.paymentMethods - User's saved payment methods
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the payment methods section
 */
function PaymentMethods({ paymentMethods = [], locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'payment-methods--rtl' : '';
  
  return `
    <div class="payment-methods ${rtlClass}">
      <div class="payment-methods__header">
        <h2 class="payment-methods__title">${getTranslation('profile.paymentMethods', locale)}</h2>
        ${Button({
          text: getTranslation('profile.addPaymentMethod', locale),
          variant: 'outline',
          icon: 'plus',
          attributes: 'data-action="add-payment-method"',
          locale
        })}
      </div>
      
      ${paymentMethods.length === 0 ? `
        <div class="payment-methods__empty">
          <p>${getTranslation('profile.noPaymentMethods', locale)}</p>
        </div>
      ` : `
        <div class="payment-methods__list">
          ${paymentMethods.map(method => `
            <div class="payment-methods__item" data-payment-id="${method.id}">
              ${method.isDefault ? `
                <div class="payment-methods__badge">
                  ${getTranslation('profile.default', locale)}
                </div>
              ` : ''}
              
              <div class="payment-methods__card">
                <div class="payment-methods__card-type payment-methods__card-type--${method.type.toLowerCase()}">
                  <span class="payment-methods__card-logo payment-methods__card-logo--${method.type.toLowerCase()}"></span>
                </div>
                
                <div class="payment-methods__card-info">
                  <div class="payment-methods__card-number">
                    •••• •••• •••• ${method.last4}
                  </div>
                  
                  <div class="payment-methods__card-expiry">
                    ${getTranslation('profile.expires', locale)}: ${method.expiryMonth}/${method.expiryYear}
                  </div>
                  
                  ${method.name ? `
                    <div class="payment-methods__card-name">
                      ${method.name}
                    </div>
                  ` : ''}
                </div>
              </div>
              
              <div class="payment-methods__actions">
                ${!method.isDefault ? `
                  ${Button({
                    text: getTranslation('profile.makeDefault', locale),
                    variant: 'text',
                    size: 'small',
                    attributes: `data-action="set-default-payment" data-payment-id="${method.id}"`,
                    locale
                  })}
                ` : ''}
                
                ${Button({
                  text: getTranslation('profile.remove', locale),
                  variant: 'text',
                  size: 'small',
                  attributes: `data-action="remove-payment-method" data-payment-id="${method.id}"`,
                  locale
                })}
              </div>
            </div>
          `).join('')}
        </div>
      `}
    </div>
  `;
}

/**
 * SecuritySettings Component
 * 
 * Renders the user's security settings
 * 
 * @param {Object} props - Component props
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the security settings section
 */
function SecuritySettings({ locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'security-settings--rtl' : '';
  
  return `
    <div class="security-settings ${rtlClass}">
      <div class="security-settings__header">
        <h2 class="security-settings__title">${getTranslation('profile.securitySettings', locale)}</h2>
      </div>
      
      <div class="security-settings__content">
        <div class="security-settings__section">
          <h3 class="security-settings__subtitle">${getTranslation('profile.password', locale)}</h3>
          <div class="security-settings__password">
            <span class="security-settings__password-mask">••••••••</span>
            ${Button({
              text: getTranslation('profile.changePassword', locale),
              variant: 'outline',
              size: 'small',
              attributes: 'data-action="change-password"',
              locale
            })}
          </div>
        </div>
        
        <div class="security-settings__section">
          <h3 class="security-settings__subtitle">
            ${getTranslation('profile.twoFactorAuthentication', locale)}
          </h3>
          <div class="security-settings__toggle">
            <div class="security-settings__toggle-status security-settings__toggle-status--disabled">
              ${getTranslation('profile.disabled', locale)}
            </div>
            ${Button({
              text: getTranslation('profile.setup2FA', locale),
              variant: 'outline',
              size: 'small',
              attributes: 'data-action="setup-2fa"',
              locale
            })}
          </div>
          <p class="security-settings__description">
            ${getTranslation('profile.twoFactorDescription', locale)}
          </p>
        </div>
        
        <div class="security-settings__section">
          <h3 class="security-settings__subtitle">
            ${getTranslation('profile.loginSessions', locale)}
          </h3>
          ${Button({
            text: getTranslation('profile.logoutAllDevices', locale),
            variant: 'outline',
            size: 'small',
            attributes: 'data-action="logout-all-devices"',
            locale
          })}
          <p class="security-settings__description">
            ${getTranslation('profile.loginSessionsDescription', locale)}
          </p>
        </div>
      </div>
    </div>
  `;
}

/**
 * NotificationPreferences Component
 * 
 * Renders the user's notification preferences
 * 
 * @param {Object} props - Component props
 * @param {Object} props.preferences - User's notification preferences
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the notification preferences section
 */
function NotificationPreferences({ preferences = {}, locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'notification-prefs--rtl' : '';
  
  const defaultPrefs = {
    orderUpdates: true,
    promotions: false,
    productUpdates: false,
    reviews: true,
    ...preferences
  };
  
  return `
    <div class="notification-prefs ${rtlClass}">
      <div class="notification-prefs__header">
        <h2 class="notification-prefs__title">${getTranslation('profile.notificationPreferences', locale)}</h2>
      </div>
      
      <form class="notification-prefs__form" data-form="notification-preferences">
        <div class="notification-prefs__option">
          ${Checkbox({
            label: getTranslation('profile.notificationTypes.orderUpdates', locale),
            id: 'notification-order-updates',
            name: 'orderUpdates',
            checked: defaultPrefs.orderUpdates,
            locale
          })}
          <p class="notification-prefs__description">
            ${getTranslation('profile.notificationDescriptions.orderUpdates', locale)}
          </p>
        </div>
        
        <div class="notification-prefs__option">
          ${Checkbox({
            label: getTranslation('profile.notificationTypes.promotions', locale),
            id: 'notification-promotions',
            name: 'promotions',
            checked: defaultPrefs.promotions,
            locale
          })}
          <p class="notification-prefs__description">
            ${getTranslation('profile.notificationDescriptions.promotions', locale)}
          </p>
        </div>
        
        <div class="notification-prefs__option">
          ${Checkbox({
            label: getTranslation('profile.notificationTypes.productUpdates', locale),
            id: 'notification-product-updates',
            name: 'productUpdates',
            checked: defaultPrefs.productUpdates,
            locale
          })}
          <p class="notification-prefs__description">
            ${getTranslation('profile.notificationDescriptions.productUpdates', locale)}
          </p>
        </div>
        
        <div class="notification-prefs__option">
          ${Checkbox({
            label: getTranslation('profile.notificationTypes.reviews', locale),
            id: 'notification-reviews',
            name: 'reviews',
            checked: defaultPrefs.reviews,
            locale
          })}
          <p class="notification-prefs__description">
            ${getTranslation('profile.notificationDescriptions.reviews', locale)}
          </p>
        </div>
        
        <div class="notification-prefs__actions">
          ${Button({
            text: getTranslation('profile.savePreferences', locale),
            variant: 'primary',
            type: 'submit',
            attributes: 'data-action="save-notification-preferences"',
            locale
          })}
        </div>
      </form>
    </div>
  `;
}

/**
 * SocialConnections Component
 * 
 * Renders the user's linked social accounts
 * 
 * @param {Object} props - Component props
 * @param {Object} props.connections - User's social connections
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the social connections section
 */
function SocialConnections({ connections = {}, locale }) {
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'social-connections--rtl' : '';
  
  // Platform data with connection status
  const platforms = [
    {
      id: 'facebook',
      name: 'Facebook',
      connected: !!connections.facebook,
      icon: 'facebook'
    },
    {
      id: 'google',
      name: 'Google',
      connected: !!connections.google,
      icon: 'google'
    },
    {
      id: 'twitter',
      name: 'Twitter',
      connected: !!connections.twitter,
      icon: 'twitter'
    },
    {
      id: 'apple',
      name: 'Apple',
      connected: !!connections.apple,
      icon: 'apple'
    }
  ];
  
  return `
    <div class="social-connections ${rtlClass}">
      <div class="social-connections__header">
        <h2 class="social-connections__title">${getTranslation('profile.connectedAccounts', locale)}</h2>
      </div>
      
      <div class="social-connections__list">
        ${platforms.map(platform => `
          <div class="social-connections__item">
            <div class="social-connections__platform">
              <span class="social-connections__icon social-connections__icon--${platform.icon}"></span>
              <span class="social-connections__name">${platform.name}</span>
            </div>
            
            <div class="social-connections__status">
              ${platform.connected ? `
                <span class="social-connections__status-text social-connections__status-text--connected">
                  ${getTranslation('profile.connected', locale)}
                </span>
                ${Button({
                  text: getTranslation('profile.disconnect', locale),
                  variant: 'text',
                  size: 'small',
                  attributes: `data-action="disconnect-social" data-platform="${platform.id}"`,
                  locale
                })}
              ` : `
                <span class="social-connections__status-text social-connections__status-text--disconnected">
                  ${getTranslation('profile.notConnected', locale)}
                </span>
                ${Button({
                  text: getTranslation('profile.connect', locale),
                  variant: 'outline',
                  size: 'small',
                  attributes: `data-action="connect-social" data-platform="${platform.id}"`,
                  locale
                })}
              `}
            </div>
          </div>
        `).join('')}
      </div>
    </div>
  `;
}

/**
 * UserProfile Template Component
 * 
 * A comprehensive template for user profile management in social commerce applications
 * 
 * @param {Object} props - Component props
 * @param {Object} props.user - User data object
 * @param {Array} props.orders - User's order history
 * @param {Array} props.addresses - User's saved addresses
 * @param {Array} props.wishlistItems - User's wishlist items
 * @param {Array} props.paymentMethods - User's payment methods
 * @param {Object} props.notificationPreferences - User's notification preferences
 * @param {Object} props.socialConnections - User's connected social accounts
 * @param {string} props.activeSection - The currently active section
 * @param {Object} props.editStates - The current edit states
 * @param {string} props.locale - Current locale code
 * @returns {string} HTML for the user profile template
 */
function UserProfile({
  user,
  orders = [],
  addresses = [],
  wishlistItems = [],
  paymentMethods = [],
  notificationPreferences = {},
  socialConnections = {},
  activeSection = 'personal-info',
  editStates = {},
  locale = 'en-US'
}) {
  if (!user) {
    return `
      <div class="user-profile user-profile--not-found">
        <div class="user-profile__not-found">
          <h1>${getTranslation('profile.userNotFound', locale)}</h1>
          <p>${getTranslation('profile.pleaseLogin', locale)}</p>
          ${Button({
            text: getTranslation('profile.login', locale),
            variant: 'primary',
            attributes: 'data-action="login"',
            locale
          })}
        </div>
      </div>
    `;
  }
  
  const direction = getLocaleDirection(locale);
  const rtlClass = direction === 'rtl' ? 'user-profile--rtl' : '';
  
  // Define available profile sections
  const sections = [
    { id: 'personal-info', icon: 'user' },
    { id: 'orders', icon: 'package' },
    { id: 'addresses', icon: 'map-pin' },
    { id: 'wishlist', icon: 'heart' },
    { id: 'payment-methods', icon: 'credit-card' },
    { id: 'security', icon: 'shield' },
    { id: 'notifications', icon: 'bell' },
    { id: 'social-connections', icon: 'share' }
  ];
  
  return `
    <div class="user-profile ${rtlClass}" id="user-profile">
      ${ProfileHeader({ user, locale })}
      
      <div class="user-profile__container">
        <div class="user-profile__sidebar">
          ${ProfileNavigation({ activeSection, sections, locale })}
        </div>
        
        <div class="user-profile__content">
          <div class="user-profile__section ${activeSection === 'personal-info' ? 'user-profile__section--active' : ''}" id="section-personal-info">
            ${PersonalInfo({ 
              user, 
              locale, 
              editing: editStates.personalInfo
            })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'orders' ? 'user-profile__section--active' : ''}" id="section-orders">
            ${OrderHistory({ orders, locale })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'addresses' ? 'user-profile__section--active' : ''}" id="section-addresses">
            ${Addresses({ 
              addresses, 
              locale, 
              editing: editStates.addresses,
              currentAddress: editStates.currentAddress
            })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'wishlist' ? 'user-profile__section--active' : ''}" id="section-wishlist">
            ${WishlistItems({ items: wishlistItems, locale })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'payment-methods' ? 'user-profile__section--active' : ''}" id="section-payment-methods">
            ${PaymentMethods({ paymentMethods, locale })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'security' ? 'user-profile__section--active' : ''}" id="section-security">
            ${SecuritySettings({ locale })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'notifications' ? 'user-profile__section--active' : ''}" id="section-notifications">
            ${NotificationPreferences({ preferences: notificationPreferences, locale })}
          </div>
          
          <div class="user-profile__section ${activeSection === 'social-connections' ? 'user-profile__section--active' : ''}" id="section-social-connections">
            ${SocialConnections({ connections: socialConnections, locale })}
          </div>
        </div>
      </div>
    </div>
    
    <!-- User Profile Scripts -->
    <script>
      (function() {
        // DOM elements
        const userProfile = document.getElementById('user-profile');
        if (!userProfile) return;
        
        // Navigation functionality
        const initNavigation = () => {
          const navLinks = userProfile.querySelectorAll('.profile-nav__link');
          
          navLinks.forEach(link => {
            link.addEventListener('click', (e) => {
              e.preventDefault();
              
              // Get the section to show
              const sectionId = link.dataset.section;
              
              // Update active state in navigation
              navLinks.forEach(navLink => {
                navLink.classList.remove('profile-nav__link--active');
              });
              link.classList.add('profile-nav__link--active');
              
              // Update visible section
              const sections = userProfile.querySelectorAll('.user-profile__section');
              sections.forEach(section => {
                section.classList.remove('user-profile__section--active');
              });
              
              const activeSection = userProfile.querySelector(\`#section-\${sectionId}\`);
              if (activeSection) {
                activeSection.classList.add('user-profile__section--active');
              }
              
              // Dispatch event for section change
              const event = new CustomEvent('profileSectionChange', {
                detail: { section: sectionId }
              });
              userProfile.dispatchEvent(event);
            });
          });
        };
        
        // Personal info edit functionality
        const initPersonalInfo = () => {
          const editBtn = userProfile.querySelector('[data-action="edit-personal-info"]');
          const cancelBtn = userProfile.querySelector('[data-action="cancel-personal-info"]');
          const saveBtn = userProfile.querySelector('[data-action="save-personal-info"]');
          const personalInfoForm = userProfile.querySelector('[data-form="personal-info"]');
          
          if (editBtn) {
            editBtn.addEventListener('click', () => {
              const event = new CustomEvent('profileEditPersonalInfo', {
                detail: { edit: true }
              });
              userProfile.dispatchEvent(event);
            });
          }
          
          if (cancelBtn) {
            cancelBtn.addEventListener('click', () => {
              const event = new CustomEvent('profileEditPersonalInfo', {
                detail: { edit: false }
              });
              userProfile.dispatchEvent(event);
            });
          }
          
          if (personalInfoForm) {
            personalInfoForm.addEventListener('submit', (e) => {
              e.preventDefault();
              
              // Collect form data
              const formData = new FormData(personalInfoForm);
              const data = Object.fromEntries(formData.entries());
              
              // Dispatch save event
              const event = new CustomEvent('profileSavePersonalInfo', {
                detail: { data }
              });
              userProfile.dispatchEvent(event);
            });
          }
        };
        
        // Addresses functionality
        const initAddresses = () => {
          // Add new address
          const addAddressBtn = userProfile.querySelector('[data-action="add-new-address"]');
          if (addAddressBtn) {
            addAddressBtn.addEventListener('click', () => {
              const event = new CustomEvent('profileEditAddress', {
                detail: { edit: true, address: null }
              });
              userProfile.dispatchEvent(event);
            });
          }
          
          // Edit address
          const editAddressBtns = userProfile.querySelectorAll('[data-action="edit-address"]');
          editAddressBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const addressId = btn.dataset.addressId;
              const event = new CustomEvent('profileEditAddress', {
                detail: { edit: true, addressId }
              });
              userProfile.dispatchEvent(event);
            });
          });
          
          // Delete address
          const deleteAddressBtns = userProfile.querySelectorAll('[data-action="delete-address"]');
          deleteAddressBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const addressId = btn.dataset.addressId;
              if (confirm('Are you sure you want to delete this address?')) {
                const event = new CustomEvent('profileDeleteAddress', {
                  detail: { addressId }
                });
                userProfile.dispatchEvent(event);
              }
            });
          });
          
          // Cancel edit address
          const cancelEditBtn = userProfile.querySelector('[data-action="cancel-edit-address"]');
          if (cancelEditBtn) {
            cancelEditBtn.addEventListener('click', () => {
              const event = new CustomEvent('profileEditAddress', {
                detail: { edit: false }
              });
              userProfile.dispatchEvent(event);
            });
          }
          
          // Save address form
          const addressForm = userProfile.querySelector('[data-form="address"]');
          if (addressForm) {
            addressForm.addEventListener('submit', (e) => {
              e.preventDefault();
              
              // Collect form data
              const formData = new FormData(addressForm);
              const data = Object.fromEntries(formData.entries());
              
              // Convert checkbox values
              data.defaultShipping = formData.has('defaultShipping');
              data.defaultBilling = formData.has('defaultBilling');
              
              // Dispatch save event
              const event = new CustomEvent('profileSaveAddress', {
                detail: { data }
              });
              userProfile.dispatchEvent(event);
            });
          }
        };
        
        // Order history functionality
        const initOrderHistory = () => {
          const viewOrderBtns = userProfile.querySelectorAll('[data-action="view-order"]');
          viewOrderBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const orderId = btn.dataset.orderId;
              const event = new CustomEvent('profileViewOrder', {
                detail: { orderId }
              });
              userProfile.dispatchEvent(event);
            });
          });
          
          const writeReviewBtns = userProfile.querySelectorAll('[data-action="write-review"]');
          writeReviewBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const orderId = btn.dataset.orderId;
              const event = new CustomEvent('profileWriteReview', {
                detail: { orderId }
              });
              userProfile.dispatchEvent(event);
            });
          });
          
          const cancelOrderBtns = userProfile.querySelectorAll('[data-action="cancel-order"]');
          cancelOrderBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const orderId = btn.dataset.orderId;
              if (confirm('Are you sure you want to cancel this order?')) {
                const event = new CustomEvent('profileCancelOrder', {
                  detail: { orderId }
                });
                userProfile.dispatchEvent(event);
              }
            });
          });
        };
        
        // Wishlist functionality
        const initWishlist = () => {
          const removeFromWishlistBtns = userProfile.querySelectorAll('[data-action="remove-from-wishlist"]');
          removeFromWishlistBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const productId = btn.dataset.productId;
              const event = new CustomEvent('profileRemoveFromWishlist', {
                detail: { productId }
              });
              userProfile.dispatchEvent(event);
            });
          });
          
          const addToCartBtns = userProfile.querySelectorAll('[data-action="add-to-cart"]');
          addToCartBtns.forEach(btn => {
            btn.addEventListener('click', () => {
              const productId = btn.dataset.productId;
              const event = new CustomEvent('profileAddToCart', {
                detail: { productId }
              });
              userProfile.dispatchEvent(event);
            });
          });
        };
        
        // Initialize all functionality
        const init = () => {
          initNavigation();
          initPersonalInfo();
          initAddresses();
          initOrderHistory();
          initWishlist();
        };
        
        // Start initialization when DOM is fully loaded
        if (document.readyState === 'loading') {
          document.addEventListener('DOMContentLoaded', init);
        } else {
          init();
        }
      })();
    </script>
  `;
}

module.exports = UserProfile;
