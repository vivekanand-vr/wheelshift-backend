-- V7__Assign_Employee_Roles.sql
-- Assign roles to existing seed employees for dashboard access and create super admin

-- Create Super Admin user
-- Password: 'admin123' (BCrypt encoded)
INSERT IGNORE INTO employees (id, name, email, password_hash, phone, position, department, join_date, status) VALUES
(100, 'Super Admin', 'superadmin@wheelshift.com', '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+92-321-9999999', 'Super Administrator', 'Management', '2024-01-01', 'ACTIVE');

-- Assign roles to employees
-- Employee IDs: 1=Admin User, 2=Sales Manager, 3=Inspector Lead, 4=Finance Officer, 5=Store Manager, 100=Super Admin
-- Role IDs: 1=SUPER_ADMIN, 2=ADMIN, 3=SALES, 4=INSPECTOR, 5=FINANCE, 6=STORE_MANAGER

-- Use INSERT IGNORE to avoid duplicate key errors on re-run
INSERT IGNORE INTO employee_roles (employee_id, role_id) VALUES
(100, 1),  -- Super Admin gets SUPER_ADMIN role
(1, 2),    -- Admin User gets ADMIN role
(2, 3),    -- Sales Manager gets SALES role
(3, 4),    -- Inspector Lead gets INSPECTOR role
(4, 5),    -- Finance Officer gets FINANCE role
(5, 6);    -- Store Manager gets STORE_MANAGER role
