-- V11__Add_File_Storage_System.sql
-- File storage system for managing images, PDFs, Excel, CSV and other file types
-- Acts as an internal S3-like storage with segregated file type management

CREATE TABLE file_metadata (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL COMMENT 'Unique identifier for the file (UUID)',
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original name of the uploaded file',
    stored_filename VARCHAR(255) NOT NULL COMMENT 'Name used to store file on disk',
    file_type VARCHAR(20) NOT NULL COMMENT 'IMAGE, PDF, EXCEL, CSV, DOCUMENT, OTHER',
    mime_type VARCHAR(128) NOT NULL COMMENT 'MIME type of the file',
    file_size BIGINT NOT NULL COMMENT 'Size in bytes',
    file_extension VARCHAR(10) NOT NULL COMMENT 'File extension without dot',
    storage_path VARCHAR(512) NOT NULL COMMENT 'Relative path where file is stored',
    public_url VARCHAR(512) NOT NULL COMMENT 'Public URL to access the file',
    upload_source VARCHAR(64) COMMENT 'Source of upload - e.g., car_images, documents',
    uploaded_by VARCHAR(128) COMMENT 'User who uploaded the file',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, DELETED, ARCHIVED',
    metadata_json TEXT COMMENT 'Additional metadata in JSON format',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Unique Constraints
    CONSTRAINT uk_file_id UNIQUE (file_id),
    CONSTRAINT uk_storage_path UNIQUE (storage_path),
    
    -- Indexes
    INDEX idx_file_type (file_type),
    INDEX idx_status (status),
    INDEX idx_upload_source (upload_source),
    INDEX idx_created_at (created_at),
    INDEX idx_file_id_status (file_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table for tracking file access logs (optional, for analytics)
CREATE TABLE file_access_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL,
    access_type VARCHAR(20) NOT NULL COMMENT 'VIEW, DOWNLOAD, DELETE',
    accessed_by VARCHAR(128),
    ip_address VARCHAR(45),
    user_agent TEXT,
    accessed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_file_access_file FOREIGN KEY (file_id) 
        REFERENCES file_metadata(file_id) ON DELETE CASCADE,
    
    -- Indexes
    INDEX idx_file_id (file_id),
    INDEX idx_access_type (access_type),
    INDEX idx_accessed_at (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;