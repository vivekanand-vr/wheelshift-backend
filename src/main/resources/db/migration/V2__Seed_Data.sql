-- V2__Seed_Data.sql
-- Insert seed data for development and testing

-- Insert Car Models
INSERT INTO car_models (make, model, variant, emission_norm, fuel_type, body_type, gears, transmission_type) VALUES
('Toyota', 'Corolla', 'GLI', 'BS6', 'PETROL', 'Sedan', 6, 'MANUAL'),
('Honda', 'Civic', 'VTi Oriel', 'BS6', 'PETROL', 'Sedan', 6, 'AUTOMATIC'),
('Suzuki', 'Alto', 'VXR', 'BS6', 'PETROL', 'Hatchback', 5, 'MANUAL'),
('Hyundai', 'Elantra', 'GLS', 'BS6', 'PETROL', 'Sedan', 6, 'AUTOMATIC'),
('Tesla', 'Model 3', 'Standard Range', 'Zero Emission', 'ELECTRIC', 'Sedan', 1, 'AUTOMATIC'),
('BMW', 'X5', 'xDrive40i', 'BS6', 'PETROL', 'SUV', 8, 'AUTOMATIC'),
('Mercedes', 'C-Class', 'C200', 'BS6', 'DIESEL', 'Sedan', 9, 'AUTOMATIC'),
('Audi', 'A4', '40 TFSI', 'BS6', 'PETROL', 'Sedan', 7, 'DCT');

-- Insert Storage Locations
INSERT INTO storage_locations (name, address, contact_person, contact_number, total_capacity, current_vehicle_count) VALUES
('Main Warehouse', '123 Industrial Road, Karachi', 'Ahmed Ali', '+92-321-1234567', 100, 0),
('North Branch', '456 North Avenue, Lahore', 'Fatima Khan', '+92-322-9876543', 50, 0),
('South Storage', '789 South Street, Islamabad', 'Hassan Sheikh', '+92-333-5555555', 75, 0),
('East Facility', '321 East Boulevard, Faisalabad', 'Ayesha Malik', '+92-300-1111111', 40, 0);

-- Insert Employees
INSERT INTO employees (name, email, password_hash, phone, position, department, join_date, status) VALUES
('Admin User', 'admin@wheelshift.com', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', '+92-321-1000001', 'System Administrator', 'IT', '2024-01-01', 'ACTIVE'),
('Sales Manager', 'sales.manager@wheelshift.com', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', '+92-321-2000002', 'Sales Manager', 'Sales', '2024-01-15', 'ACTIVE'),
('Inspector Lead', 'inspector@wheelshift.com', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', '+92-321-3000003', 'Chief Inspector', 'Quality', '2024-02-01', 'ACTIVE'),
('Finance Officer', 'finance@wheelshift.com', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', '+92-321-4000004', 'Finance Officer', 'Finance', '2024-02-15', 'ACTIVE'),
('Store Manager', 'store@wheelshift.com', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', '+92-321-5000005', 'Store Manager', 'Operations', '2024-03-01', 'ACTIVE');

-- Insert Clients
INSERT INTO clients (name, email, phone, location, status, total_purchases, last_purchase) VALUES
('John Doe', 'john.doe@example.com', '+92-300-1111111', 'Karachi', 'ACTIVE', 2, '2024-06-15'),
('Jane Smith', 'jane.smith@example.com', '+92-300-2222222', 'Lahore', 'ACTIVE', 1, '2024-08-20'),
('Ali Ahmed', 'ali.ahmed@example.com', '+92-300-3333333', 'Islamabad', 'ACTIVE', 0, NULL),
('Sara Khan', 'sara.khan@example.com', '+92-300-4444444', 'Faisalabad', 'ACTIVE', 3, '2024-11-10'),
('Michael Brown', 'michael.brown@example.com', '+92-300-5555555', 'Karachi', 'ACTIVE', 0, NULL);

-- Note: Do not insert actual cars in seed data as they require proper business logic
-- Cars should be added through the application to ensure proper storage capacity management
