-- V8__Fix_Employee_Roles_And_Add_SuperAdmin.sql
-- Fix employee role assignments and create super admin user

-- Create Super Admin user if not exists
-- Password: 'admin123' (BCrypt encoded)
INSERT IGNORE INTO employees (id, name, email, password_hash, phone, position, department, join_date, status) VALUES
(100, 'Super Admin', 'superadmin@wheelshift.com', '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+92-321-9999999', 'Super Administrator', 'Management', '2024-01-01', 'ACTIVE');

-- Assign Super Admin role
-- Role IDs: 1=SUPER_ADMIN
INSERT IGNORE INTO employee_roles (employee_id, role_id) VALUES
(100, 1);  -- Super Admin gets SUPER_ADMIN role
