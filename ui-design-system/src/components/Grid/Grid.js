/**
 * Grid System Component
 * A responsive grid system for consistent layouts across all screen sizes.
 * Designed for Team Alpha's responsive UI requirements with mobile-first approach.
 */

const tokens = require('../../tokens');

/**
 * Container component - provides a centered, max-width container
 * @param {object} props - Component properties
 * @returns {string} - HTML for the container
 */
function Container({
  children,
  fluid = false,
  size = 'lg',
  className = '',
  ...otherProps
}) {
  // Determine max-width based on size
  const maxWidth = fluid ? '100%' : tokens.spacing.layout[`container${size.charAt(0).toUpperCase()}${size.slice(1)}`];
  
  // Generate container HTML
  return `
    <div 
      class="container ${className}" 
      style="
        width: 100%;
        max-width: ${maxWidth};
        margin-left: auto;
        margin-right: auto;
        padding-left: ${tokens.spacing.layout.margin};
        padding-right: ${tokens.spacing.layout.margin};
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${children}
    </div>
  `;
}

/**
 * Row component - creates a flexbox row for the grid system
 * @param {object} props - Component properties
 * @returns {string} - HTML for the row
 */
function Row({
  children,
  gutter = 'md',
  align = 'start', // start, center, end, stretch
  justify = 'start', // start, center, end, between, around
  wrap = true,
  className = '',
  ...otherProps
}) {
  // Calculate gutter size
  const gutterSize = typeof gutter === 'string' 
    ? tokens.spacing[gutter] || tokens.spacing.md
    : `${gutter}px`;
  
  // Map align and justify values to flexbox properties
  const alignMap = {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    stretch: 'stretch',
    baseline: 'baseline'
  };
  
  const justifyMap = {
    start: 'flex-start',
    center: 'center',
    end: 'flex-end',
    between: 'space-between',
    around: 'space-around',
    evenly: 'space-evenly'
  };
  
  // Generate row HTML
  return `
    <div 
      class="row ${className}" 
      style="
        display: flex;
        flex-wrap: ${wrap ? 'wrap' : 'nowrap'};
        align-items: ${alignMap[align] || alignMap.start};
        justify-content: ${justifyMap[justify] || justifyMap.start};
        margin-left: -${gutterSize};
        margin-right: -${gutterSize};
        box-sizing: border-box;
      "
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${children}
    </div>
  `;
}

/**
 * Column component - creates a flexbox column within the grid system
 * @param {object} props - Component properties
 * @returns {string} - HTML for the column
 */
function Column({
  children,
  xs = 12, // columns on extra small screens (0+)
  sm = null, // columns on small screens (576px+)
  md = null, // columns on medium screens (768px+)
  lg = null, // columns on large screens (992px+)
  xl = null, // columns on extra large screens (1200px+)
  xxl = null, // columns on extra extra large screens (1600px+)
  offset = {}, // offsets for different screen sizes
  gutter = 'md',
  align = null, // Individual column alignment (overrides row)
  className = '',
  ...otherProps
}) {
  // Calculate gutter size
  const gutterSize = typeof gutter === 'string' 
    ? tokens.spacing[gutter] || tokens.spacing.md
    : `${gutter}px`;
  
  // Calculate column width percentages
  const getWidthStyle = (columns) => {
    if (columns === 'auto') return 'auto';
    if (columns === 0) return '0';
    return columns ? `${(columns / 12) * 100}%` : null;
  };
  
  // Calculate column offset percentages
  const getOffsetStyle = (offsetColumns) => {
    return offsetColumns ? `${(offsetColumns / 12) * 100}%` : null;
  };
  
  // Build responsive styles
  const baseStyles = `
    padding-left: ${gutterSize};
    padding-right: ${gutterSize};
    box-sizing: border-box;
  `;
  
  // Apply column alignment if specified
  const alignStyle = align ? `align-self: ${align};` : '';
  
  // Generate responsive CSS
  const responsiveStyles = `
    flex: 0 0 ${getWidthStyle(xs)};
    max-width: ${getWidthStyle(xs)};
    ${offset.xs ? `margin-left: ${getOffsetStyle(offset.xs)};` : ''}
    
    @media (min-width: 576px) {
      ${sm !== null ? `
        flex: 0 0 ${getWidthStyle(sm)};
        max-width: ${getWidthStyle(sm)};
      ` : ''}
      ${offset.sm ? `margin-left: ${getOffsetStyle(offset.sm)};` : ''}
    }
    
    @media (min-width: 768px) {
      ${md !== null ? `
        flex: 0 0 ${getWidthStyle(md)};
        max-width: ${getWidthStyle(md)};
      ` : ''}
      ${offset.md ? `margin-left: ${getOffsetStyle(offset.md)};` : ''}
    }
    
    @media (min-width: 992px) {
      ${lg !== null ? `
        flex: 0 0 ${getWidthStyle(lg)};
        max-width: ${getWidthStyle(lg)};
      ` : ''}
      ${offset.lg ? `margin-left: ${getOffsetStyle(offset.lg)};` : ''}
    }
    
    @media (min-width: 1200px) {
      ${xl !== null ? `
        flex: 0 0 ${getWidthStyle(xl)};
        max-width: ${getWidthStyle(xl)};
      ` : ''}
      ${offset.xl ? `margin-left: ${getOffsetStyle(offset.xl)};` : ''}
    }
    
    @media (min-width: 1600px) {
      ${xxl !== null ? `
        flex: 0 0 ${getWidthStyle(xxl)};
        max-width: ${getWidthStyle(xxl)};
      ` : ''}
      ${offset.xxl ? `margin-left: ${getOffsetStyle(offset.xxl)};` : ''}
    }
  `;
  
  // Generate column HTML
  return `
    <div 
      class="column ${className}" 
      style="${baseStyles} ${alignStyle}"
      data-responsive-styles="${responsiveStyles.replace(/\n/g, ' ').replace(/\s+/g, ' ')}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${children}
    </div>
  `;
}

