-- V4__Add_RBAC_Tables.sql
-- Migration to add Role-Based Access Control (RBAC) tables

-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE,
    description VARCHAR(256),
    is_system BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    CONSTRAINT uk_role_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    name VARCHAR(96) NOT NULL UNIQUE,
    description VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    CONSTRAINT uk_permission_name UNIQUE (name),
    INDEX idx_permission_resource (resource)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create role_permissions junction table
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create employee_roles junction table
CREATE TABLE IF NOT EXISTS employee_roles (
    employee_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (employee_id, role_id),
    CONSTRAINT fk_employee_roles_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_employee_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT uk_employee_role UNIQUE (employee_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create employee_data_scopes table
CREATE TABLE IF NOT EXISTS employee_data_scopes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    scope_type ENUM('LOCATION', 'DEPARTMENT', 'ASSIGNMENT') NOT NULL,
    scope_value VARCHAR(128) NOT NULL,
    effect ENUM('INCLUDE', 'EXCLUDE') NOT NULL DEFAULT 'INCLUDE',
    description VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    CONSTRAINT fk_employee_data_scopes_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    INDEX idx_employee_scope (employee_id, scope_type, scope_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create resource_acl table
CREATE TABLE IF NOT EXISTS resource_acl (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_type ENUM('CAR', 'CLIENT', 'INQUIRY', 'RESERVATION', 'SALE', 'TRANSACTION') NOT NULL,
    resource_id BIGINT NOT NULL,
    subject_type ENUM('ROLE', 'EMPLOYEE') NOT NULL,
    subject_id BIGINT NOT NULL,
    access ENUM('READ', 'WRITE', 'ADMIN') NOT NULL,
    reason VARCHAR(512),
    granted_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    CONSTRAINT uk_resource_acl UNIQUE (resource_type, resource_id, subject_type, subject_id, access),
    INDEX idx_resource_acl_resource (resource_type, resource_id),
    INDEX idx_resource_acl_subject (subject_type, subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
