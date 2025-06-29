/**
 * MobileNavigation Component
 * Responsive navigation system for mobile devices with localization support
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Button = require('../Button');

/**
 * MobileNavigation component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the mobile navigation
 */
function MobileNavigation({
  id,
  items = [],
  activeItemId = null,
  logo = '',
  logoAlt = 'Logo',
  searchEnabled = true,
  cartEnabled = true,
  cartItemCount = 0,
  profileEnabled = true,
  showNotifications = false,
  notificationCount = 0,
  locale = localization.DEFAULT_LOCALE,
  onItemClick,
  onProfileClick,
  onCartClick,
  onSearchClick,
  onLogoClick,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const navId = id || `mobile-nav-${Math.random().toString(36).substring(2, 9)}`;
  
  // Translations based on locale
  const translations = getTranslations(locale);
  
  // Generate menu items HTML
  const menuItemsHtml = items.map(item => {
    const isActive = activeItemId === item.id;
    
    return `
      <li 
        class="nav-item ${isActive ? 'active' : ''}" 
        style="
          list-style-type: none;
          margin: 0;
          padding: 0;
        "
      >
        <a 
          href="${item.href || '#'}" 
          id="nav-item-${item.id}"
          onclick="${onItemClick ? `${onItemClick}('${item.id}')` : ''}"
          style="
            display: flex;
            align-items: center;
            gap: ${tokens.spacing[2]};
            padding: ${tokens.spacing[3]} ${tokens.spacing[4]};
            color: ${isActive ? tokens.colors.brand.primary[500] : tokens.colors.neutral.gray[700]};
            text-decoration: none;
            font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
            font-weight: ${isActive ? tokens.typography.fontWeights.medium : tokens.typography.fontWeights.regular};
            transition: ${tokens.animations.transitions.all};
            position: relative;
            
            &:hover {
              color: ${tokens.colors.brand.primary[600]};
              background-color: ${tokens.colors.neutral.gray[50]};
            }
          "
        >
          ${item.icon ? `
            <i class="${item.icon}" style="font-size: 1.25em;"></i>
          ` : ''}
          <span>${item.label}</span>
          ${isActive ? `
            <span 
              class="active-indicator" 
              style="
                position: absolute;
                ${dir === 'rtl' ? 'right' : 'left'}: 0;
                top: 0;
                bottom: 0;
                width: 3px;
                background-color: ${tokens.colors.brand.primary[500]};
              "
            ></span>
          ` : ''}
        </a>
      </li>
    `;
  }).join('');
  
  // Create drawer menu for mobile
  const drawerHtml = `
    <div 
      id="${navId}-drawer" 
      class="mobile-nav-drawer" 
      style="
        position: fixed;
        top: 0;
        ${dir === 'rtl' ? 'right' : 'left'}: 0;
        bottom: 0;
        width: 280px;
        background-color: ${tokens.colors.neutral.white};
        box-shadow: ${tokens.shadows.presets.drawer.standard};
        z-index: 1000;
        transform: translateX(${dir === 'rtl' ? '100%' : '-100%'});
        transition: transform 0.3s ${tokens.animations.easings.easeInOut};
        display: flex;
        flex-direction: column;
        overflow-y: auto;
      "
    >
      <div class="drawer-header" style="
        padding: ${tokens.spacing[4]};
        display: flex;
        align-items: center;
        justify-content: space-between;
        border-bottom: ${tokens.borders.presets.divider.light};
      ">
        <div class="logo" style="display: flex; align-items: center; cursor: pointer;" onclick="${onLogoClick || ''}">
          ${logo ? `<img src="${logo}" alt="${logoAlt}" style="height: 32px;" />` : ''}
        </div>
        <button 
          class="close-menu" 
          aria-label="${translations.closeMenu}"
          onclick="closeMobileMenu()"
          style="
            background: none;
            border: none;
            font-size: 24px;
            cursor: pointer;
            color: ${tokens.colors.neutral.gray[700]};
            padding: ${tokens.spacing[2]};
          "
        >
          <i class="icon-close"></i>
        </button>
      </div>
      
      ${searchEnabled ? `
        <div class="search-bar" style="
          padding: ${tokens.spacing[3]} ${tokens.spacing[4]};
          border-bottom: ${tokens.borders.presets.divider.light};
        ">
          <div 
            class="search-input"
            onclick="${onSearchClick || ''}"
            style="
              display: flex;
              align-items: center;
              gap: ${tokens.spacing[2]};
              padding: ${tokens.spacing[2]} ${tokens.spacing[3]};
              background-color: ${tokens.colors.neutral.gray[100]};
              border-radius: ${tokens.borders.borderRadius.md};
              cursor: pointer;
            "
          >
            <i class="icon-search" style="color: ${tokens.colors.neutral.gray[500]};"></i>
            <span style="color: ${tokens.colors.neutral.gray[500]};">${translations.searchPlaceholder}</span>
          </div>
        </div>
      ` : ''}
      
      <nav class="drawer-nav" style="flex: 1;">
        <ul style="
          padding: 0;
          margin: 0;
        ">
          ${menuItemsHtml}
        </ul>
      </nav>
      
      <div class="drawer-footer" style="
        padding: ${tokens.spacing[4]};
        border-top: ${tokens.borders.presets.divider.light};
      ">
        ${profileEnabled ? `
          <div 
            class="profile-link"
            onclick="${onProfileClick || ''}" 
            style="
              display: flex;
              align-items: center;
              gap: ${tokens.spacing[3]};
              padding: ${tokens.spacing[3]} 0;
              cursor: pointer;
            "
          >
            <div style="
              width: 40px;
              height: 40px;
              border-radius: 50%;
              background-color: ${tokens.colors.neutral.gray[200]};
              display: flex;
              align-items: center;
              justify-content: center;
            ">
              <i class="icon-user" style="color: ${tokens.colors.neutral.gray[500]};"></i>
            </div>
            <div>
              <div style="font-weight: ${tokens.typography.fontWeights.medium};">${translations.myAccount}</div>
              <div style="font-size: ${tokens.typography.textStyles.bodySmall.fontSize}; color: ${tokens.colors.neutral.gray[600]};">${translations.viewProfile}</div>
            </div>
          </div>
        ` : ''}
      </div>
    </div>
  `;
  
  // Create overlay for mobile drawer
  const overlayHtml = `
    <div 
      id="${navId}-overlay" 
      class="mobile-nav-overlay" 
      onclick="closeMobileMenu()"
      style="
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background-color: rgba(0, 0, 0, 0.5);
        z-index: 999;
        opacity: 0;
        visibility: hidden;
        transition: opacity 0.3s ${tokens.animations.easings.easeInOut}, visibility 0.3s ${tokens.animations.easings.easeInOut};
      "
    ></div>
  `;
  
  // Create mobile header
  const mobileHeaderHtml = `
    <header 
      id="${navId}-header" 
      class="mobile-header ${className}" 
      dir="${dir}"
      style="
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: ${tokens.spacing[3]} ${tokens.spacing[4]};
        background-color: ${tokens.colors.neutral.white};
        box-shadow: ${tokens.shadows.presets.header.bottom};
        position: sticky;
        top: 0;
        z-index: 100;
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div class="mobile-header-left" style="display: flex; align-items: center; gap: ${tokens.spacing[3]};">
        <button 
          class="menu-toggle" 
          aria-label="${translations.openMenu}"
          onclick="openMobileMenu()"
          style="
            background: none;
            border: none;
            font-size: 24px;
            cursor: pointer;
            color: ${tokens.colors.neutral.gray[700]};
            padding: ${tokens.spacing[2]};
          "
        >
          <i class="icon-menu"></i>
        </button>
        
        <div class="logo" style="display: flex; align-items: center; cursor: pointer;" onclick="${onLogoClick || ''}">
          ${logo ? `<img src="${logo}" alt="${logoAlt}" style="height: 32px;" />` : ''}
        </div>
      </div>
      
      <div class="mobile-header-right" style="display: flex; align-items: center; gap: ${tokens.spacing[3]};">
        ${searchEnabled ? `
          <button 
            class="search-button" 
            aria-label="${translations.search}"
            onclick="${onSearchClick || ''}"
            style="
              background: none;
              border: none;
              font-size: 20px;
              cursor: pointer;
              color: ${tokens.colors.neutral.gray[700]};
              padding: ${tokens.spacing[2]};
            "
          >
            <i class="icon-search"></i>
          </button>
        ` : ''}
        
        ${cartEnabled ? `
          <button 
            class="cart-button" 
            aria-label="${translations.cart}"
            onclick="${onCartClick || ''}"
            style="
              background: none;
              border: none;
              font-size: 20px;
              cursor: pointer;
              color: ${tokens.colors.neutral.gray[700]};
              padding: ${tokens.spacing[2]};
              position: relative;
            "
          >
            <i class="icon-cart"></i>
            ${cartItemCount > 0 ? `
              <span 
                class="cart-badge" 
                style="
                  position: absolute;
                  top: 0;
                  ${dir === 'rtl' ? 'left' : 'right'}: 0;
                  background-color: ${tokens.colors.semantic.error.standard};
                  color: white;
                  font-size: 10px;
                  font-weight: ${tokens.typography.fontWeights.bold};
                  min-width: 16px;
                  height: 16px;
                  border-radius: 8px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  padding: 0 4px;
                "
              >
                ${cartItemCount}
              </span>
            ` : ''}
          </button>
        ` : ''}
        
        ${showNotifications ? `
          <button 
            class="notifications-button" 
            aria-label="${translations.notifications}"
            style="
              background: none;
              border: none;
              font-size: 20px;
              cursor: pointer;
              color: ${tokens.colors.neutral.gray[700]};
              padding: ${tokens.spacing[2]};
              position: relative;
            "
          >
            <i class="icon-bell"></i>
            ${notificationCount > 0 ? `
              <span 
                class="notification-badge" 
                style="
                  position: absolute;
                  top: 0;
                  ${dir === 'rtl' ? 'left' : 'right'}: 0;
                  background-color: ${tokens.colors.semantic.error.standard};
                  color: white;
                  font-size: 10px;
                  font-weight: ${tokens.typography.fontWeights.bold};
                  min-width: 16px;
                  height: 16px;
                  border-radius: 8px;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  padding: 0 4px;
                "
              >
                ${notificationCount}
              </span>
            ` : ''}
          </button>
        ` : ''}
      </div>
    </header>
  `;
  
  // JavaScript for mobile menu functionality
  const menuScriptHtml = `
    <script>
      function openMobileMenu() {
        const drawer = document.getElementById('${navId}-drawer');
        const overlay = document.getElementById('${navId}-overlay');
        
        if (drawer && overlay) {
          drawer.style.transform = 'translateX(0)';
          overlay.style.visibility = 'visible';
          overlay.style.opacity = '1';
          document.body.style.overflow = 'hidden';
        }
      }
      
      function closeMobileMenu() {
        const drawer = document.getElementById('${navId}-drawer');
        const overlay = document.getElementById('${navId}-overlay');
        
        if (drawer && overlay) {
          drawer.style.transform = 'translateX(${dir === 'rtl' ? '100%' : '-100%'})';
          overlay.style.visibility = 'hidden';
          overlay.style.opacity = '0';
          document.body.style.overflow = '';
        }
      }
    </script>
  `;
  
  // Combine all elements
  return `
    ${mobileHeaderHtml}
    ${drawerHtml}
    ${overlayHtml}
    ${menuScriptHtml}
  `;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      openMenu: 'Open menu',
      closeMenu: 'Close menu',
      search: 'Search',
      searchPlaceholder: 'Search...',
      cart: 'Shopping cart',
      notifications: 'Notifications',
      myAccount: 'My Account',
      viewProfile: 'View profile'
    },
    ar: {
      openMenu: 'فتح القائمة',
      closeMenu: 'إغلاق القائمة',
      search: 'بحث',
      searchPlaceholder: 'بحث...',
      cart: 'عربة التسوق',
      notifications: 'إشعارات',
      myAccount: 'حسابي',
      viewProfile: 'عرض الملف الشخصي'
    },
    fr: {
      openMenu: 'Ouvrir le menu',
      closeMenu: 'Fermer le menu',
      search: 'Rechercher',
      searchPlaceholder: 'Rechercher...',
      cart: 'Panier',
      notifications: 'Notifications',
      myAccount: 'Mon Compte',
      viewProfile: 'Voir le profil'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

// Export the MobileNavigation component
module.exports = MobileNavigation;
