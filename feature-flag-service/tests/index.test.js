const request = require('supertest');
const app = require('../index');

describe('Feature Flag Service', () => {
  test('GET /health should return OK', async () => {
    const response = await request(app)
      .get('/health')
      .expect(200);
    
    expect(response.body.status).toBe('OK');
    expect(response.body.service).toBe('feature-flags');
  });

  test('GET /flags should return all feature flags', async () => {
    const response = await request(app)
      .get('/flags')
      .expect(200);
    
    expect(response.body).toHaveProperty('flags');
    expect(response.body).toHaveProperty('count');
    expect(response.body.flags).toHaveProperty('social-media-integration');
  });

  test('GET /flags/:flagName should return specific flag', async () => {
    const response = await request(app)
      .get('/flags/social-media-integration')
      .expect(200);
    
    expect(response.body.flagName).toBe('social-media-integration');
    expect(response.body).toHaveProperty('enabled');
    expect(response.body).toHaveProperty('percentage');
  });

  test('POST /flags/:flagName/check should check flag for user', async () => {
    const response = await request(app)
      .post('/flags/social-media-integration/check')
      .send({
        userId: 'test-user-123'
      })
      .expect(200);
    
    expect(response.body).toHaveProperty('enabled');
    expect(response.body).toHaveProperty('flagName');
    expect(response.body.userId).toBe('test-user-123');
  });

  test('POST /flags/check should return bulk flag check', async () => {
    const response = await request(app)
      .post('/flags/check')
      .send({
        userId: 'test-user-123'
      })
      .expect(200);
    
    expect(response.body).toHaveProperty('flags');
    expect(response.body.userId).toBe('test-user-123');
    expect(response.body.flags).toHaveProperty('social-media-integration');
  });

  test('PUT /flags/:flagName should update flag', async () => {
    const response = await request(app)
      .put('/flags/social-media-integration')
      .send({
        enabled: true,
        percentage: 80,
        description: 'Updated description'
      })
      .expect(200);
    
    expect(response.body.enabled).toBe(true);
    expect(response.body.percentage).toBe(80);
    expect(response.body.description).toBe('Updated description');
  });

  test('GET /metrics should return service metrics', async () => {
    const response = await request(app)
      .get('/metrics')
      .expect(200);
    
    expect(response.body).toHaveProperty('service');
    expect(response.body).toHaveProperty('flagCount');
    expect(response.body.service).toBe('feature-flags');
  });

  test('Should return 404 for non-existent flag', async () => {
    const response = await request(app)
      .get('/flags/non-existent-flag')
      .expect(404);
    
    expect(response.body.error).toContain('Flag not found');
  });
});