/**
 * Localization utilities for the Social Commerce Ecosystem
 * Provides functions for internationalization (i18n) and localization (l10n)
 * Designed for use by both Team Alpha and Team Omega
 */

// Default locale and fallback settings
const DEFAULT_LOCALE = 'en-US';
const DEFAULT_DIRECTION = 'ltr';
const RTL_LOCALES = ['ar', 'he', 'fa', 'ur'];

/**
 * Determines if a locale is RTL (right-to-left)
 * @param {string} locale - The locale code to check
 * @returns {boolean} - True if locale is RTL
 */
function isRtlLocale(locale) {
  if (!locale) return false;
  
  // Check if the locale starts with any RTL language code
  return RTL_LOCALES.some(rtlCode => 
    locale.toLowerCase().startsWith(rtlCode.toLowerCase())
  );
}

/**
 * Gets text direction based on locale
 * @param {string} locale - The locale code
 * @returns {string} - 'rtl' or 'ltr'
 */
function getTextDirection(locale) {
  return isRtlLocale(locale) ? 'rtl' : 'ltr';
}

/**
 * Gets appropriate locale-specific font family
 * @param {string} locale - The locale code
 * @param {object} typography - Typography tokens
 * @returns {string} - Font family stack
 */
function getLocalizedFontFamily(locale, typography) {
  if (!locale || !typography) {
    return typography?.fontFamilies?.primary?.regular || '';
  }
  
  // Map locale to appropriate font family
  const localePrefix = locale.split('-')[0].toLowerCase();
  
  if (localePrefix === 'ar' || localePrefix === 'fa' || localePrefix === 'ur') {
    return typography.fontFamilies.regional.arabic;
  } else if (localePrefix === 'zh') {
    return typography.fontFamilies.regional.chinese;
  } else if (localePrefix === 'ja') {
    return typography.fontFamilies.regional.japanese;
  } else if (localePrefix === 'ko') {
    return typography.fontFamilies.regional.korean;
  } else if (localePrefix === 'th') {
    return typography.fontFamilies.regional.thai;
  } else if (localePrefix === 'hi' || localePrefix === 'mr' || localePrefix === 'ne') {
    return typography.fontFamilies.regional.devanagari;
  }
  
  // Default to primary font
  return typography.fontFamilies.primary.regular;
}

/**
 * Returns locale-specific formatting for dates
 * @param {Date} date - Date to format
 * @param {string} locale - Target locale
 * @param {object} options - Intl.DateTimeFormat options
 * @returns {string} - Formatted date string
 */
function formatLocalizedDate(date, locale = DEFAULT_LOCALE, options = {}) {
  const defaultOptions = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  };
  
  try {
    const formatter = new Intl.DateTimeFormat(
      locale, 
      { ...defaultOptions, ...options }
    );
    return formatter.format(date);
  } catch (error) {
    console.error(`Error formatting date for locale ${locale}:`, error);
    // Fallback to default locale
    const fallbackFormatter = new Intl.DateTimeFormat(
      DEFAULT_LOCALE, 
      { ...defaultOptions, ...options }
    );
    return fallbackFormatter.format(date);
  }
}

/**
 * Returns locale-specific formatting for currency
 * @param {number} amount - Amount to format
 * @param {string} currencyCode - ISO currency code (e.g., USD, EUR)
 * @param {string} locale - Target locale
 * @returns {string} - Formatted currency string
 */
function formatLocalizedCurrency(amount, currencyCode = 'USD', locale = DEFAULT_LOCALE) {
  try {
    const formatter = new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: currencyCode,
    });
    return formatter.format(amount);
  } catch (error) {
    console.error(`Error formatting currency for locale ${locale} and currency ${currencyCode}:`, error);
    // Fallback to default locale
    const fallbackFormatter = new Intl.NumberFormat(DEFAULT_LOCALE, {
      style: 'currency',
      currency: currencyCode,
    });
    return fallbackFormatter.format(amount);
  }
}

/**
 * Returns a localized number format
 * @param {number} number - Number to format
 * @param {string} locale - Target locale
 * @param {object} options - Intl.NumberFormat options
 * @returns {string} - Formatted number string
 */
function formatLocalizedNumber(number, locale = DEFAULT_LOCALE, options = {}) {
  try {
    const formatter = new Intl.NumberFormat(locale, options);
    return formatter.format(number);
  } catch (error) {
    console.error(`Error formatting number for locale ${locale}:`, error);
    // Fallback to default locale
    const fallbackFormatter = new Intl.NumberFormat(DEFAULT_LOCALE, options);
    return fallbackFormatter.format(number);
  }
}

/**
 * Gets the appropriate regional color theme based on locale
 * @param {string} locale - The locale code
 * @param {object} colors - Color tokens
 * @returns {object} - Regional color theme
 */
function getRegionalColorTheme(locale, colors) {
  if (!locale || !colors?.regional) {
    return {};
  }
  
  // Map locale to region
  const localePrefix = locale.split('-')[0].toLowerCase();
  const country = locale.split('-')[1]?.toUpperCase();
  
  // Map locale to regional color theme
  if (country === 'US' || country === 'CA') {
    return { accent: colors.regional.northAmerica.accent };
  } else if (
    country === 'GB' || country === 'FR' || country === 'DE' || 
    country === 'IT' || country === 'ES' || country === 'NL'
  ) {
    return { accent: colors.regional.europe.accent };
  } else if (
    localePrefix === 'zh' || localePrefix === 'ja' || 
    localePrefix === 'ko' || localePrefix === 'th'
  ) {
    return { accent: colors.regional.asia.accent };
  } else if (
    localePrefix === 'ar' || country === 'SA' || 
    country === 'AE' || country === 'QA'
  ) {
    return { accent: colors.regional.middleEast.accent };
  } else if (
    country === 'ZA' || country === 'NG' || 
    country === 'KE' || country === 'GH'
  ) {
    return { accent: colors.regional.africa.accent };
  } else if (
    country === 'BR' || country === 'MX' || 
    country === 'AR' || country === 'CO'
  ) {
    return { accent: colors.regional.southAmerica.accent };
  }
  
  // Default
  return {};
}

module.exports = {
  isRtlLocale,
  getTextDirection,
  getLocalizedFontFamily,
  formatLocalizedDate,
  formatLocalizedCurrency,
  formatLocalizedNumber,
  getRegionalColorTheme,
  DEFAULT_LOCALE,
  DEFAULT_DIRECTION,
  RTL_LOCALES
};
