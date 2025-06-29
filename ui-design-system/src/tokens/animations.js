/**
 * Animation and transition tokens for the Social Commerce Ecosystem UI Design System
 * These animation definitions ensure consistent motion patterns
 * across Team Alpha (customer-facing) and Team Omega (operations) interfaces.
 */

// Duration values in milliseconds
const durations = {
  instant: 0,
  extraFast: 100,
  fast: 200,
  normal: 300,
  slow: 500,
  extraSlow: 800
};

// Easing functions for natural movement
const easings = {
  // Standard easing curves
  linear: 'linear',
  easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
  easeOut: 'cubic-bezier(0, 0, 0.2, 1)',
  easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
  
  // Specialized easing curves
  emphasizedAccelerate: 'cubic-bezier(0.3, 0, 0.8, 0.15)',
  emphasizedDecelerate: 'cubic-bezier(0.05, 0.7, 0.1, 1.0)',
  standard: 'cubic-bezier(0.2, 0, 0, 1)',
  standardAccelerate: 'cubic-bezier(0.3, 0, 1, 1)',
  standardDecelerate: 'cubic-bezier(0, 0, 0, 1)'
};

// Presets for common transition patterns
const transitions = {
  hover: `${durations.fast}ms ${easings.easeOut}`,
  focus: `${durations.fast}ms ${easings.easeOut}`,
  active: `${durations.extraFast}ms ${easings.easeOut}`,
  fade: `opacity ${durations.normal}ms ${easings.easeInOut}`,
  transform: `transform ${durations.normal}ms ${easings.easeInOut}`,
  color: `color ${durations.fast}ms ${easings.easeOut}`,
  background: `background-color ${durations.fast}ms ${easings.easeOut}`,
  border: `border ${durations.fast}ms ${easings.easeOut}`,
  boxShadow: `box-shadow ${durations.fast}ms ${easings.easeOut}`,
  all: `all ${durations.normal}ms ${easings.easeInOut}`
};

// Keyframe animations
const keyframes = {
  fadeIn: `
    @keyframes fadeIn {
      from { opacity: 0; }
      to { opacity: 1; }
    }
  `,
  fadeOut: `
    @keyframes fadeOut {
      from { opacity: 1; }
      to { opacity: 0; }
    }
  `,
  slideInRight: `
    @keyframes slideInRight {
      from { transform: translateX(100%); }
      to { transform: translateX(0); }
    }
  `,
  slideOutRight: `
    @keyframes slideOutRight {
      from { transform: translateX(0); }
      to { transform: translateX(100%); }
    }
  `,
  slideInLeft: `
    @keyframes slideInLeft {
      from { transform: translateX(-100%); }
      to { transform: translateX(0); }
    }
  `,
  slideOutLeft: `
    @keyframes slideOutLeft {
      from { transform: translateX(0); }
      to { transform: translateX(-100%); }
    }
  `,
  slideInUp: `
    @keyframes slideInUp {
      from { transform: translateY(100%); }
      to { transform: translateY(0); }
    }
  `,
  slideOutUp: `
    @keyframes slideOutUp {
      from { transform: translateY(0); }
      to { transform: translateY(-100%); }
    }
  `,
  slideInDown: `
    @keyframes slideInDown {
      from { transform: translateY(-100%); }
      to { transform: translateY(0); }
    }
  `,
  slideOutDown: `
    @keyframes slideOutDown {
      from { transform: translateY(0); }
      to { transform: translateY(100%); }
    }
  `,
  pulse: `
    @keyframes pulse {
      0% { transform: scale(1); }
      50% { transform: scale(1.05); }
      100% { transform: scale(1); }
    }
  `,
  spin: `
    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `,
  shimmer: `
    @keyframes shimmer {
      0% { background-position: -200% 0; }
      100% { background-position: 200% 0; }
    }
  `
};

// Animation presets with duration, delay, timing function, and fill mode
const animations = {
  fadeIn: {
    name: 'fadeIn',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeOut,
    fillMode: 'forwards'
  },
  fadeOut: {
    name: 'fadeOut',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeOut,
    fillMode: 'forwards'
  },
  slideInRight: {
    name: 'slideInRight',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeOut,
    fillMode: 'forwards'
  },
  slideOutRight: {
    name: 'slideOutRight',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeIn,
    fillMode: 'forwards'
  },
  slideInLeft: {
    name: 'slideInLeft',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeOut,
    fillMode: 'forwards'
  },
  slideOutLeft: {
    name: 'slideOutLeft',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeIn,
    fillMode: 'forwards'
  },
  slideInUp: {
    name: 'slideInUp',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeOut,
    fillMode: 'forwards'
  },
  slideOutUp: {
    name: 'slideOutUp',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeIn,
    fillMode: 'forwards'
  },
  slideInDown: {
    name: 'slideInDown',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeOut,
    fillMode: 'forwards'
  },
  slideOutDown: {
    name: 'slideOutDown',
    duration: `${durations.normal}ms`,
    timingFunction: easings.easeIn,
    fillMode: 'forwards'
  },
  pulse: {
    name: 'pulse',
    duration: `${durations.slow}ms`,
    timingFunction: easings.easeInOut,
    fillMode: 'forwards',
    iterationCount: 'infinite'
  },
  spin: {
    name: 'spin',
    duration: '1s',
    timingFunction: easings.linear,
    fillMode: 'none',
    iterationCount: 'infinite'
  },
  shimmer: {
    name: 'shimmer',
    duration: '1.5s',
    timingFunction: easings.easeInOut,
    fillMode: 'forwards',
    iterationCount: 'infinite'
  }
};

// Helper function to generate animation CSS
function generateAnimationCSS(animation, delay = '0s') {
  if (!animation) return '';
  
  return `
    animation-name: ${animation.name};
    animation-duration: ${animation.duration || durations.normal + 'ms'};
    animation-timing-function: ${animation.timingFunction || easings.easeInOut};
    animation-fill-mode: ${animation.fillMode || 'forwards'};
    animation-delay: ${delay};
    ${animation.iterationCount ? `animation-iteration-count: ${animation.iterationCount};` : ''}
  `;
}

// Motion patterns for specific UI elements
const motionPatterns = {
  // Mobile navigation drawer
  drawer: {
    enter: animations.slideInLeft,
    exit: animations.slideOutLeft,
    overlay: {
      enter: animations.fadeIn,
      exit: animations.fadeOut
    },
  },
  
  // Modal dialogs
  modal: {
    enter: animations.fadeIn,
    exit: animations.fadeOut,
    content: {
      enter: animations.slideInUp,
      exit: animations.slideOutDown
    }
  },
  
  // Toast/Notification
  toast: {
    enter: animations.slideInUp,
    exit: animations.fadeOut
  },
  
  // Page transitions
  page: {
    enter: animations.fadeIn,
    exit: animations.fadeOut
  },
  
  // Loading indicators
  loading: {
    spinner: animations.spin,
    skeleton: animations.shimmer
  },
  
  // Button states
  button: {
    hover: transitions.hover,
    active: transitions.active,
    focus: transitions.focus
  }
};

// Export all animation tokens
module.exports = {
  durations,
  easings,
  transitions,
  keyframes,
  animations,
  generateAnimationCSS,
  motionPatterns
};
