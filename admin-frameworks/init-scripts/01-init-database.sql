-- Admin Framework Database Initialization
-- This script sets up the basic database structure for the admin framework

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS admin_framework;
CREATE SCHEMA IF NOT EXISTS audit;
CREATE SCHEMA IF NOT EXISTS exports;
CREATE SCHEMA IF NOT EXISTS regions;

-- Set default schema
SET search_path TO admin_framework, public;

-- Create sequences
CREATE SEQUENCE IF NOT EXISTS admin_framework.global_id_seq START 1;

-- Create basic tables that might be needed by the framework

-- Regions table (if not created by JPA)
CREATE TABLE IF NOT EXISTS admin_framework.regions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    region_type VARCHAR(50),
    parent_id BIGINT REFERENCES admin_framework.regions(id),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Export templates table
CREATE TABLE IF NOT EXISTS admin_framework.export_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    format VARCHAR(50) NOT NULL,
    template_config JSONB,
    field_mappings JSONB,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit entries table
CREATE TABLE IF NOT EXISTS audit.audit_entries (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255),
    action VARCHAR(50) NOT NULL,
    user_id VARCHAR(255),
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Export history table
CREATE TABLE IF NOT EXISTS exports.export_history (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT REFERENCES admin_framework.export_templates(id),
    format VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    file_path VARCHAR(500),
    file_size BIGINT,
    row_count INTEGER,
    user_id VARCHAR(255),
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- WebSocket metrics table (for rate limiting tracking)
CREATE TABLE IF NOT EXISTS admin_framework.websocket_metrics (
    id BIGSERIAL PRIMARY KEY,
    connection_id VARCHAR(255),
    user_id VARCHAR(255),
    ip_address INET,
    event_type VARCHAR(50),
    message_count INTEGER DEFAULT 0,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_regions_parent_id ON admin_framework.regions(parent_id);
CREATE INDEX IF NOT EXISTS idx_regions_code ON admin_framework.regions(code);
CREATE INDEX IF NOT EXISTS idx_regions_status ON admin_framework.regions(status);

CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit.audit_entries(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit.audit_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit.audit_entries(created_at);

CREATE INDEX IF NOT EXISTS idx_export_history_user ON exports.export_history(user_id);
CREATE INDEX IF NOT EXISTS idx_export_history_status ON exports.export_history(status);
CREATE INDEX IF NOT EXISTS idx_export_history_started ON exports.export_history(started_at);

CREATE INDEX IF NOT EXISTS idx_websocket_metrics_user ON admin_framework.websocket_metrics(user_id);
CREATE INDEX IF NOT EXISTS idx_websocket_metrics_ip ON admin_framework.websocket_metrics(ip_address);
CREATE INDEX IF NOT EXISTS idx_websocket_metrics_activity ON admin_framework.websocket_metrics(last_activity);

-- Sample data for development
INSERT INTO admin_framework.regions (name, code, description, region_type, status) 
VALUES 
    ('Global', 'GLOBAL', 'Global region covering all territories', 'GLOBAL', 'ACTIVE'),
    ('Europe', 'EU', 'European region', 'CONTINENT', 'ACTIVE'),
    ('Africa', 'AF', 'African region', 'CONTINENT', 'ACTIVE'),
    ('Western Europe', 'WE', 'Western European countries', 'REGION', 'ACTIVE'),
    ('Eastern Europe', 'EE', 'Eastern European countries', 'REGION', 'ACTIVE'),
    ('North Africa', 'NAF', 'North African countries', 'REGION', 'ACTIVE'),
    ('Sub-Saharan Africa', 'SSA', 'Sub-Saharan African countries', 'REGION', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- Update parent relationships
UPDATE admin_framework.regions SET parent_id = (SELECT id FROM admin_framework.regions WHERE code = 'EU') WHERE code IN ('WE', 'EE');
UPDATE admin_framework.regions SET parent_id = (SELECT id FROM admin_framework.regions WHERE code = 'AF') WHERE code IN ('NAF', 'SSA');

-- Sample export templates
INSERT INTO admin_framework.export_templates (name, format, template_config, field_mappings, created_by)
VALUES 
    ('Standard User Report', 'CSV', '{"delimiter":",","header":true}', '{"id":"User ID","name":"Full Name","email":"Email Address","status":"Status"}', 'system'),
    ('Region Summary', 'EXCEL', '{"sheetName":"Regions","autoSizeColumns":true}', '{"code":"Region Code","name":"Region Name","type":"Region Type","status":"Status"}', 'system'),
    ('Audit Trail Report', 'PDF', '{"orientation":"LANDSCAPE","title":"Audit Trail Report"}', '{"action":"Action","user":"User","timestamp":"Date/Time","details":"Details"}', 'system')
ON CONFLICT DO NOTHING;

-- Create triggers for updated_at columns
CREATE OR REPLACE FUNCTION admin_framework.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_regions_updated_at BEFORE UPDATE ON admin_framework.regions FOR EACH ROW EXECUTE FUNCTION admin_framework.update_updated_at_column();
CREATE TRIGGER update_export_templates_updated_at BEFORE UPDATE ON admin_framework.export_templates FOR EACH ROW EXECUTE FUNCTION admin_framework.update_updated_at_column();

-- Grants for application user (if needed)
-- GRANT USAGE ON SCHEMA admin_framework TO app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA admin_framework TO app_user;
-- GRANT USAGE ON ALL SEQUENCES IN SCHEMA admin_framework TO app_user;

COMMIT;