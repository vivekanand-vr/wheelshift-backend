-- V22__Add_Audit_Logs_Table.sql
-- Creates a single audit_logs table to track all write operations across every domain.
-- Roles can filter by category (CAR, MOTORCYCLE, FINANCIAL_TRANSACTION, …) and
-- severity level (INFO, REGULAR, HIGH, CRITICAL).

CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    category        VARCHAR(64)  NOT NULL          COMMENT 'Domain area: CAR, MOTORCYCLE, FINANCIAL_TRANSACTION, SALE, RESERVATION, INQUIRY, CLIENT, EMPLOYEE, INSPECTION, STORAGE_LOCATION, TASK, SYSTEM',
    entity_id       BIGINT                         COMMENT 'Primary key of the affected entity (NULL for SYSTEM events)',
    action          VARCHAR(64)  NOT NULL          COMMENT 'Operation: CREATE, UPDATE, DELETE, MOVE, STATUS_CHANGE, etc.',
    level           VARCHAR(16)  NOT NULL
                    DEFAULT 'REGULAR'              COMMENT 'Severity: INFO, REGULAR, HIGH, CRITICAL',
    performed_by_id BIGINT                         COMMENT 'Employee who triggered the action; NULL = system',
    details         TEXT                           COMMENT 'Optional human-readable description of what changed',
    created_at      DATETIME     NOT NULL
                    DEFAULT CURRENT_TIMESTAMP      COMMENT 'Timestamp of the audit event',

    CONSTRAINT fk_audit_performed_by
        FOREIGN KEY (performed_by_id) REFERENCES employees(id)
        ON DELETE SET NULL,

    INDEX idx_audit_category     (category),
    INDEX idx_audit_level        (level),
    INDEX idx_audit_action       (action),
    INDEX idx_audit_entity_id    (entity_id),
    INDEX idx_audit_performed_by (performed_by_id),
    INDEX idx_audit_created_at   (created_at)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
