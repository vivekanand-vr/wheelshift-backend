-- V5__Seed_RBAC_Data.sql
-- Seed data for RBAC: roles, permissions, and role-permission mappings

-- Insert system roles
INSERT INTO roles (name, description, is_system) VALUES
('SUPER_ADMIN', 'Full system control; manage admins, global settings, feature flags, audit logs', TRUE),
('ADMIN', 'Manage employees, inventory, inquiries, reservations, sales within data scopes', TRUE),
('SALES', 'Manage inquiries, reservations, sales within scope/assignment', TRUE),
('INSPECTOR', 'Create and update inspections', TRUE),
('FINANCE', 'View/record transactions, financial reports within scope', TRUE),
('STORE_MANAGER', 'Manage storage locations and movements within locations', TRUE);

-- Insert permissions

-- Car permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('cars', 'read', 'cars:read', 'View car information'),
('cars', 'write', 'cars:write', 'Create and update cars'),
('cars', 'delete', 'cars:delete', 'Delete cars'),
('cars', 'manage', 'cars:manage', 'Full car management');

-- Car Model permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('car_models', 'read', 'car_models:read', 'View car models'),
('car_models', 'write', 'car_models:write', 'Create and update car models'),
('car_models', 'delete', 'car_models:delete', 'Delete car models');

-- Inspection permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('inspections', 'read', 'inspections:read', 'View inspections'),
('inspections', 'write', 'inspections:write', 'Create and update inspections'),
('inspections', 'delete', 'inspections:delete', 'Delete inspections');

-- Client permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('clients', 'read', 'clients:read', 'View client information'),
('clients', 'write', 'clients:write', 'Create and update clients'),
('clients', 'delete', 'clients:delete', 'Delete clients');

-- Inquiry permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('inquiries', 'read', 'inquiries:read', 'View inquiries'),
('inquiries', 'write', 'inquiries:write', 'Create and update inquiries'),
('inquiries', 'assign', 'inquiries:assign', 'Assign inquiries to employees'),
('inquiries', 'delete', 'inquiries:delete', 'Delete inquiries');

-- Reservation permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('reservations', 'read', 'reservations:read', 'View reservations'),
('reservations', 'write', 'reservations:write', 'Create and update reservations'),
('reservations', 'convert', 'reservations:convert', 'Convert reservations to sales'),
('reservations', 'delete', 'reservations:delete', 'Delete reservations');

-- Sale permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('sales', 'read', 'sales:read', 'View sales'),
('sales', 'write', 'sales:write', 'Create and update sales'),
('sales', 'delete', 'sales:delete', 'Delete sales');

-- Transaction permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('transactions', 'read', 'transactions:read', 'View financial transactions'),
('transactions', 'write', 'transactions:write', 'Create and update transactions'),
('transactions', 'delete', 'transactions:delete', 'Delete transactions');

-- Storage Location permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('storage', 'read', 'storage:read', 'View storage locations'),
('storage', 'write', 'storage:write', 'Create and update storage locations'),
('storage', 'manage', 'storage:manage', 'Full storage management');

-- Employee permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('employees', 'read', 'employees:read', 'View employee information'),
('employees', 'write', 'employees:write', 'Create and update employees'),
('employees', 'manage', 'employees:manage', 'Full employee management including role assignment'),
('employees', 'delete', 'employees:delete', 'Delete employees');

-- Task permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('tasks', 'read', 'tasks:read', 'View tasks'),
('tasks', 'write', 'tasks:write', 'Create and update tasks'),
('tasks', 'assign', 'tasks:assign', 'Assign tasks to employees'),
('tasks', 'delete', 'tasks:delete', 'Delete tasks');

-- Event permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('events', 'read', 'events:read', 'View events'),
('events', 'write', 'events:write', 'Create and update events'),
('events', 'delete', 'events:delete', 'Delete events');

-- Report permissions
INSERT INTO permissions (resource, action, name, description) VALUES
('reports', 'view', 'reports:view', 'View reports and dashboards'),
('reports', 'export', 'reports:export', 'Export reports');

-- Settings permissions (Super Admin only)
INSERT INTO permissions (resource, action, name, description) VALUES
('settings', 'read', 'settings:read', 'View system settings'),
('settings', 'manage', 'settings:manage', 'Manage global settings, feature flags, security policies');

-- ACL permissions (Super Admin only)
INSERT INTO permissions (resource, action, name, description) VALUES
('acl', 'read', 'acl:read', 'View ACL entries'),
('acl', 'manage', 'acl:manage', 'Manage resource-level ACLs');

-- Audit permissions (Super Admin only)
INSERT INTO permissions (resource, action, name, description) VALUES
('audit', 'read', 'audit:read', 'View audit logs');

-- Assign permissions to SUPER_ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN';

-- Assign permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
AND p.name IN (
    'cars:read', 'cars:write', 'cars:delete', 'cars:manage',
    'car_models:read', 'car_models:write',
    'inspections:read', 'inspections:write',
    'clients:read', 'clients:write',
    'inquiries:read', 'inquiries:write', 'inquiries:assign', 'inquiries:delete',
    'reservations:read', 'reservations:write', 'reservations:convert', 'reservations:delete',
    'sales:read', 'sales:write', 'sales:delete',
    'transactions:read', 'transactions:write',
    'storage:read', 'storage:write', 'storage:manage',
    'employees:read', 'employees:write', 'employees:manage',
    'tasks:read', 'tasks:write', 'tasks:assign', 'tasks:delete',
    'events:read', 'events:write', 'events:delete',
    'reports:view', 'reports:export',
    'settings:read',
    'acl:read'
);

-- Assign permissions to SALES role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SALES'
AND p.name IN (
    'cars:read',
    'clients:read', 'clients:write',
    'inquiries:read', 'inquiries:write',
    'reservations:read', 'reservations:write', 'reservations:convert',
    'sales:read', 'sales:write',
    'tasks:read', 'tasks:write',
    'events:read', 'events:write',
    'reports:view'
);

-- Assign permissions to INSPECTOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'INSPECTOR'
AND p.name IN (
    'cars:read',
    'inspections:read', 'inspections:write',
    'tasks:read', 'tasks:write',
    'events:read'
);

-- Assign permissions to FINANCE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'FINANCE'
AND p.name IN (
    'cars:read',
    'sales:read',
    'transactions:read', 'transactions:write',
    'reports:view', 'reports:export',
    'events:read'
);

-- Assign permissions to STORE_MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'STORE_MANAGER'
AND p.name IN (
    'cars:read', 'cars:write',
    'storage:read', 'storage:write', 'storage:manage',
    'tasks:read', 'tasks:write',
    'events:read', 'events:write',
    'reports:view'
);
