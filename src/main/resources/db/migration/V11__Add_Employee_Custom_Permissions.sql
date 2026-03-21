-- V12__Add_Employee_Custom_Permissions.sql
-- Add support for custom permissions assigned directly to employees

CREATE TABLE employee_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_by BIGINT COMMENT 'ID of admin who granted this permission',
    reason VARCHAR(255) COMMENT 'Reason for granting this custom permission',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_emp_perm_employee FOREIGN KEY (employee_id) 
        REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_perm_permission FOREIGN KEY (permission_id) 
        REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_perm_granted_by FOREIGN KEY (granted_by) 
        REFERENCES employees(id) ON DELETE SET NULL,
    
    -- Unique Constraint (prevent duplicate assignments)
    CONSTRAINT uk_employee_permission UNIQUE (employee_id, permission_id),
    
    -- Indexes
    INDEX idx_employee_id (employee_id),
    INDEX idx_permission_id (permission_id),
    INDEX idx_granted_by (granted_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Custom permissions assigned directly to employees (independent of roles)';

-- Add comment to explain the purpose
ALTER TABLE employee_permissions 
COMMENT = 'Allows super admins to assign custom permissions to individual employees beyond their role permissions';
