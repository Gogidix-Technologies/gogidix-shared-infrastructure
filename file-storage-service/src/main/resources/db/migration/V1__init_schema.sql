-- Initial schema for file-storage-service

-- Table for storing file metadata
CREATE TABLE IF NOT EXISTS file_metadata (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(1000),
    content_type VARCHAR(255),
    size BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    storage_path VARCHAR(1000) NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP,
    is_directory BOOLEAN DEFAULT FALSE NOT NULL,
    parent_id UUID,
    FOREIGN KEY (parent_id) REFERENCES file_metadata(id)
);

-- Table for access control
CREATE TABLE IF NOT EXISTS file_access (
    id SERIAL PRIMARY KEY,
    file_id UUID NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    permission VARCHAR(50) NOT NULL, -- READ, WRITE, DELETE
    granted_at TIMESTAMP NOT NULL,
    granted_by VARCHAR(255),
    FOREIGN KEY (file_id) REFERENCES file_metadata(id) ON DELETE CASCADE,
    UNIQUE (file_id, user_id, permission)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_file_metadata_path ON file_metadata(path);
CREATE INDEX IF NOT EXISTS idx_file_metadata_name ON file_metadata(name);
CREATE INDEX IF NOT EXISTS idx_file_metadata_parent_id ON file_metadata(parent_id);
CREATE INDEX IF NOT EXISTS idx_file_access_file_id ON file_access(file_id);
CREATE INDEX IF NOT EXISTS idx_file_access_user_id ON file_access(user_id);