/**
 * Comment Component
 * A versatile comment component for social interactions
 * For use by Team Alpha in customer-facing interfaces
 */

const tokens = require('../../tokens');
const localization = require('../../utils/localization');
const Button = require('../Button');

/**
 * Comment component generator
 * @param {object} props - Component properties
 * @returns {string} - HTML for the comment element
 */
function Comment({
  id,
  author = {},
  content = '',
  timestamp = new Date(),
  likes = 0,
  isLiked = false,
  replies = [],
  showReplyForm = false,
  depth = 0,
  maxDepth = 3,
  locale = localization.DEFAULT_LOCALE,
  onLike,
  onReply,
  onDelete,
  onReport,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const commentId = id || `comment-${Math.random().toString(36).substring(2, 9)}`;
  
  // Format timestamp based on locale
  const formattedTime = localization.formatLocalizedDate(timestamp, locale, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  });

  // Translations
  const translations = getTranslations(locale);
  
  // Get date display (today, yesterday, or date)
  const timeDisplay = getTimeDisplay(timestamp, locale);
  
  // Determine if nested comments should be indented
  const showIndent = depth > 0 && depth <= maxDepth;
  
  // Create reply button
  const replyButton = Button({
    children: translations.reply,
    variant: 'text',
    size: 'sm',
    onClick: `handleReply('${commentId}')`,
    locale
  });
  
  // Create like button
  const likeButton = Button({
    children: `
      <i class="icon-${isLiked ? 'heart-filled' : 'heart'}" style="margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing[1]};"></i>
      ${translations.like}
    `,
    variant: 'text',
    size: 'sm',
    onClick: `handleLike('${commentId}')`,
    className: isLiked ? 'liked' : '',
    locale
  });
  
  // Create report button
  const reportButton = Button({
    children: translations.report,
    variant: 'text',
    size: 'sm',
    onClick: `handleReport('${commentId}')`,
    locale
  });
  
  // Create delete button (only visible for the author)
  const deleteButton = author.isCurrentUser ? Button({
    children: translations.delete,
    variant: 'text',
    size: 'sm',
    onClick: `handleDelete('${commentId}')`,
    className: 'delete-comment',
    locale
  }) : '';
  
  // Create reply form if showing
  const replyForm = showReplyForm ? `
    <div class="reply-form" style="
      margin-top: ${tokens.spacing.md};
      margin-${dir === 'rtl' ? 'right' : 'left'}: ${depth < maxDepth ? tokens.spacing.xl : '0'};
    ">
      <div style="
        display: flex;
        gap: ${tokens.spacing.md};
        align-items: flex-start;
      ">
        <div class="avatar" style="
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background-color: ${tokens.colors.neutral.gray[200]};
          flex-shrink: 0;
          overflow: hidden;
        ">
          <img 
            src="${author.avatarUrl || ''}" 
            alt="${author.name || translations.you}"
            style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: ${author.avatarUrl ? 'block' : 'none'};
            "
          />
        </div>
        
        <div style="flex-grow: 1;">
          <textarea 
            id="reply-${commentId}"
            placeholder="${translations.writeReply}"
            style="
              width: 100%;
              min-height: 80px;
              padding: ${tokens.spacing.sm};
              border: ${tokens.borders.presets.input.normal};
              border-radius: ${tokens.borders.borderRadius.md};
              font-family: inherit;
              font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
              resize: vertical;
              
              &:focus {
                border: ${tokens.borders.presets.input.focus};
                box-shadow: ${tokens.shadows.presets.input.focus};
                outline: none;
              }
            "
          ></textarea>
          
          <div style="
            display: flex;
            justify-content: flex-end;
            margin-top: ${tokens.spacing.sm};
            gap: ${tokens.spacing.sm};
          ">
            ${Button({
              children: translations.cancel,
              variant: 'secondary',
              size: 'sm',
              onClick: `cancelReply('${commentId}')`,
              locale
            })}
            
            ${Button({
              children: translations.submitReply,
              variant: 'primary',
              size: 'sm',
              onClick: `submitReply('${commentId}')`,
              locale
            })}
          </div>
        </div>
      </div>
    </div>
  ` : '';
  
  // Generate replies HTML if any and if not too deep
  let repliesHtml = '';
  if (replies && replies.length > 0 && depth < maxDepth) {
    repliesHtml = `
      <div class="comment-replies" style="
        margin-top: ${tokens.spacing.md};
        margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xl};
      ">
        ${replies.map(reply => {
          // Recursively render nested comments
          return Comment({
            ...reply,
            depth: depth + 1,
            maxDepth,
            locale,
            onLike,
            onReply,
            onDelete,
            onReport
          });
        }).join('')}
      </div>
    `;
  } else if (depth >= maxDepth && replies && replies.length > 0) {
    // Show "view more replies" button if depth exceeds maxDepth
    repliesHtml = `
      <div style="
        margin-top: ${tokens.spacing.sm};
        margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xl};
      ">
        ${Button({
          children: `${translations.viewMoreReplies} (${replies.length})`,
          variant: 'text',
          size: 'sm',
          onClick: `viewMoreReplies('${commentId}')`,
          locale
        })}
      </div>
    `;
  }
  
  // Build comment HTML
  return `
    <div 
      id="${commentId}" 
      class="comment ${className}" 
      dir="${dir}"
      data-comment-id="${id}"
      style="
        margin-bottom: ${tokens.spacing.md};
        padding: ${tokens.spacing.md};
        border-radius: ${tokens.borders.borderRadius.md};
        background-color: ${depth % 2 === 0 ? tokens.colors.neutral.white : tokens.colors.neutral.gray[50]};
        ${showIndent ? `border-${dir === 'rtl' ? 'right' : 'left'}: 2px solid ${tokens.colors.neutral.gray[200]};` : ''}
        transition: ${tokens.animations.transitions.all};
        
        &:hover {
          background-color: ${tokens.colors.neutral.gray[50]};
        }
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div class="comment-header" style="
        display: flex;
        align-items: center;
        margin-bottom: ${tokens.spacing.sm};
        gap: ${tokens.spacing.sm};
      ">
        <div class="author-avatar" style="
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background-color: ${tokens.colors.neutral.gray[200]};
          flex-shrink: 0;
          overflow: hidden;
        ">
          <img 
            src="${author.avatarUrl || ''}" 
            alt="${author.name || ''}"
            style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: ${author.avatarUrl ? 'block' : 'none'};
            "
          />
        </div>
        
        <div style="flex-grow: 1;">
          <div style="
            display: flex;
            align-items: baseline;
            flex-wrap: wrap;
            gap: ${tokens.spacing.xs} ${tokens.spacing.sm};
          ">
            <span class="author-name" style="
              font-weight: ${tokens.typography.fontWeights.medium};
              color: ${tokens.colors.neutral.gray[900]};
            ">
              ${author.name || ''}
              ${author.isCurrentUser ? `<span style="
                font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
                color: ${tokens.colors.neutral.gray[600]};
                margin-${dir === 'rtl' ? 'right' : 'left'}: ${tokens.spacing.xs};
              ">(${translations.you})</span>` : ''}
            </span>
            
            ${author.badge ? `
              <span class="author-badge" style="
                font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
                padding: ${tokens.spacing.xs} ${tokens.spacing.sm};
                border-radius: ${tokens.borders.borderRadius.sm};
                background-color: ${tokens.colors.brand.accent[100]};
                color: ${tokens.colors.brand.accent[700]};
              ">
                ${author.badge}
              </span>
            ` : ''}
            
            <span class="comment-time" style="
              font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
              color: ${tokens.colors.neutral.gray[600]};
            ">
              ${timeDisplay}
            </span>
          </div>
        </div>
      </div>
      
      <div class="comment-content" style="
        margin-bottom: ${tokens.spacing.md};
        line-height: ${tokens.typography.lineHeights.relaxed};
        color: ${tokens.colors.neutral.gray[800]};
      ">
        ${content}
      </div>
      
      <div class="comment-actions" style="
        display: flex;
        align-items: center;
        gap: ${tokens.spacing.sm};
        flex-wrap: wrap;
      ">
        ${likeButton}
        <span class="like-count" style="
          font-size: ${tokens.typography.textStyles.bodySmall.fontSize};
          color: ${tokens.colors.neutral.gray[600]};
          margin-${dir === 'rtl' ? 'left' : 'right'}: ${tokens.spacing.sm};
        ">
          ${likes > 0 ? localization.formatLocalizedNumber(likes, locale) : ''}
        </span>
        ${replyButton}
        ${reportButton}
        ${deleteButton}
      </div>
      
      ${replyForm}
      ${repliesHtml}
    </div>
  `;
}

/**
 * Get translations based on locale
 */
function getTranslations(locale) {
  const translations = {
    en: {
      reply: 'Reply',
      like: 'Like',
      report: 'Report',
      delete: 'Delete',
      you: 'You',
      writeReply: 'Write a reply...',
      cancel: 'Cancel',
      submitReply: 'Reply',
      viewMoreReplies: 'View more replies',
      today: 'Today',
      yesterday: 'Yesterday',
      at: 'at'
    },
    ar: {
      reply: 'رد',
      like: 'إعجاب',
      report: 'إبلاغ',
      delete: 'حذف',
      you: 'أنت',
      writeReply: 'اكتب ردًا...',
      cancel: 'إلغاء',
      submitReply: 'رد',
      viewMoreReplies: 'عرض المزيد من الردود',
      today: 'اليوم',
      yesterday: 'الأمس',
      at: 'في'
    },
    fr: {
      reply: 'Répondre',
      like: 'J\'aime',
      report: 'Signaler',
      delete: 'Supprimer',
      you: 'Vous',
      writeReply: 'Écrire une réponse...',
      cancel: 'Annuler',
      submitReply: 'Répondre',
      viewMoreReplies: 'Voir plus de réponses',
      today: 'Aujourd\'hui',
      yesterday: 'Hier',
      at: 'à'
    },
    // Add more locales as needed
  };
  
  return translations[locale] || translations['en'];
}

/**
 * Get relative time display
 */
function getTimeDisplay(timestamp, locale) {
  const translations = getTranslations(locale);
  const today = new Date();
  const commentDate = new Date(timestamp);
  
  // Format time
  const timeStr = localization.formatLocalizedDate(timestamp, locale, {
    hour: 'numeric',
    minute: 'numeric'
  });
  
  // Check if same day
  if (today.toDateString() === commentDate.toDateString()) {
    return `${translations.today} ${translations.at} ${timeStr}`;
  }
  
  // Check if yesterday
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  if (yesterday.toDateString() === commentDate.toDateString()) {
    return `${translations.yesterday} ${translations.at} ${timeStr}`;
  }
  
  // Default to full date
  return localization.formatLocalizedDate(timestamp, locale, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric'
  });
}

/**
 * CommentForm component for adding new comments
 */
function CommentForm({
  id,
  placeholderText = '',
  submitButtonText = 'Submit',
  avatarUrl = '',
  locale = localization.DEFAULT_LOCALE,
  onSubmit,
  className = '',
  ...otherProps
}) {
  // Get text direction based on locale
  const dir = localization.getTextDirection(locale);
  
  // Generate unique ID if not provided
  const formId = id || `comment-form-${Math.random().toString(36).substring(2, 9)}`;
  
  // Get translations based on locale
  const translations = getTranslations(locale);
  
  return `
    <div 
      id="${formId}" 
      class="comment-form ${className}" 
      dir="${dir}"
      style="
        margin-bottom: ${tokens.spacing.lg};
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      <div style="
        display: flex;
        gap: ${tokens.spacing.md};
        align-items: flex-start;
      ">
        <div class="avatar" style="
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background-color: ${tokens.colors.neutral.gray[200]};
          flex-shrink: 0;
          overflow: hidden;
        ">
          <img 
            src="${avatarUrl || ''}" 
            alt=""
            style="
              width: 100%;
              height: 100%;
              object-fit: cover;
              display: ${avatarUrl ? 'block' : 'none'};
            "
          />
        </div>
        
        <div style="flex-grow: 1;">
          <textarea 
            id="${formId}-input"
            placeholder="${placeholderText || translations.writeReply}"
            style="
              width: 100%;
              min-height: 100px;
              padding: ${tokens.spacing.sm};
              border: ${tokens.borders.presets.input.normal};
              border-radius: ${tokens.borders.borderRadius.md};
              font-family: inherit;
              font-size: ${tokens.typography.textStyles.bodyMedium.fontSize};
              resize: vertical;
              
              &:focus {
                border: ${tokens.borders.presets.input.focus};
                box-shadow: ${tokens.shadows.presets.input.focus};
                outline: none;
              }
            "
          ></textarea>
          
          <div style="
            display: flex;
            justify-content: flex-end;
            margin-top: ${tokens.spacing.sm};
          ">
            ${Button({
              children: submitButtonText || translations.submitReply,
              variant: 'primary',
              size: 'md',
              onClick: `submitComment('${formId}')`,
              locale
            })}
          </div>
        </div>
      </div>
    </div>
  `;
}

module.exports = { Comment, CommentForm };
