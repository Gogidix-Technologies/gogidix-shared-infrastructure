/**
 * ReviewsSection Template
 * A complete product reviews section template with summary statistics, filtering, and social features
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Rating = require('../../components/Rating');
const Review = require('../../components/Review');

/**
 * ReviewsSection template generator
 * @param {Object} props - Template properties
 * @param {Object} props.summaryData - Review summary statistics
 * @param {Array} props.reviews - Array of review objects to display
 * @param {number} [props.totalReviews] - Total number of reviews (for pagination)
 * @param {boolean} [props.allowSorting=true] - Whether to show sorting options
 * @param {boolean} [props.allowFiltering=true] - Whether to show filtering options
 * @param {string} [props.defaultSort='newest'] - Default sort order: newest, oldest, highestRated, lowestRated, mostHelpful
 * @param {boolean} [props.showWriteReview=true] - Whether to show the write review button
 * @param {Function} [props.onReviewSubmit] - Handler for review submission
 * @param {Function} [props.onSortChange] - Handler for sort change
 * @param {Function} [props.onFilterChange] - Handler for filter change
 * @param {string} [props.locale=localization.DEFAULT_LOCALE] - Locale for the component
 * @param {string} [props.className=""] - Additional CSS class names
 * @returns {string} HTML for the reviews section template
 */
