/**
 * RegionalSettings Component
 * Provides localization settings UI for customer-facing interfaces.
 * Primary component for Team Alpha to implement localization features.
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Button = require('../Button');

/**
 * RegionalSettings component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the regional settings component
 */
function RegionalSettings({
  currentLocale = localization.DEFAULT_LOCALE,
  currentCurrency = 'USD',
  availableLocales = ['en-US', 'es-ES', 'fr-FR', 'ar-SA', 'zh-CN', 'ja-JP'],
  availableCurrencies = ['USD', 'EUR', 'GBP', 'JPY', 'CNY', 'AED'],
  onLocaleChange,
  onCurrencyChange,
  className = '',
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(currentLocale);
  
  // Get regional color theme
  const regionalTheme = localization.getRegionalColorTheme(currentLocale, tokens.colors);
  
  // Format the current locale for display
  const currentLocaleName = new Intl.DisplayNames([currentLocale], { type: 'language' })
    .of(currentLocale.split('-')[0]);
  
  // Generate locale options HTML
  const localeOptions = availableLocales.map(locale => {
    const localeName = new Intl.DisplayNames([currentLocale], { type: 'language' })
      .of(locale.split('-')[0]);
    const selected = locale === currentLocale ? 'selected' : '';
    
    return `<option value="${locale}" ${selected}>${localeName} (${locale})</option>`;
  }).join('');
  
  // Generate currency options HTML
  const currencyOptions = availableCurrencies.map(currency => {
    const selected = currency === currentCurrency ? 'selected' : '';
    return `<option value="${currency}" ${selected}>${currency}</option>`;
  }).join('');
  
  // Create apply button using Button component
  const applyButton = Button({
    children: 'Apply Settings',
    variant: 'primary',
    size: 'md',
    locale: currentLocale,
    onClick: 'applyRegionalSettings()',
  });
  
  // Demo exchange rate display
  const demoAmount = 100;
  const formattedCurrency = localization.formatLocalizedCurrency(
    demoAmount, 
    currentCurrency, 
    currentLocale
  );
  
  // Demo date display
  const today = new Date();
  const formattedDate = localization.formatLocalizedDate(
    today, 
    currentLocale
  );
  
  // Build component with appropriate direction attribute for RTL support
  return `
    <div 
      class="regional-settings ${className}" 
      dir="${dir}"
      style="
        border: 1px solid ${tokens.colors.neutral.gray[300]};
        border-radius: 8px;
        padding: ${tokens.spacing.md};
        background-color: ${tokens.colors.neutral.gray[50]};
        ${regionalTheme.accent ? `border-top: 3px solid ${regionalTheme.accent};` : ''}
      "
    >
      <h3 style="
        font-size: ${tokens.typography.textStyles.h5.fontSize};
        font-weight: ${tokens.typography.textStyles.h5.fontWeight};
        margin-bottom: ${tokens.spacing.md};
        color: ${tokens.colors.neutral.gray[900]};
      ">
        ${dir === 'rtl' ? 'إعدادات المنطقة' : 'Regional Settings'}
      </h3>
      
      <div class="settings-grid" style="
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: ${tokens.spacing.md};
        margin-bottom: ${tokens.spacing.md};
      ">
        <div class="setting-group">
          <label for="locale-selector" style="
            display: block;
            margin-bottom: ${tokens.spacing.xs};
            font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
            font-weight: ${tokens.typography.textStyles.bodyMedium.fontWeight};
            color: ${tokens.colors.neutral.gray[700]};
          ">
            ${dir === 'rtl' ? 'اللغة والمنطقة' : 'Language & Region'}
          </label>
          <select 
            id="locale-selector" 
            class="locale-selector"
            onchange="${onLocaleChange}"
            style="
              width: 100%;
              padding: ${tokens.spacing.sm};
              border: 1px solid ${tokens.colors.neutral.gray[300]};
              border-radius: 4px;
              background-color: ${tokens.colors.neutral.white};
              font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
            "
          >
            ${localeOptions}
          </select>
          
          <div class="helper-text" style="
            font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            color: ${tokens.colors.neutral.gray[500]};
            margin-top: ${tokens.spacing.xs};
          ">
            ${dir === 'rtl' 
              ? 'يؤثر على اللغة وتنسيق التاريخ والوقت' 
              : 'Affects language, date and time formats'}
          </div>
        </div>
        
        <div class="setting-group">
          <label for="currency-selector" style="
            display: block;
            margin-bottom: ${tokens.spacing.xs};
            font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
            font-weight: ${tokens.typography.textStyles.bodyMedium.fontWeight};
            color: ${tokens.colors.neutral.gray[700]};
          ">
            ${dir === 'rtl' ? 'العملة' : 'Currency'}
          </label>
          <select 
            id="currency-selector" 
            class="currency-selector"
            onchange="${onCurrencyChange}"
            style="
              width: 100%;
              padding: ${tokens.spacing.sm};
              border: 1px solid ${tokens.colors.neutral.gray[300]};
              border-radius: 4px;
              background-color: ${tokens.colors.neutral.white};
              font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
            "
          >
            ${currencyOptions}
          </select>
          
          <div class="helper-text" style="
            font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            color: ${tokens.colors.neutral.gray[500]};
            margin-top: ${tokens.spacing.xs};
          ">
            ${dir === 'rtl' 
              ? 'يؤثر على كيفية عرض الأسعار' 
              : 'Affects how prices are displayed'}
          </div>
        </div>
      </div>
      
      <div class="preview-section" style="
        background-color: ${tokens.colors.neutral.white};
        border: 1px solid ${tokens.colors.neutral.gray[200]};
        border-radius: 4px;
        padding: ${tokens.spacing.md};
        margin-bottom: ${tokens.spacing.md};
      ">
        <h4 style="
          font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
          margin-bottom: ${tokens.spacing.sm};
          color: ${tokens.colors.neutral.gray[700]};
        ">
          ${dir === 'rtl' ? 'معاينة' : 'Preview'}
        </h4>
        
        <div class="preview-item" style="
          display: flex;
          justify-content: space-between;
          margin-bottom: ${tokens.spacing.xs};
        ">
          <span style="color: ${tokens.colors.neutral.gray[600]};">
            ${dir === 'rtl' ? 'المبلغ:' : 'Amount:'}
          </span>
          <span style="font-weight: ${tokens.typography.fontWeights.medium};">
            ${formattedCurrency}
          </span>
        </div>
        
        <div class="preview-item" style="
          display: flex;
          justify-content: space-between;
        ">
          <span style="color: ${tokens.colors.neutral.gray[600]};">
            ${dir === 'rtl' ? 'التاريخ:' : 'Date:'}
          </span>
          <span style="font-weight: ${tokens.typography.fontWeights.medium};">
            ${formattedDate}
          </span>
        </div>
      </div>
      
      <div class="actions" style="
        display: flex;
        justify-content: ${dir === 'rtl' ? 'flex-start' : 'flex-end'};
      ">
        ${applyButton}
      </div>
    </div>
  `;
}

module.exports = RegionalSettings;
