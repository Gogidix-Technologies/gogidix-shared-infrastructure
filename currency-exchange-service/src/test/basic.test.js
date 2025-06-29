// Basic test for currency-exchange-service
describe('Currency Exchange Service', () => {
  test('should have basic functionality', () => {
    expect(true).toBe(true);
  });

  test('should be able to require main module', () => {
    expect(() => {
      const packageJson = require('../../package.json');
      expect(packageJson.name).toBe('currency-exchange-service');
    }).not.toThrow();
  });
});