function ReviewsSection({
  summaryData = {
    average: 0,
    total: 0,
    distribution: [0, 0, 0, 0, 0] // 5 stars to 1 star
  },
  reviews = [],
  totalReviews,
  allowSorting = true,
  allowFiltering = true,
  defaultSort = 'newest',
  showWriteReview = true,
  onReviewSubmit,
  onSortChange,
  onFilterChange,
  locale = localization.DEFAULT_LOCALE,
  className = '',
  ...otherProps
}) {
  // Set up translations based on locale
  const translations = getTranslations(locale);
  
  // Handle RTL layout
  const dir = localization.isRTL(locale) ? 'rtl' : 'ltr';
  
  // Create unique ID
  const sectionId = `reviews-section-${Math.random().toString(36).substring(2, 10)}`;
  
  // Calculate total reviews if not provided
  const totalCount = totalReviews || summaryData.total;
  
  // Create review summary
  const totalDistribution = summaryData.distribution.reduce((a, b) => a + b, 0);
  const reviewSummary = `
    <div class="reviews-summary" style="
      margin-bottom: ${tokens.spacing.xl};
      padding: ${tokens.spacing.lg};
      background-color: ${tokens.colors.neutral.gray[50]};
      border-radius: ${tokens.borders.borderRadius.lg};
      box-shadow: ${tokens.shadows.sm};
    ">
      <div style="
        display: flex;
        flex-wrap: wrap;
        gap: ${tokens.spacing.lg};
        justify-content: space-between;
      ">
        <div class="average-score" style="
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          padding-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.lg};
          border-${dir === 'rtl' ? 'left' : 'right'}: 1px solid ${tokens.colors.neutral.gray[200]};
          flex: 0 0 160px;
        ">
          <div style="
            font-size: 3rem;
            font-weight: ${tokens.typography.fontWeights.bold};
            color: ${tokens.colors.neutral.gray[900]};
            line-height: 1;
            margin-bottom: ${tokens.spacing.xs};
          ">
            ${localization.formatLocalizedNumber(summaryData.average, locale, { minimumFractionDigits: 1, maximumFractionDigits: 1 })}
          </div>
          
          <div style="margin-bottom: ${tokens.spacing.sm};">
            ${Rating({
              value: summaryData.average,
              readOnly: true,
              size: 'lg',
              locale
            })}
          </div>
          
          <div style="
            font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            color: ${tokens.colors.neutral.gray[600]};
          ">
            ${localization.formatLocalizedNumber(totalCount, locale)} ${totalCount === 1 ? translations.review : translations.reviews}
          </div>
        </div>
        
        <div class="rating-distribution" style="
          flex: 1;
          min-width: 280px;
        ">
          ${[5, 4, 3, 2, 1].map((star, idx) => {
            const count = summaryData.distribution[idx] || 0;
            const percentage = totalDistribution > 0 ? Math.round((count / totalDistribution) * 100) : 0;
            
            return `
              <div class="rating-bar" style="
                display: flex;
                align-items: center;
                margin-bottom: ${tokens.spacing.sm};
                gap: ${tokens.spacing.md};
              ">
                <div style="
                  min-width: 40px;
                  display: flex;
                  align-items: center;
                ">
                  <span style="
                    font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
                    font-weight: ${tokens.typography.fontWeights.medium};
                    margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.xs};
                  ">
                    ${star}
                  </span>
                  <i class="icon-star-filled" style="
                    color: ${tokens.colors.brand.accent[500]};
                    font-size: 0.8em;
                  "></i>
                </div>
                
                <div class="progress-bar" style="
                  flex: 1;
                  height: 12px;
                  background-color: ${tokens.colors.neutral.gray[200]};
                  border-radius: ${tokens.borders.borderRadius.full};
                  overflow: hidden;
                ">
                  <div style="
                    height: 100%;
                    width: ${percentage}%;
                    background-color: ${tokens.colors.brand.accent[500]};
                  "></div>
                </div>
                
                <div style="
                  min-width: 70px;
                  text-align: ${dir === 'rtl' ? 'right' : 'left'};
                  font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
                  color: ${tokens.colors.neutral.gray[600]};
                ">
                  ${localization.formatLocalizedNumber(count, locale)} (${percentage}%)
                </div>
              </div>
            `;
          }).join('')}
        </div>
      </div>
      
      ${showWriteReview ? `
        <div style="
          margin-top: ${tokens.spacing.lg};
          text-align: center;
          padding-top: ${tokens.spacing.md};
          border-top: 1px solid ${tokens.colors.neutral.gray[200]};
        ">
          <button
            onclick="showReviewForm('${sectionId}')"
            style="
              background-color: ${tokens.colors.brand.primary[600]};
              color: white;
              border: none;
              border-radius: ${tokens.borders.borderRadius.md};
              padding: ${tokens.spacing.sm} ${tokens.spacing.lg};
              font-family: inherit;
              font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
              font-weight: ${tokens.typography.fontWeights.medium};
              cursor: pointer;
              transition: background-color 0.2s;
              
              &:hover {
                background-color: ${tokens.colors.brand.primary[700]};
              }
            "
          >
            ${translations.writeReview}
          </button>
        </div>
      ` : ''}
    </div>
  `;
  // Create sorting controls
  let sortingHtml = '';
  if (allowSorting) {
    const sortOptions = [
      { value: 'newest', label: translations.sortNewest },
      { value: 'oldest', label: translations.sortOldest },
      { value: 'highestRated', label: translations.sortHighestRated },
      { value: 'lowestRated', label: translations.sortLowestRated },
      { value: 'mostHelpful', label: translations.sortMostHelpful },
    ];
    
    sortingHtml = `
      <div class="reviews-sorting" style="
        display: flex;
        align-items: center;
        margin-bottom: ${tokens.spacing.md};
      ">
        <label for="${sectionId}-sort" style="
          font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.sm};
        ">
          ${translations.sortBy}:
        </label>
        <select
          id="${sectionId}-sort"
          onchange="handleSortChange('${sectionId}', this.value)"
          style="
            padding: ${tokens.spacing.xs} ${tokens.spacing.md};
            border: ${tokens.borders.presets.input.normal};
            border-radius: ${tokens.borders.borderRadius.md};
            font-family: inherit;
            appearance: none;
            background-image: url('data:image/svg+xml;utf8,<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"24\" height=\"24\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" stroke-linecap=\"round\" stroke-linejoin=\"round\"><polyline points=\"6 9 12 15 18 9\"></polyline></svg>');
            background-repeat: no-repeat;
            background-position: right ${tokens.spacing.sm} center;
            background-size: 16px;
            padding-right: ${tokens.spacing.xl};
          "
        >
          ${sortOptions.map(option => `
            <option 
              value="${option.value}" 
              ${option.value === defaultSort ? 'selected' : ''}
            >
              ${option.label}
            </option>
          `).join('')}
        </select>
      </div>
    `;
  }
  
  // Create filtering controls
  let filteringHtml = '';
  if (allowFiltering) {
    filteringHtml = `
      <div class="reviews-filtering" style="
        display: flex;
        flex-wrap: wrap;
        gap: ${tokens.spacing.sm};
        margin-bottom: ${tokens.spacing.lg};
      ">
        <button
          class="filter-btn active"
          onclick="handleFilterChange('${sectionId}', 'all')"
          style="
            border: none;
            background: ${tokens.colors.neutral.gray[200]};
            color: ${tokens.colors.neutral.gray[900]};
            padding: ${tokens.spacing.xs} ${tokens.spacing.md};
            border-radius: ${tokens.borders.borderRadius.full};
            font-family: inherit;
            font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            cursor: pointer;
            transition: background-color 0.2s;
            
            &:hover {
              background: ${tokens.colors.neutral.gray[300]};
            }
            
            &.active {
              background: ${tokens.colors.brand.primary[600]};
              color: white;
            }
          "
        >
          ${translations.allRatings}
        </button>
        
        ${[5, 4, 3, 2, 1].map(star => `
          <button
            class="filter-btn"
            onclick="handleFilterChange('${sectionId}', ${star})"
            style="
              border: none;
              background: ${tokens.colors.neutral.gray[200]};
              color: ${tokens.colors.neutral.gray[900]};
              padding: ${tokens.spacing.xs} ${tokens.spacing.md};
              border-radius: ${tokens.borders.borderRadius.full};
              font-family: inherit;
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              cursor: pointer;
              transition: background-color 0.2s;
              
              &:hover {
                background: ${tokens.colors.neutral.gray[300]};
              }
              
              &.active {
                background: ${tokens.colors.brand.primary[600]};
                color: white;
              }
            "
          >
            ${star} <i class="icon-star-filled" style="font-size: 0.8em;"></i>
          </button>
        `).join('')}
        
        <button
          class="filter-btn"
          onclick="handleFilterChange('${sectionId}', 'withMedia')"
          style="
            border: none;
            background: ${tokens.colors.neutral.gray[200]};
            color: ${tokens.colors.neutral.gray[900]};
            padding: ${tokens.spacing.xs} ${tokens.spacing.md};
            border-radius: ${tokens.borders.borderRadius.full};
            font-family: inherit;
            font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            cursor: pointer;
            transition: background-color 0.2s;
            
            &:hover {
              background: ${tokens.colors.neutral.gray[300]};
            }
            
            &.active {
              background: ${tokens.colors.brand.primary[600]};
              color: white;
            }
          "
        >
          ${translations.withImages}
        </button>
        
        <button
          class="filter-btn"
          onclick="handleFilterChange('${sectionId}', 'verified')"
          style="
            border: none;
            background: ${tokens.colors.neutral.gray[200]};
            color: ${tokens.colors.neutral.gray[900]};
            padding: ${tokens.spacing.xs} ${tokens.spacing.md};
            border-radius: ${tokens.borders.borderRadius.full};
            font-family: inherit;
            font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
            cursor: pointer;
            transition: background-color 0.2s;
            
            &:hover {
              background: ${tokens.colors.neutral.gray[300]};
            }
            
            &.active {
              background: ${tokens.colors.brand.primary[600]};
              color: white;
            }
          "
        >
          ${translations.verifiedPurchases}
        </button>
      </div>
    `;
  }
  
  // Create reviews list
  let reviewsHtml = '';
  if (reviews.length === 0) {
    reviewsHtml = `
      <div class="reviews-empty" style="
        padding: ${tokens.spacing.lg};
        text-align: center;
        background: ${tokens.colors.neutral.gray[100]};
        border-radius: ${tokens.borders.borderRadius.md};
        margin-bottom: ${tokens.spacing.lg};
      ">
        <i class="icon-review" style="
          font-size: 2rem;
          color: ${tokens.colors.neutral.gray[400]};
          margin-bottom: ${tokens.spacing.sm};
          display: block;
        "></i>
        <p style="
          margin: 0;
          color: ${tokens.colors.neutral.gray[600]};
        ">
          ${translations.noReviews}
        </p>
      </div>
    `;
  } else {
    reviewsHtml = `
      <div class="reviews-list" style="
        display: flex;
        flex-direction: column;
        gap: ${tokens.spacing.xl};
      ">
        ${reviews.map(review => {
          return Review({
            ...review,
            locale
          });
        }).join('')}
      </div>
    `;
  }
  
  // Create pagination if needed
  let paginationHtml = '';
  if (totalReviews > reviews.length) {
    paginationHtml = `
      <div class="reviews-pagination" style="
        display: flex;
        justify-content: center;
        margin-top: ${tokens.spacing.xl};
      ">
        <button
          onclick="loadMoreReviews('${sectionId}')"
          style="
            background: none;
            border: 1px solid ${tokens.colors.neutral.gray[300]};
            color: ${tokens.colors.neutral.gray[900]};
            padding: ${tokens.spacing.sm} ${tokens.spacing.lg};
            border-radius: ${tokens.borders.borderRadius.md};
            font-family: inherit;
            cursor: pointer;
            transition: all 0.2s;
            
            &:hover {
              border-color: ${tokens.colors.brand.primary[500]};
              color: ${tokens.colors.brand.primary[600]};
            }
          "
        >
          ${translations.loadMore}
        </button>
      </div>
    `;
  }
  
  // Build the complete reviews section
  return `
    <section 
      id="${sectionId}" 
      class="reviews-section ${className}" 
      dir="${dir}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${reviewSummary}
      
      <div class="reviews-controls" style="
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        align-items: center;
        margin-bottom: ${tokens.spacing.md};
      ">
        <h3 style="
          font-size: ${tokens.typography.textStyles.heading4.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
          margin: 0;
        ">
          ${totalCount > 0 ? translations.customerReviews : ''}
        </h3>
        
        <div>
          ${sortingHtml}
        </div>
      </div>
      
      ${filteringHtml}
      
      ${reviewsHtml}
      
      ${paginationHtml}
      
      <script>
        // Handle review form display
        function showReviewForm(sectionId) {
          // Implementation would be injected by the application
          console.log('Show review form for section:', sectionId);
        }
        
        // Event handlers
        function handleSortChange(sectionId, sortValue) {
          ${onSortChange ? `
            // Custom handler would be injected here
            console.log('Sort changed:', sectionId, sortValue);
          ` : ''}
        }
        
        function handleFilterChange(sectionId, filterValue) {
          ${onFilterChange ? `
            // Update active class on filter buttons
            const filterButtons = document.querySelectorAll(\`#\${sectionId} .filter-btn\`);
            filterButtons.forEach(btn => {
              btn.classList.remove('active');
            });
            
            // Find the clicked button and add active class
            let clickedButton;
            if (filterValue === 'all') {
              clickedButton = document.querySelector(\`#\${sectionId} .filter-btn:first-child\`);
            } else if (filterValue === 'withMedia') {
              clickedButton = [...filterButtons].find(btn => btn.textContent.trim().includes(translations.withImages));
            } else if (filterValue === 'verified') {
              clickedButton = [...filterButtons].find(btn => btn.textContent.trim().includes(translations.verifiedPurchases));
            } else {
              // This is a star rating filter
              clickedButton = [...filterButtons].find(btn => btn.textContent.trim().startsWith(filterValue));
            }
            
            if (clickedButton) {
              clickedButton.classList.add('active');
            }
            
            // Custom handler would be injected here
            console.log('Filter changed:', sectionId, filterValue);
          ` : ''}
        }
        
        function loadMoreReviews(sectionId) {
          // Custom handler would be injected here
          console.log('Load more reviews:', sectionId);
        }
      </script>
    </section>
  `;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      review: 'Review',
      reviews: 'Reviews',
      customerReviews: 'Customer Reviews',
      writeReview: 'Write a Review',
      noReviews: 'No reviews yet. Be the first to review this product!',
      loadMore: 'Load More Reviews',
      sortBy: 'Sort by',
      sortNewest: 'Newest',
      sortOldest: 'Oldest',
      sortHighestRated: 'Highest Rated',
      sortLowestRated: 'Lowest Rated',
      sortMostHelpful: 'Most Helpful',
      allRatings: 'All Ratings',
      verifiedPurchases: 'Verified Purchases',
      withImages: 'With Images'
    },
    ar: {
      review: 'تقييم',
      reviews: 'تقييمات',
      customerReviews: 'تقييمات العملاء',
      writeReview: 'كتابة تقييم',
      noReviews: 'لا توجد تقييمات بعد. كن أول من يقيم هذا المنتج!',
      loadMore: 'تحميل المزيد من التقييمات',
      sortBy: 'ترتيب حسب',
      sortNewest: 'الأحدث',
      sortOldest: 'الأقدم',
      sortHighestRated: 'الأعلى تقييمًا',
      sortLowestRated: 'الأقل تقييمًا',
      sortMostHelpful: 'الأكثر فائدة',
      allRatings: 'جميع التقييمات',
      verifiedPurchases: 'مشتريات موثقة',
      withImages: 'مع الصور'
    },
    fr: {
      review: 'Avis',
      reviews: 'Avis',
      customerReviews: 'Avis des clients',
      writeReview: 'Écrire un avis',
      noReviews: "Aucun avis pour le moment. Soyez le premier à donner votre avis sur ce produit!",
      loadMore: "Charger plus d'avis",
      sortBy: 'Trier par',
      sortNewest: 'Plus récents',
      sortOldest: 'Plus anciens',
      sortHighestRated: 'Mieux notés',
      sortLowestRated: 'Moins bien notés',
      sortMostHelpful: 'Plus utiles',
      allRatings: 'Toutes les notes',
      verifiedPurchases: 'Achats vérifiés',
      withImages: 'Avec images'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

module.exports = ReviewsSection;
