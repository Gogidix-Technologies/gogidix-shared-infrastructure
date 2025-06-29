/**
 * ProductList Component
 * A flexible and customizable product listing component for displaying 
 * collections of products in various layouts (grid, list, carousel).
 * Supports filtering, sorting, pagination, and social commerce features.
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const ProductCard = require('../ProductCard');

/**
 * ProductList component generator
 * @param {Object} props - Component properties
 * @param {Array} props.products - Array of product objects to display
 * @param {string} [props.layout="grid"] - Display layout: "grid", "list", or "carousel"
 * @param {number} [props.columns=3] - Number of columns for grid layout
 * @param {Object} [props.filters={}] - Active filters
 * @param {Object} [props.sorting={}] - Sorting preferences
 * @param {Object} [props.pagination={}] - Pagination options
 * @param {boolean} [props.showFilters=false] - Whether to show filter panel
 * @param {boolean} [props.showSorting=true] - Whether to show sorting options
 * @param {boolean} [props.showSocialMetrics=true] - Whether to show social metrics on cards
 * @param {Function} [props.onProductClick] - Click handler for product cards
 * @param {Function} [props.onFilterChange] - Filter change handler
 * @param {Function} [props.onSortChange] - Sort change handler
 * @param {Function} [props.onPageChange] - Page change handler
 * @param {string} [props.emptyStateMessage] - Message to show when no products are available
 * @param {string} [props.locale=localization.DEFAULT_LOCALE] - Locale for the component
 * @param {string} [props.className=""] - Additional CSS class names
 * @returns {string} HTML for the product list component
 */
