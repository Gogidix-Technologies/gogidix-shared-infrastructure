/**
 * CommentsSection Template
 * A complete comments section template with social features, sorting, and filtering
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const { Comment, CommentForm } = require('../../components/Comment');

/**
 * CommentsSection template generator
 * @param {Object} props - Template properties
 * @param {Array} props.comments - Array of comment objects to display
 * @param {number} [props.totalComments] - Total number of comments (for pagination)
 * @param {boolean} [props.allowReplies=true] - Whether to allow comment replies
 * @param {boolean} [props.allowSorting=true] - Whether to show sorting options
 * @param {boolean} [props.allowFiltering=true] - Whether to show filtering options
 * @param {string} [props.defaultSort='newest'] - Default sort order: newest, oldest, mostLiked
 * @param {Function} [props.onCommentSubmit] - Handler for comment submission
 * @param {Function} [props.onReplySubmit] - Handler for reply submission
 * @param {Function} [props.onSortChange] - Handler for sort change
 * @param {Function} [props.onFilterChange] - Handler for filter change
 * @param {string} [props.locale=localization.DEFAULT_LOCALE] - Locale for the component
 * @param {string} [props.className=""] - Additional CSS class names
 * @returns {string} HTML for the comments section template
 */
function CommentsSection({
  comments = [],
  totalComments,
  allowReplies = true,
  allowSorting = true,
  allowFiltering = true,
  defaultSort = 'newest',
  onCommentSubmit,
  onReplySubmit,
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
  const sectionId = `comments-section-${Math.random().toString(36).substring(2, 10)}`;
  
  // Create sorting controls
  let sortingHtml = '';
  if (allowSorting) {
    const sortOptions = [
      { value: 'newest', label: translations.sortNewest },
      { value: 'oldest', label: translations.sortOldest },
      { value: 'mostLiked', label: translations.sortMostLiked },
    ];
    
    sortingHtml = `
      <div class="comments-sorting" style="
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
      <div class="comments-filtering" style="
        display: flex;
        flex-wrap: wrap;
        gap: ${tokens.spacing.sm};
        margin-bottom: ${tokens.spacing.md};
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
          ${translations.allComments}
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
        
        <button
          class="filter-btn"
          onclick="handleFilterChange('${sectionId}', 'withImages')"
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
      </div>
    `;
  }
  
  // Create comment form
  const commentForm = CommentForm({
    id: `${sectionId}-form`,
    placeholder: translations.writeComment,
    submitLabel: translations.postComment,
    onSubmit: onCommentSubmit ? `submitComment('${sectionId}')` : '',
    locale,
  });
  
  // Create comments list
  let commentsHtml = '';
  if (comments.length === 0) {
    commentsHtml = `
      <div class="comments-empty" style="
        padding: ${tokens.spacing.lg};
        text-align: center;
        background: ${tokens.colors.neutral.gray[100]};
        border-radius: ${tokens.borders.borderRadius.md};
        margin-bottom: ${tokens.spacing.lg};
      ">
        <i class="icon-comments" style="
          font-size: 2rem;
          color: ${tokens.colors.neutral.gray[400]};
          margin-bottom: ${tokens.spacing.sm};
          display: block;
        "></i>
        <p style="
          margin: 0;
          color: ${tokens.colors.neutral.gray[600]};
        ">
          ${translations.noComments}
        </p>
      </div>
    `;
  } else {
    commentsHtml = `
      <div class="comments-list" style="
        display: flex;
        flex-direction: column;
        gap: ${tokens.spacing.lg};
      ">
        ${comments.map(comment => {
          return Comment({
            ...comment,
            showReplyForm: allowReplies,
            onReply: onReplySubmit ? `replyToComment('${sectionId}', '${comment.id}')` : '',
            locale
          });
        }).join('')}
      </div>
    `;
  }
  
  // Create pagination if needed
  let paginationHtml = '';
  if (totalComments > comments.length) {
    paginationHtml = `
      <div class="comments-pagination" style="
        display: flex;
        justify-content: center;
        margin-top: ${tokens.spacing.xl};
      ">
        <button
          onclick="loadMoreComments('${sectionId}')"
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
  
  // Build the complete comments section
  return `
    <section 
      id="${sectionId}" 
      class="comments-section ${className}" 
      dir="${dir}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div class="comments-header" style="
        margin-bottom: ${tokens.spacing.lg};
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        align-items: center;
      ">
        <h2 style="
          font-size: ${tokens.typography.textStyles.heading3.fontSize};
          font-weight: ${tokens.typography.fontWeights.semiBold};
          margin: 0;
        ">
          ${translations.comments}
          ${totalComments !== undefined ? `
            <span style="
              color: ${tokens.colors.neutral.gray[600]};
              font-size: 0.8em;
            ">
              (${localization.formatLocalizedNumber(totalComments, locale)})
            </span>
          ` : ''}
        </h2>
        
        <div>
          ${sortingHtml}
        </div>
      </div>
      
      ${filteringHtml}
      
      <div class="comments-form-container" style="
        margin-bottom: ${tokens.spacing.xl};
      ">
        ${commentForm}
      </div>
      
      ${commentsHtml}
      
      ${paginationHtml}
      
      <script>
        // Event handlers
        function submitComment(sectionId) {
          ${onCommentSubmit ? `
            const form = document.getElementById(\`\${sectionId}-form\`);
            const content = form.querySelector('textarea').value;
            
            // Custom handler would be injected here
            console.log('Comment submitted:', sectionId, content);
          ` : ''}
        }
        
        function replyToComment(sectionId, commentId) {
          ${onReplySubmit ? `
            const replyForm = document.getElementById(\`comment-\${commentId}-reply-form\`);
            const content = replyForm.querySelector('textarea').value;
            
            // Custom handler would be injected here
            console.log('Reply submitted:', sectionId, commentId, content);
          ` : ''}
        }
        
        function handleSortChange(sectionId, sortValue) {
          ${onSortChange ? `
            // Update active class on sort dropdown
            const sortDropdown = document.getElementById(\`\${sectionId}-sort\`);
            
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
              if (btn.textContent.trim() === translations[filterValue] || 
                  (filterValue === 'all' && btn.textContent.trim() === translations.allComments)) {
                btn.classList.add('active');
              }
            });
            
            // Custom handler would be injected here
            console.log('Filter changed:', sectionId, filterValue);
          ` : ''}
        }
        
        function loadMoreComments(sectionId) {
          // Custom handler would be injected here
          console.log('Load more comments:', sectionId);
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
      comments: 'Comments',
      writeComment: 'Write your comment...',
      postComment: 'Post Comment',
      noComments: 'No comments yet. Be the first to comment!',
      loadMore: 'Load More Comments',
      sortBy: 'Sort by',
      sortNewest: 'Newest First',
      sortOldest: 'Oldest First',
      sortMostLiked: 'Most Liked',
      allComments: 'All Comments',
      verifiedPurchases: 'Verified Purchases',
      withImages: 'With Images'
    },
    ar: {
      comments: 'التعليقات',
      writeComment: 'اكتب تعليقك...',
      postComment: 'نشر التعليق',
      noComments: 'لا توجد تعليقات بعد. كن أول من يعلق!',
      loadMore: 'تحميل المزيد من التعليقات',
      sortBy: 'ترتيب حسب',
      sortNewest: 'الأحدث أولاً',
      sortOldest: 'الأقدم أولاً',
      sortMostLiked: 'الأكثر إعجابًا',
      allComments: 'جميع التعليقات',
      verifiedPurchases: 'مشتريات موثقة',
      withImages: 'مع الصور'
    },
    fr: {
      comments: 'Commentaires',
      writeComment: 'Écrivez votre commentaire...',
      postComment: 'Publier le commentaire',
      noComments: 'Pas encore de commentaires. Soyez le premier à commenter!',
      loadMore: 'Charger plus de commentaires',
      sortBy: 'Trier par',
      sortNewest: 'Plus récents',
      sortOldest: 'Plus anciens',
      sortMostLiked: 'Plus aimés',
      allComments: 'Tous les commentaires',
      verifiedPurchases: 'Achats vérifiés',
      withImages: 'Avec images'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

module.exports = CommentsSection;
