-- V1__Initial_Schema.sql
-- WheelShift Database Initial Schema
-- Creates all tables as per the database design document

-- Car Models Table
CREATE TABLE car_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    make VARCHAR(64) NOT NULL,
    model VARCHAR(64) NOT NULL,
    variant VARCHAR(64) NOT NULL,
    emission_norm VARCHAR(32),
    fuel_type VARCHAR(20),
    body_type VARCHAR(32),
    gears TINYINT,
    transmission_type VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_make_model_variant (make, model, variant),
    INDEX idx_fuel_type (fuel_type),
    INDEX idx_body_type (body_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Storage Locations Table
CREATE TABLE storage_locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    address VARCHAR(256) NOT NULL,
    contact_person VARCHAR(64),
    contact_number VARCHAR(32),
    total_capacity INT NOT NULL,
    current_vehicle_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity CHECK (current_vehicle_count <= total_capacity),
    CONSTRAINT chk_vehicle_count_positive CHECK (current_vehicle_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cars Table
CREATE TABLE cars (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_model_id BIGINT NOT NULL,
    vin_number CHAR(17) NOT NULL,
    registration_number VARCHAR(32),
    year SMALLINT NOT NULL,
    color VARCHAR(32),
    mileage_km INT,
    engine_cc INT,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    storage_location_id BIGINT,
    purchase_date DATE,
    purchase_price DECIMAL(12, 2),
    selling_price DECIMAL(12, 2),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_car_model FOREIGN KEY (car_model_id) REFERENCES car_models(id),
    CONSTRAINT fk_storage_location FOREIGN KEY (storage_location_id) REFERENCES storage_locations(id),
    CONSTRAINT uk_vin_number UNIQUE (vin_number),
    CONSTRAINT uk_registration_number UNIQUE (registration_number),
    CONSTRAINT chk_year CHECK (year BETWEEN 1980 AND 2100),
    INDEX idx_status_year (status, year),
    INDEX idx_storage_location (storage_location_id),
    INDEX idx_car_model (car_model_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Car Detailed Specs Table
CREATE TABLE car_detailed_specs (
    car_id BIGINT PRIMARY KEY,
    doors TINYINT,
    seats TINYINT,
    cargo_capacity_liters INT,
    acceleration_0_100 DECIMAL(5, 2),
    top_speed_kmh INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_car_specs FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Car Features Table
CREATE TABLE car_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    feature_name VARCHAR(64) NOT NULL,
    feature_value VARCHAR(128),
    CONSTRAINT fk_car_feature FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    CONSTRAINT uk_car_feature UNIQUE (car_id, feature_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Employees Table
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    position VARCHAR(64),
    department VARCHAR(64),
    join_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_employee_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Car Movements Table
CREATE TABLE car_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    from_location_id BIGINT,
    to_location_id BIGINT NOT NULL,
    moved_at DATETIME NOT NULL,
    moved_by_employee_id BIGINT,
    CONSTRAINT fk_movement_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_from_location FOREIGN KEY (from_location_id) REFERENCES storage_locations(id),
    CONSTRAINT fk_to_location FOREIGN KEY (to_location_id) REFERENCES storage_locations(id),
    CONSTRAINT fk_moved_by_employee FOREIGN KEY (moved_by_employee_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Car Inspections Table
CREATE TABLE car_inspections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    inspection_date DATE NOT NULL,
    inspector_name VARCHAR(64),
    exterior_condition TEXT,
    interior_condition TEXT,
    mechanical_condition TEXT,
    electrical_condition TEXT,
    accident_history TEXT,
    required_repairs TEXT,
    estimated_repair_cost DECIMAL(12, 2),
    inspection_pass BOOLEAN,
    report_url VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inspection_car FOREIGN KEY (car_id) REFERENCES cars(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Clients Table
CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    email VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    location VARCHAR(128),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    total_purchases INT DEFAULT 0,
    last_purchase DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_client_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Inquiries Table
CREATE TABLE inquiries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT,
    client_id BIGINT NOT NULL,
    assigned_employee_id BIGINT,
    inquiry_type VARCHAR(64),
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    response TEXT,
    response_date DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inquiry_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_inquiry_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_inquiry_employee FOREIGN KEY (assigned_employee_id) REFERENCES employees(id),
    INDEX idx_inquiry_status_employee (status, assigned_employee_id),
    INDEX idx_inquiry_car_client (car_id, client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Reservations Table
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    reservation_date DATETIME NOT NULL,
    expiry_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    deposit_amount DECIMAL(12, 2),
    deposit_paid BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_reservation_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT uk_reservation_car UNIQUE (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sales Table
CREATE TABLE sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    sale_date DATETIME NOT NULL,
    sale_price DECIMAL(12, 2) NOT NULL,
    commission_rate DECIMAL(5, 2),
    total_commission DECIMAL(12, 2),
    payment_method VARCHAR(20),
    documents_url VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_car FOREIGN KEY (car_id) REFERENCES cars(id),
    CONSTRAINT fk_sale_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_sale_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uk_sale_car UNIQUE (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Financial Transactions Table
CREATE TABLE financial_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    transaction_date DATETIME NOT NULL,
    description TEXT,
    vendor_name VARCHAR(128),
    receipt_url VARCHAR(512),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_car FOREIGN KEY (car_id) REFERENCES cars(id),
    INDEX idx_transaction_car_type_date (car_id, transaction_type, transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tasks Table
CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    assignee_id BIGINT,
    due_date DATETIME,
    priority VARCHAR(20),
    tags JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Events Table
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(64) NOT NULL,
    name VARCHAR(128),
    car_id BIGINT,
    title VARCHAR(128),
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_car FOREIGN KEY (car_id) REFERENCES cars(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