function ProductList({
  products = [],
  layout = 'grid', // 'grid', 'list', 'carousel'
  columns = 3,
  filters = {},
  sorting = {
    field: 'relevance',
    direction: 'desc'
  },
  pagination = {
    page: 1,
    perPage: 12,
    total: 0
  },
  showFilters = false,
  showSorting = true,
  showSocialMetrics = true,
  onProductClick,
  onFilterChange,
  onSortChange,
  onPageChange,
  emptyStateMessage,
  locale = localization.DEFAULT_LOCALE,
  className = '',
  ...otherProps
}) {
  // Set up translations based on locale
  const translations = getTranslations(locale);

  // Handle RTL layout
  const dir = localization.isRTL(locale) ? 'rtl' : 'ltr';

  // Create unique ID
  const productListId = `product-list-${Math.random().toString(36).substring(2, 10)}`;
  
  // Determine gaps between products based on layout type
  const gaps = {
    grid: {
      column: tokens.spacing.md,
      row: tokens.spacing.lg
    },
    list: {
      column: 0,
      row: tokens.spacing.md
    },
    carousel: {
      column: tokens.spacing.md,
      row: 0
    }
  };
  // Create filters panel
  let filtersHtml = '';
  if (showFilters) {
    // Simple category, price, rating filters
    filtersHtml = `
      <div class="product-filters" style="
        padding: ${tokens.spacing.md};
        border: 1px solid ${tokens.colors.neutral.gray[200]};
        border-radius: ${tokens.borders.borderRadius.md};
        margin-bottom: ${tokens.spacing.md};
        background-color: ${tokens.colors.neutral.gray[50]};
      ">
        <h3 style="
          font-size: ${tokens.typography.textStyles.heading4.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
          margin-top: 0;
          margin-bottom: ${tokens.spacing.md};
        ">
          ${translations.filters}
        </h3>
        
        ${Object.keys(filters).map(filterKey => {
          const filter = filters[filterKey];
          
          if (filter.type === 'checkbox' && Array.isArray(filter.options)) {
            return `
              <div class="filter-group" style="margin-bottom: ${tokens.spacing.md};">
                <h4 style="
                  font-size: ${tokens.typography.textStyles.bodyLarge.fontSize};
                  font-weight: ${tokens.typography.fontWeights.medium};
                  margin-top: 0;
                  margin-bottom: ${tokens.spacing.sm};
                ">
                  ${filter.label || filterKey}
                </h4>
                
                <div style="
                  display: flex;
                  flex-direction: column;
                  gap: ${tokens.spacing.xs};
                ">
                  ${filter.options.map(option => `
                    <label style="
                      display: flex;
                      align-items: center;
                      cursor: pointer;
                    ">
                      <input 
                        type="checkbox" 
                        name="filter-${filterKey}" 
                        value="${option.value}" 
                        ${option.selected ? 'checked' : ''}
                        onclick="handleFilterChange('${productListId}', '${filterKey}', '${option.value}', this.checked)"
                        style="
                          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
                        "
                      />
                      <span>${option.label}</span>
                      ${option.count !== undefined ? `
                        <span style="
                          color: ${tokens.colors.neutral.gray[500]};
                          margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xs};
                        ">
                          (${localization.formatLocalizedNumber(option.count, locale)})
                        </span>
                      ` : ''}
                    </label>
                  `).join('')}
                </div>
              </div>
            `;
          }
          
          if (filter.type === 'price' || filter.type === 'range') {
            return `
              <div class="filter-group" style="margin-bottom: ${tokens.spacing.md};">
                <h4 style="
                  font-size: ${tokens.typography.textStyles.bodyLarge.fontSize};
                  font-weight: ${tokens.typography.fontWeights.medium};
                  margin-top: 0;
                  margin-bottom: ${tokens.spacing.sm};
                ">
                  ${filter.label || filterKey}
                </h4>
                
                <div style="
                  display: flex;
                  gap: ${tokens.spacing.sm};
                  align-items: center;
                ">
                  <input 
                    type="number" 
                    value="${filter.min || ''}" 
                    min="${filter.minLimit || 0}" 
                    max="${filter.max || filter.maxLimit || 1000}"
                    placeholder="${filter.minPlaceholder || translations.minPrice}"
                    onchange="handleRangeFilterChange('${productListId}', '${filterKey}', 'min', this.value)"
                    style="
                      width: 50%;
                      padding: ${tokens.spacing.xs} ${tokens.spacing.sm};
                      border: ${tokens.borders.presets.input.normal};
                      border-radius: ${tokens.borders.borderRadius.md};
                      font-family: inherit;
                    "
                  />
                  <span>-</span>
                  <input 
                    type="number" 
                    value="${filter.max || ''}" 
                    min="${filter.min || filter.minLimit || 0}" 
                    max="${filter.maxLimit || 10000}"
                    placeholder="${filter.maxPlaceholder || translations.maxPrice}"
                    onchange="handleRangeFilterChange('${productListId}', '${filterKey}', 'max', this.value)"
                    style="
                      width: 50%;
                      padding: ${tokens.spacing.xs} ${tokens.spacing.sm};
                      border: ${tokens.borders.presets.input.normal};
                      border-radius: ${tokens.borders.borderRadius.md};
                      font-family: inherit;
                    "
                  />
                </div>
              </div>
            `;
          }
          
          if (filter.type === 'rating') {
            return `
              <div class="filter-group" style="margin-bottom: ${tokens.spacing.md};">
                <h4 style="
                  font-size: ${tokens.typography.textStyles.bodyLarge.fontSize};
                  font-weight: ${tokens.typography.fontWeights.medium};
                  margin-top: 0;
                  margin-bottom: ${tokens.spacing.sm};
                ">
                  ${filter.label || translations.customerRating}
                </h4>
                
                <div style="
                  display: flex;
                  flex-direction: column;
                  gap: ${tokens.spacing.xs};
                ">
                  ${[4, 3, 2, 1].map(rating => `
                    <label style="
                      display: flex;
                      align-items: center;
                      cursor: pointer;
                    ">
                      <input 
                        type="checkbox" 
                        name="filter-rating" 
                        value="${rating}" 
                        ${(filter.selected || []).includes(rating) ? 'checked' : ''}
                        onclick="handleFilterChange('${productListId}', 'rating', '${rating}', this.checked)"
                        style="
                          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
                        "
                      />
                      <div style="display: flex; align-items: center;">
                        ${Array(5).fill(0).map((_, i) => `
                          <i 
                            class="icon-star${i < rating ? '-filled' : ''}" 
                            style="
                              color: ${i < rating ? tokens.colors.brand.accent[500] : tokens.colors.neutral.gray[300]};
                              font-size: 1em;
                            "
                          ></i>
                        `).join('')}
                        <span style="margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xs};">
                          ${translations.andAbove}
                        </span>
                      </div>
                    </label>
                  `).join('')}
                </div>
              </div>
            `;
          }
          
          return '';
        }).join('')}
        
        <div style="
          display: flex;
          justify-content: space-between;
          margin-top: ${tokens.spacing.md};
        ">
          <button
            class="clear-filters-btn"
            onclick="clearFilters('${productListId}')"
            style="
              background: none;
              border: none;
              color: ${tokens.colors.brand.primary[600]};
              cursor: pointer;
              font-family: inherit;
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              text-decoration: underline;
              
              &:hover {
                color: ${tokens.colors.brand.primary[700]};
              }
            "
          >
            ${translations.clearFilters}
          </button>
          
          <button
            class="apply-filters-btn"
            onclick="applyFilters('${productListId}')"
            style="
              background-color: ${tokens.colors.brand.primary[600]};
              border: none;
              color: white;
              cursor: pointer;
              font-family: inherit;
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              font-weight: ${tokens.typography.fontWeights.medium};
              padding: ${tokens.spacing.xs} ${tokens.spacing.md};
              border-radius: ${tokens.borders.borderRadius.md};
              
              &:hover {
                background-color: ${tokens.colors.brand.primary[700]};
              }
            "
          >
            ${translations.applyFilters}
          </button>
        </div>
      </div>
    `;
  }
  // Create product cards
  let productsHtml = '';
  let layoutStyle = '';
  
  if (products.length === 0) {
    // Empty state
    productsHtml = `
      <div class="product-list-empty" style="
        padding: ${tokens.spacing.xl} ${tokens.spacing.md};
        text-align: center;
        background-color: ${tokens.colors.neutral.gray[50]};
        border-radius: ${tokens.borders.borderRadius.md};
      ">
        <i class="icon-search" style="
          font-size: 3rem;
          color: ${tokens.colors.neutral.gray[400]};
          margin-bottom: ${tokens.spacing.md};
          display: block;
        "></i>
        <h2 style="
          font-size: ${tokens.typography.textStyles.heading3.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
          margin-top: 0;
          margin-bottom: ${tokens.spacing.sm};
        ">
          ${emptyStateMessage || translations.noProductsFound}
        </h2>
        <p style="
          color: ${tokens.colors.neutral.gray[600]};
          margin-bottom: ${tokens.spacing.lg};
        ">
          ${translations.tryAdjustingFilters}
        </p>
      </div>
    `;
  } else {
    switch (layout) {
      case 'list':
        layoutStyle = `
          display: flex;
          flex-direction: column;
          gap: ${gaps.list.row};
        `;
        break;
        
      case 'carousel':
        layoutStyle = `
          display: flex;
          flex-wrap: nowrap;
          overflow-x: auto;
          scroll-snap-type: x mandatory;
          gap: ${gaps.carousel.column};
          scrollbar-width: thin;
          scrollbar-color: ${tokens.colors.neutral.gray[400]} ${tokens.colors.neutral.gray[200]};
          padding-bottom: ${tokens.spacing.md}; /* for scrollbar space */
          
          &::-webkit-scrollbar {
            height: 8px;
          }
          
          &::-webkit-scrollbar-track {
            background: ${tokens.colors.neutral.gray[200]};
            border-radius: ${tokens.borders.borderRadius.full};
          }
          
          &::-webkit-scrollbar-thumb {
            background-color: ${tokens.colors.neutral.gray[400]};
            border-radius: ${tokens.borders.borderRadius.full};
          }
        `;
        break;
        
      case 'grid':
      default:
        // Design mobile-first grid that adjusts columns based on screen size
        layoutStyle = `
          display: grid;
          grid-template-columns: repeat(1, 1fr);
          gap: ${gaps.grid.row} ${gaps.grid.column};
          
          @media (min-width: 576px) {
            grid-template-columns: repeat(2, 1fr);
          }
          
          @media (min-width: 768px) {
            grid-template-columns: repeat(${Math.min(columns, 3)}, 1fr);
          }
          
          @media (min-width: 992px) {
            grid-template-columns: repeat(${Math.min(columns, 4)}, 1fr);
          }
          
          @media (min-width: 1200px) {
            grid-template-columns: repeat(${columns}, 1fr);
          }
        `;
    }

    // Create product cards based on layout
    productsHtml = `
      <div class="products-container" style="${layoutStyle}">
        ${products.map(product => {
          const cardProps = {
            ...product,
            layout: layout === 'list' ? 'horizontal' : 'vertical',
            onClick: onProductClick ? `productClicked('${productListId}', '${product.id}')` : '',
            showSocialMetrics,
            locale
          };
          
          // Generate card HTML
          return ProductCard(cardProps);
        }).join('')}
      </div>
    `;
  }
  
  // Create pagination controls
  let paginationHtml = '';
  
  if (pagination && pagination.total > pagination.perPage) {
    const totalPages = Math.ceil(pagination.total / pagination.perPage);
    const currentPage = pagination.page;
    
    // Create numeric page buttons with previous/next controls
    const createPageButton = (pageNum, label, isActive = false, isDisabled = false) => {
      return `
        <button 
          class="page-btn ${isActive ? 'active' : ''}"
          onclick="${!isDisabled ? `changePage('${productListId}', ${pageNum})` : ''}"
          ${isDisabled ? 'disabled' : ''}
          style="
            min-width: 36px;
            height: 36px;
            border: 1px solid ${isActive ? tokens.colors.brand.primary[500] : tokens.colors.neutral.gray[300]};
            background-color: ${isActive ? tokens.colors.brand.primary[500] : 'white'};
            color: ${isActive ? 'white' : tokens.colors.neutral.gray[900]};
            cursor: ${isDisabled ? 'not-allowed' : 'pointer'};
            border-radius: ${tokens.borders.borderRadius.md};
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: inherit;
            opacity: ${isDisabled ? '0.5' : '1'};
          "
        >
          ${label}
        </button>
      `;
    };
    
    // Determine which page buttons to show
    const maxButtons = 5;
    let pageButtons = [];
    
    // Always show first and last page
    if (totalPages <= maxButtons) {
      // Show all pages
      for (let i = 1; i <= totalPages; i++) {
        pageButtons.push(createPageButton(i, i.toString(), i === currentPage));
      }
    } else {
      // Show limited pages with ellipsis
      pageButtons.push(createPageButton(1, '1', currentPage === 1));
      
      if (currentPage > 3) {
        pageButtons.push(`<span class="ellipsis" style="margin: 0 ${tokens.spacing.xs};">...</span>`);
      }
      
      for (let i = Math.max(2, currentPage - 1); i <= Math.min(totalPages - 1, currentPage + 1); i++) {
        pageButtons.push(createPageButton(i, i.toString(), i === currentPage));
      }
      
      if (currentPage < totalPages - 2) {
        pageButtons.push(`<span class="ellipsis" style="margin: 0 ${tokens.spacing.xs};">...</span>`);
      }
      
      pageButtons.push(createPageButton(totalPages, totalPages.toString(), currentPage === totalPages));
    }
    
    // Add previous/next buttons
    const prevButton = createPageButton(currentPage - 1, `<i class="icon-chevron-${dir === 'rtl' ? 'right' : 'left'}"></i>`, false, currentPage === 1);
    const nextButton = createPageButton(currentPage + 1, `<i class="icon-chevron-${dir === 'rtl' ? 'left' : 'right'}"></i>`, false, currentPage === totalPages);
    
    pageButtons = [prevButton, ...pageButtons, nextButton];

    paginationHtml = `
      <div class="pagination" style="
        display: flex;
        justify-content: center;
        align-items: center;
        margin-top: ${tokens.spacing.lg};
        gap: ${tokens.spacing.xs};
        flex-wrap: wrap;
      ">
        ${pageButtons.join('')}
      </div>
    `;
  }
  // Build complete product list HTML
  return `
    <div 
      id="${productListId}" 
      class="product-list ${className}" 
      dir="${dir}"
      data-layout="${layout}"
      data-locale="${locale}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div style="
        display: ${showFilters ? 'flex' : 'block'};
        flex-wrap: wrap;
        gap: ${tokens.spacing.lg};
      ">
        ${showFilters ? `
          <div class="product-list-sidebar" style="
            flex: 0 0 100%;
            
            @media (min-width: 768px) {
              flex: 0 0 250px;
            }
          ">
            ${filtersHtml}
          </div>
        ` : ''}
        
        <div class="product-list-main" style="
          flex: 1;
        ">
          ${sortingHtml}
          ${productsHtml}
          ${paginationHtml}
        </div>
      </div>
      
      <script>
        // Event handlers
        function productClicked(listId, productId) {
          ${onProductClick ? `
            // Custom handler would be injected here
            console.log('Product clicked:', listId, productId);
          ` : ''}
        }
        
        function handleFilterChange(listId, filterName, value, isChecked) {
          ${onFilterChange ? `
            // Custom handler would be injected here
            console.log('Filter changed:', listId, filterName, value, isChecked);
          ` : ''}
        }
        
        function handleRangeFilterChange(listId, filterName, bound, value) {
          ${onFilterChange ? `
            // Custom handler would be injected here
            console.log('Range filter changed:', listId, filterName, bound, value);
          ` : ''}
        }
        
        function clearFilters(listId) {
          ${onFilterChange ? `
            // Custom handler would be injected here
            console.log('Filters cleared:', listId);
          ` : ''}
        }
        
        function applyFilters(listId) {
          ${onFilterChange ? `
            // Custom handler would be injected here
            console.log('Filters applied:', listId);
          ` : ''}
        }
        
        function handleSortChange(listId, sortValue) {
          ${onSortChange ? `
            // Custom handler would be injected here
            console.log('Sort changed:', listId, sortValue);
          ` : ''}
        }
        
        function toggleLayout(listId, layoutType) {
          // Updates the layout attribute in the DOM, actual data change would be handled by the application
          const productList = document.getElementById(listId);
          if (productList) {
            productList.dataset.layout = layoutType;
            
            ${onLayoutChange ? `
              // Custom handler would be injected here
              console.log('Layout toggled:', listId, layoutType);
            ` : ''}
          }
        }
        
        function changePage(listId, page) {
          ${onPageChange ? `
            // Custom handler would be injected here
            console.log('Page changed:', listId, page);
          ` : ''}
        }
      </script>
    </div>
  `;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      filters: 'Filters',
      sortBy: 'Sort by',
      sortRelevance: 'Relevance',
      sortPriceAsc: 'Price: Low to High',
      sortPriceDesc: 'Price: High to Low',
      sortRatingDesc: 'Highest Rated',
      sortNewest: 'Newest First',
      sortPopular: 'Most Popular',
      clearFilters: 'Clear All',
      applyFilters: 'Apply',
      noProductsFound: 'No products found',
      tryAdjustingFilters: 'Try adjusting your filters or search criteria',
      showingProducts: 'Showing {start}-{end} of {total} products',
      minPrice: 'Min',
      maxPrice: 'Max',
      customerRating: 'Customer Rating',
      andAbove: '& above'
    },
    ar: {
      filters: 'الفلاتر',
      sortBy: 'ترتيب حسب',
      sortRelevance: 'صلة',
      sortPriceAsc: 'السعر: من الأقل إلى الأعلى',
      sortPriceDesc: 'السعر: من الأعلى إلى الأقل',
      sortRatingDesc: 'الأعلى تقييماً',
      sortNewest: 'الأحدث أولاً',
      sortPopular: 'الأكثر شعبية',
      clearFilters: 'مسح الكل',
      applyFilters: 'تطبيق',
      noProductsFound: 'لم يتم العثور على منتجات',
      tryAdjustingFilters: 'حاول تعديل الفلاتر أو معايير البحث',
      showingProducts: 'عرض {start}-{end} من {total} منتج',
      minPrice: 'الحد الأدنى',
      maxPrice: 'الحد الأقصى',
      customerRating: 'تقييم العملاء',
      andAbove: 'وأعلى'
    },
    fr: {
      filters: 'Filtres',
      sortBy: 'Trier par',
      sortRelevance: 'Pertinence',
      sortPriceAsc: 'Prix: croissant',
      sortPriceDesc: 'Prix: décroissant',
      sortRatingDesc: 'Mieux notés',
      sortNewest: 'Plus récents',
      sortPopular: 'Plus populaires',
      clearFilters: 'Effacer tout',
      applyFilters: 'Appliquer',
      noProductsFound: 'Aucun produit trouvé',
      tryAdjustingFilters: 'Essayez d\'ajuster vos filtres ou critères de recherche',
      showingProducts: 'Affichage de {start}-{end} sur {total} produits',
      minPrice: 'Min',
      maxPrice: 'Max',
      customerRating: 'Avis clients',
      andAbove: 'et plus'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

module.exports = ProductList;
