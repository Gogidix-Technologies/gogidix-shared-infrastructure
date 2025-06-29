const request = require('supertest');
const app = require('../src/index');

describe('Currency Exchange Service', () => {
  test('GET /health should return OK', async () => {
    const response = await request(app)
      .get('/health')
      .expect(200);
    
    expect(response.body.status).toBe('OK');
    expect(response.body.service).toBe('currency-exchange');
  });

  test('GET /rates should return exchange rates', async () => {
    const response = await request(app)
      .get('/rates')
      .expect(200);
    
    expect(response.body).toHaveProperty('rates');
    expect(response.body).toHaveProperty('timestamp');
    expect(response.body.rates).toHaveProperty('USD');
  });

  test('POST /convert should convert currencies', async () => {
    const response = await request(app)
      .post('/convert')
      .send({
        amount: 100,
        from: 'USD',
        to: 'EUR'
      })
      .expect(200);
    
    expect(response.body).toHaveProperty('convertedAmount');
    expect(response.body).toHaveProperty('exchangeRate');
    expect(response.body.fromCurrency).toBe('USD');
    expect(response.body.toCurrency).toBe('EUR');
  });

  test('POST /convert should validate required fields', async () => {
    const response = await request(app)
      .post('/convert')
      .send({
        amount: 100
      })
      .expect(400);
    
    expect(response.body.error).toContain('required');
  });

  test('GET /metrics should return service metrics', async () => {
    const response = await request(app)
      .get('/metrics')
      .expect(200);
    
    expect(response.body).toHaveProperty('service');
    expect(response.body).toHaveProperty('uptime');
    expect(response.body.service).toBe('currency-exchange');
  });
});