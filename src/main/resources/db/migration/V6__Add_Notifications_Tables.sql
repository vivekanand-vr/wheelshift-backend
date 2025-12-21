-- Notifications System Tables
-- V6__Add_Notifications_Tables.sql

-- notification_events: stores the raw events that trigger notifications
CREATE TABLE notification_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(64) NOT NULL COMMENT 'e.g., inquiry.assigned, reservation.created',
    entity_type VARCHAR(32) NOT NULL COMMENT 'e.g., INQUIRY, RESERVATION, SALE',
    entity_id BIGINT NOT NULL COMMENT 'FK to the entity (inquiry_id, reservation_id, etc.)',
    payload JSON NOT NULL COMMENT 'Event payload with all context data',
    severity ENUM('INFO', 'WARN', 'CRITICAL') DEFAULT 'INFO',
    occurred_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_occurred_at (occurred_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notification_jobs: individual notification delivery jobs
CREATE TABLE notification_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    recipient_type ENUM('EMPLOYEE', 'CLIENT', 'ROLE') NOT NULL,
    recipient_id BIGINT NULL COMMENT 'employee_id, client_id, or role_id',
    channel ENUM('EMAIL', 'SMS', 'WHATSAPP', 'PUSH', 'IN_APP', 'WEBHOOK') NOT NULL,
    status ENUM('PENDING', 'SCHEDULED', 'SENT', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    scheduled_for DATETIME NULL COMMENT 'For delayed/digest notifications',
    dedup_key VARCHAR(128) NULL COMMENT 'Prevents duplicate sends',
    retries INT DEFAULT 0,
    last_error VARCHAR(512) NULL,
    sent_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES notification_events(id) ON DELETE CASCADE,
    UNIQUE KEY uk_dedup (dedup_key),
    INDEX idx_status (status),
    INDEX idx_recipient (recipient_type, recipient_id),
    INDEX idx_scheduled (scheduled_for)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notification_deliveries: tracks actual delivery attempts and results
CREATE TABLE notification_deliveries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    provider VARCHAR(64) NULL COMMENT 'Email provider, SMS gateway, etc.',
    provider_message_id VARCHAR(128) NULL,
    status ENUM('SENT', 'DELIVERED', 'BOUNCED', 'FAILED') DEFAULT 'SENT',
    sent_at DATETIME NOT NULL,
    delivered_at DATETIME NULL,
    error_message VARCHAR(512) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES notification_jobs(id) ON DELETE CASCADE,
    INDEX idx_job (job_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notification_templates: reusable templates for notifications
CREATE TABLE notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL COMMENT 'Template identifier, e.g., inquiry_assigned',
    channel ENUM('EMAIL', 'SMS', 'WHATSAPP', 'PUSH', 'IN_APP', 'WEBHOOK') NOT NULL,
    locale VARCHAR(8) DEFAULT 'en',
    version INT NOT NULL DEFAULT 1,
    subject VARCHAR(255) NULL COMMENT 'For email/push',
    content TEXT NOT NULL COMMENT 'Template content with {{variables}}',
    variables JSON NULL COMMENT 'List of available variables',
    created_by_employee_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by_employee_id) REFERENCES employees(id) ON DELETE SET NULL,
    UNIQUE KEY uk_template (name, channel, locale, version),
    INDEX idx_name_channel (name, channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notification_preferences: user/role preferences for notifications
CREATE TABLE notification_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    principal_type ENUM('EMPLOYEE', 'CLIENT', 'ROLE', 'COMPANY') NOT NULL,
    principal_id BIGINT NULL COMMENT 'employee_id, client_id, role_id, or NULL for COMPANY',
    event_type VARCHAR(64) NOT NULL COMMENT 'e.g., inquiry.assigned, reservation.expiring.soon',
    channel ENUM('EMAIL', 'SMS', 'WHATSAPP', 'PUSH', 'IN_APP', 'WEBHOOK') NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    frequency ENUM('IMMEDIATE', 'DIGEST') DEFAULT 'IMMEDIATE',
    quiet_hours_start TIME NULL,
    quiet_hours_end TIME NULL,
    severity_threshold ENUM('INFO', 'WARN', 'CRITICAL') DEFAULT 'INFO',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_principal (principal_type, principal_id, event_type, channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notification_providers: external provider configurations (future use)
CREATE TABLE notification_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel ENUM('EMAIL', 'SMS', 'WHATSAPP', 'PUSH', 'WEBHOOK') NOT NULL,
    name VARCHAR(64) NOT NULL,
    config JSON NOT NULL COMMENT 'Provider-specific configuration',
    is_primary BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 1,
    enabled BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_channel (channel, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- notification_digests: aggregated digest notifications
CREATE TABLE notification_digests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_type ENUM('EMPLOYEE', 'CLIENT', 'ROLE') NOT NULL,
    recipient_id BIGINT NOT NULL,
    window_start DATETIME NOT NULL,
    window_end DATETIME NOT NULL,
    compiled_content TEXT NULL,
    channel ENUM('EMAIL', 'SMS', 'PUSH', 'IN_APP') NOT NULL,
    status ENUM('PENDING', 'SENT') DEFAULT 'PENDING',
    sent_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_recipient (recipient_type, recipient_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add default IN_APP channel preferences for all employees
INSERT INTO notification_preferences (principal_type, principal_id, event_type, channel, enabled, frequency)
SELECT 'EMPLOYEE', id, 'ALL', 'IN_APP', TRUE, 'IMMEDIATE'
FROM employees;

-- Add default templates for common events
INSERT INTO notification_templates (name, channel, locale, version, subject, content, variables)
VALUES 
    ('inquiry_assigned', 'IN_APP', 'en', 1, 'New Inquiry Assigned', 
     'A new inquiry (#{{inquiryId}}) has been assigned to you by {{assignedBy}}. Client: {{clientName}}', 
     '["inquiryId", "assignedBy", "clientName"]'),
    
    ('reservation_created', 'IN_APP', 'en', 1, 'New Reservation Created', 
     'Reservation #{{reservationId}} created for {{carModel}} by {{clientName}}. Pickup: {{pickupDate}}', 
     '["reservationId", "carModel", "clientName", "pickupDate"]'),
    
    ('reservation_expiring_soon', 'IN_APP', 'en', 1, 'Reservation Expiring Soon', 
     'Reservation #{{reservationId}} for {{clientName}} expires in {{hoursRemaining}} hours', 
     '["reservationId", "clientName", "hoursRemaining"]'),
    
    ('sale_recorded', 'IN_APP', 'en', 1, 'New Sale Recorded', 
     'Sale #{{saleId}} recorded for {{carModel}} to {{clientName}}. Amount: {{amount}}', 
     '["saleId", "carModel", "clientName", "amount"]'),
    
    ('inspection_due', 'IN_APP', 'en', 1, 'Car Inspection Due', 
     'Inspection due for {{carIdentifier}} ({{carModel}}) on {{dueDate}}', 
     '["carIdentifier", "carModel", "dueDate"]'),
    
    ('task_assigned', 'IN_APP', 'en', 1, 'New Task Assigned', 
     'Task "{{taskTitle}}" has been assigned to you by {{assignedBy}}. Due: {{dueDate}}', 
     '["taskTitle", "assignedBy", "dueDate"]'),
    
    ('task_overdue', 'IN_APP', 'en', 1, 'Task Overdue', 
     'Task "{{taskTitle}}" is now overdue. Original due date: {{dueDate}}', 
     '["taskTitle", "dueDate"]');