/**
 * Responsive utility that hides content based on breakpoint
 * @param {object} props - Component properties
 * @returns {string} - HTML with display conditions
 */
function Responsive({
  children,
  hideOn = [], // Array of breakpoints to hide on: xs, sm, md, lg, xl, xxl
  showOn = [], // Array of breakpoints to show on: xs, sm, md, lg, xl, xxl
  className = '',
  ...otherProps
}) {
  // Generate responsive display CSS
  const generateDisplayStyles = () => {
    const styles = [];
    
    // Hide on specified breakpoints
    if (hideOn.length > 0) {
      if (hideOn.includes('xs')) {
        styles.push('display: none;');
      }
      
      if (hideOn.includes('sm')) {
        styles.push('@media (min-width: 576px) and (max-width: 767px) { display: none; }');
      }
      
      if (hideOn.includes('md')) {
        styles.push('@media (min-width: 768px) and (max-width: 991px) { display: none; }');
      }
      
      if (hideOn.includes('lg')) {
        styles.push('@media (min-width: 992px) and (max-width: 1199px) { display: none; }');
      }
      
      if (hideOn.includes('xl')) {
        styles.push('@media (min-width: 1200px) and (max-width: 1599px) { display: none; }');
      }
      
      if (hideOn.includes('xxl')) {
        styles.push('@media (min-width: 1600px) { display: none; }');
      }
    }
    
    // Show only on specified breakpoints (if both hideOn and showOn are provided, hideOn takes precedence)
    if (showOn.length > 0 && hideOn.length === 0) {
      // Hide by default, then selectively show
      styles.push('display: none;');
      
      if (showOn.includes('xs')) {
        styles.push('@media (max-width: 575px) { display: block; }');
      }
      
      if (showOn.includes('sm')) {
        styles.push('@media (min-width: 576px) and (max-width: 767px) { display: block; }');
      }
      
      if (showOn.includes('md')) {
        styles.push('@media (min-width: 768px) and (max-width: 991px) { display: block; }');
      }
      
      if (showOn.includes('lg')) {
        styles.push('@media (min-width: 992px) and (max-width: 1199px) { display: block; }');
      }
      
      if (showOn.includes('xl')) {
        styles.push('@media (min-width: 1200px) and (max-width: 1599px) { display: block; }');
      }
      
      if (showOn.includes('xxl')) {
        styles.push('@media (min-width: 1600px) { display: block; }');
      }
    }
    
    return styles.join(' ');
  };
  
  // Generate responsive HTML
  return `
    <div 
      class="responsive ${className}" 
      data-responsive-display="${generateDisplayStyles().replace(/\n/g, ' ').replace(/\s+/g, ' ')}"
      ${Object.entries(otherProps).map(([key, value]) => `${key}="${value}"`).join(' ')}
    >
      ${children}
    </div>
  `;
}

module.exports = {
  Container,
  Row,
  Column,
  Responsive
};
