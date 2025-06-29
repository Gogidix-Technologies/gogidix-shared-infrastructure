/**
 * Social Commerce Ecosystem UI Design System
 * Main entry point for importing design tokens, components and utilities
 */

const tokens = require('./tokens');
const components = require('./components');
const localization = require('./utils/localization');

module.exports = {
  tokens,
  components,
  utils: {
    localization
  }
};
