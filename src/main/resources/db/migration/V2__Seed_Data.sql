-- V2__Seed_Data.sql
-- Insert seed data for development and testing

-- Insert Storage Locations (Bangalore, India)
INSERT INTO storage_locations (name, address, contact_person, contact_number, total_capacity, current_vehicle_count) VALUES
('MG Road Hub',             '12 Brigade Road, MG Road, Bangalore 560001',         'Rajesh Kumar',  '+91-98450-11111', 100, 0),
('Hebbal Branch',           '45 Bellary Road, Hebbal, Bangalore 560024',           'Priya Nair',    '+91-98450-22222', 60,  0),
('Electronic City Storage', '78 Phase 1 Main Road, Electronic City, Bangalore 560100', 'Suresh Babu',   '+91-98450-33333', 80,  0),
('Whitefield Facility',     '22 ITPL Main Road, Whitefield, Bangalore 560066',    'Kavitha Reddy', '+91-98450-44444', 50,  0);

-- Insert Employees
-- Password for all users: 'admin123' (BCrypt encoded)
INSERT INTO employees (name, email, password_hash, phone, position, department, join_date, status) VALUES
('Admin User',    'admin@wheelshift.com',         '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+91-98450-10001', 'System Administrator', 'IT',         '2024-01-01', 'ACTIVE'),
('Sales Manager', 'sales.manager@wheelshift.com', '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+91-98450-20002', 'Sales Manager',        'Sales',      '2024-01-15', 'ACTIVE'),
('Inspector Lead','inspector@wheelshift.com',     '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+91-98450-30003', 'Chief Inspector',      'Quality',    '2024-02-01', 'ACTIVE'),
('Finance Officer','finance@wheelshift.com',      '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+91-98450-40004', 'Finance Officer',      'Finance',    '2024-02-15', 'ACTIVE'),
('Store Manager', 'store@wheelshift.com',         '$2a$10$UEVvqDoY24DCOvv0bJCeLO5kdSYtgZSGQzbW9A9xBJ2GbN746I.4y', '+91-98450-50005', 'Store Manager',        'Operations', '2024-03-01', 'ACTIVE');

