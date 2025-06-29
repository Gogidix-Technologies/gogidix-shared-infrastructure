-- Initial schema for geo-location-service

-- Table for storing geo locations
CREATE TABLE IF NOT EXISTS simple_location (
    id SERIAL PRIMARY KEY,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    name VARCHAR(255),
    formatted_address VARCHAR(500)
);

-- Table for tracking geo requests (for caching and auditing)
CREATE TABLE IF NOT EXISTS geo_request (
    id SERIAL PRIMARY KEY,
    request_type VARCHAR(50) NOT NULL,
    query VARCHAR(1000),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    result VARCHAR(1000),
    provider VARCHAR(50) NOT NULL,
    request_time TIMESTAMP NOT NULL,
    response_time_ms INTEGER
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_geo_request_query ON geo_request(query, request_type, request_time);
CREATE INDEX IF NOT EXISTS idx_geo_request_coords ON geo_request(latitude, longitude, request_type, request_time);
CREATE INDEX IF NOT EXISTS idx_geo_request_provider ON geo_request(provider, request_time);
