-- V9__Add_Motorcycle_Tables.sql
-- Motorcycle inventory management system for 2-wheelers (bikes, scooters, motorcycles)

-- =====================================================
-- Table: motorcycle_models
-- Purpose: Catalog of motorcycle makes, models, and variants
-- =====================================================
CREATE TABLE motorcycle_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    make VARCHAR(100) NOT NULL COMMENT 'Manufacturer name (e.g., Honda, Yamaha, Royal Enfield)',
    model VARCHAR(100) NOT NULL COMMENT 'Model name (e.g., Activa, R15, Classic 350)',
    variant VARCHAR(100) COMMENT 'Specific variant (e.g., Standard, Deluxe, BS6)',
    year INT NOT NULL COMMENT 'Model year',
    engine_capacity INT COMMENT 'Engine displacement in CC',
    fuel_type VARCHAR(20) NOT NULL DEFAULT 'PETROL' COMMENT 'PETROL, ELECTRIC, HYBRID',
    transmission_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL, AUTOMATIC, SEMI_AUTOMATIC, CVT',
    vehicle_type VARCHAR(50) NOT NULL DEFAULT 'MOTORCYCLE' COMMENT 'MOTORCYCLE, SCOOTER, SPORT_BIKE, CRUISER, OFF_ROAD',
    seating_capacity INT DEFAULT 2 COMMENT 'Number of seats',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Whether this model is still in production',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    UNIQUE KEY uk_model_variant_year (make, model, variant, year),
    INDEX idx_make (make),
    INDEX idx_model (model),
    INDEX idx_year (year),
    INDEX idx_vehicle_type (vehicle_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Motorcycle models catalog';

-- =====================================================
-- Table: motorcycles
-- Purpose: Main inventory table for all motorcycles
-- =====================================================
CREATE TABLE motorcycles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Basic Information
    vin_number VARCHAR(17) NOT NULL UNIQUE COMMENT 'Vehicle Identification Number (17 chars)',
    registration_number VARCHAR(20) UNIQUE COMMENT 'License plate number',
    engine_number VARCHAR(50) COMMENT 'Engine serial number',
    chassis_number VARCHAR(50) COMMENT 'Chassis/Frame number',
    
    -- Model Reference
    motorcycle_model_id BIGINT NOT NULL COMMENT 'Reference to motorcycle_models table',
    
    -- Physical Attributes
    color VARCHAR(50) COMMENT 'Primary color',
    mileage_km INT DEFAULT 0 COMMENT 'Odometer reading in kilometers',
    manufacture_year INT NOT NULL COMMENT 'Year of manufacture',
    registration_date DATE COMMENT 'First registration date',
    
    -- Status & Location
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE, RESERVED, SOLD, MAINTENANCE, INSPECTION_PENDING',
    storage_location_id BIGINT COMMENT 'Current storage facility',
    
    -- Pricing
    purchase_price DECIMAL(12,2) NOT NULL COMMENT 'Purchase/acquisition cost',
    purchase_date DATE NOT NULL COMMENT 'Date of purchase',
    selling_price DECIMAL(12,2) COMMENT 'Target selling price',
    minimum_price DECIMAL(12,2) COMMENT 'Minimum acceptable price',
    
    -- Additional Details
    previous_owners INT DEFAULT 1 COMMENT 'Number of previous owners',
    insurance_expiry_date DATE COMMENT 'Insurance validity end date',
    pollution_certificate_expiry DATE COMMENT 'Pollution certificate expiry',
    is_financed BOOLEAN DEFAULT FALSE COMMENT 'Whether motorcycle has existing finance',
    is_accidental BOOLEAN DEFAULT FALSE COMMENT 'Whether motorcycle has accident history',
    description TEXT COMMENT 'Additional notes and description',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_motorcycle_model 
        FOREIGN KEY (motorcycle_model_id) REFERENCES motorcycle_models(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_motorcycle_storage 
        FOREIGN KEY (storage_location_id) REFERENCES storage_locations(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    
    -- Indexes
    INDEX idx_status (status),
    INDEX idx_model (motorcycle_model_id),
    INDEX idx_storage (storage_location_id),
    INDEX idx_purchase_date (purchase_date),
    INDEX idx_price_range (selling_price),
    INDEX idx_mileage (mileage_km),
    INDEX idx_manufacture_year (manufacture_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Motorcycle inventory';

-- =====================================================
-- Table: motorcycle_detailed_specs
-- Purpose: Extended specifications and features for motorcycles
-- =====================================================
CREATE TABLE motorcycle_detailed_specs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    motorcycle_id BIGINT NOT NULL UNIQUE COMMENT 'One-to-one relationship with motorcycles',
    
    -- Engine Specifications
    engine_type VARCHAR(50) COMMENT 'Engine configuration (Single Cylinder, Twin Cylinder, etc.)',
    max_power_bhp DECIMAL(6,2) COMMENT 'Maximum power output in BHP',
    max_torque_nm DECIMAL(6,2) COMMENT 'Maximum torque in Newton-meters',
    cooling_system VARCHAR(30) COMMENT 'AIR_COOLED, LIQUID_COOLED, OIL_COOLED',
    fuel_tank_capacity DECIMAL(5,2) COMMENT 'Fuel tank capacity in liters',
    claimed_mileage_kmpl DECIMAL(5,2) COMMENT 'Manufacturer claimed mileage',
    
    -- Dimensions
    length_mm INT COMMENT 'Overall length in millimeters',
    width_mm INT COMMENT 'Overall width in millimeters',
    height_mm INT COMMENT 'Overall height in millimeters',
    wheelbase_mm INT COMMENT 'Wheelbase in millimeters',
    ground_clearance_mm INT COMMENT 'Ground clearance in millimeters',
    kerb_weight_kg INT COMMENT 'Kerb weight in kilograms',
    
    -- Braking System
    front_brake_type VARCHAR(50) COMMENT 'Front brake type (Disc, Drum)',
    rear_brake_type VARCHAR(50) COMMENT 'Rear brake type (Disc, Drum)',
    abs_available BOOLEAN DEFAULT FALSE COMMENT 'Anti-lock Braking System',
    
    -- Suspension
    front_suspension VARCHAR(100) COMMENT 'Front suspension type',
    rear_suspension VARCHAR(100) COMMENT 'Rear suspension type',
    
    -- Tires
    front_tyre_size VARCHAR(30) COMMENT 'Front tyre specification',
    rear_tyre_size VARCHAR(30) COMMENT 'Rear tyre specification',
    
    -- Features
    has_electric_start BOOLEAN DEFAULT TRUE COMMENT 'Electric starter available',
    has_kick_start BOOLEAN DEFAULT FALSE COMMENT 'Kick starter available',
    has_digital_console BOOLEAN DEFAULT FALSE COMMENT 'Digital instrument cluster',
    has_usb_charging BOOLEAN DEFAULT FALSE COMMENT 'USB charging port',
    has_led_lights BOOLEAN DEFAULT FALSE COMMENT 'LED headlamps/taillamps',
    additional_features TEXT COMMENT 'Other features (JSON or comma-separated)',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_motorcycle_specs 
        FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Detailed specifications for motorcycles';

-- =====================================================
-- Table: motorcycle_inspections
-- Purpose: Inspection and condition reports for motorcycles
-- =====================================================
CREATE TABLE motorcycle_inspections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    motorcycle_id BIGINT NOT NULL COMMENT 'Reference to motorcycles table',
    
    -- Inspection Details
    inspection_date DATE NOT NULL COMMENT 'Date of inspection',
    inspector_id BIGINT COMMENT 'Employee who performed inspection',
    overall_condition VARCHAR(20) NOT NULL COMMENT 'EXCELLENT, GOOD, FAIR, POOR',
    
    -- Condition Assessment
    engine_condition VARCHAR(20) COMMENT 'Engine health status',
    transmission_condition VARCHAR(20) COMMENT 'Gearbox condition',
    suspension_condition VARCHAR(20) COMMENT 'Suspension system condition',
    brake_condition VARCHAR(20) COMMENT 'Braking system condition',
    tyre_condition VARCHAR(20) COMMENT 'Tyre health',
    electrical_condition VARCHAR(20) COMMENT 'Electrical system condition',
    body_condition VARCHAR(20) COMMENT 'Body/paint/cosmetic condition',
    
    -- Issues & Repairs
    has_accident_history BOOLEAN DEFAULT FALSE COMMENT 'Accident damage detected',
    requires_repair BOOLEAN DEFAULT FALSE COMMENT 'Whether repairs needed',
    estimated_repair_cost DECIMAL(10,2) DEFAULT 0 COMMENT 'Cost estimate for repairs',
    repair_notes TEXT COMMENT 'Details of required repairs',
    
    -- Documents
    inspection_report_url VARCHAR(500) COMMENT 'URL to uploaded inspection report PDF',
    passed BOOLEAN DEFAULT TRUE COMMENT 'Whether inspection passed',
    
    -- Notes
    notes TEXT COMMENT 'Additional inspector notes',
    
    -- Audit fields
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_motorcycle_inspection_motorcycle 
        FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    
    CONSTRAINT fk_motorcycle_inspection_inspector 
        FOREIGN KEY (inspector_id) REFERENCES employees(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    
    -- Indexes
    INDEX idx_motorcycle (motorcycle_id),
    INDEX idx_inspector (inspector_id),
    INDEX idx_date (inspection_date),
    INDEX idx_condition (overall_condition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='Motorcycle inspection records';

-- =====================================================
-- Modify existing tables to support motorcycles
-- =====================================================

-- Add motorcycle support to inquiries
ALTER TABLE inquiries 
ADD COLUMN motorcycle_id BIGINT COMMENT 'Reference to motorcycles (if inquiry is for motorcycle)' AFTER car_id,
ADD COLUMN vehicle_type VARCHAR(20) DEFAULT 'CAR' COMMENT 'CAR or MOTORCYCLE' AFTER motorcycle_id,
ADD CONSTRAINT fk_inquiry_motorcycle 
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,
ADD INDEX idx_motorcycle (motorcycle_id),
ADD INDEX idx_vehicle_type (vehicle_type);

-- Note: Vehicle type validation (either car_id or motorcycle_id, not both) is handled at application level

-- Add motorcycle support to reservations
ALTER TABLE reservations 
ADD COLUMN motorcycle_id BIGINT COMMENT 'Reference to motorcycles (if reservation is for motorcycle)' AFTER car_id,
ADD COLUMN vehicle_type VARCHAR(20) DEFAULT 'CAR' COMMENT 'CAR or MOTORCYCLE' AFTER motorcycle_id,
ADD CONSTRAINT fk_reservation_motorcycle 
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
ADD INDEX idx_motorcycle (motorcycle_id),
ADD INDEX idx_vehicle_type (vehicle_type);

-- Note: Vehicle type validation (either car_id or motorcycle_id, not both) is handled at application level

-- Add motorcycle support to sales
ALTER TABLE sales 
ADD COLUMN motorcycle_id BIGINT COMMENT 'Reference to motorcycles (if sale is for motorcycle)' AFTER car_id,
ADD COLUMN vehicle_type VARCHAR(20) DEFAULT 'CAR' COMMENT 'CAR or MOTORCYCLE' AFTER motorcycle_id,
ADD CONSTRAINT fk_sale_motorcycle 
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
ADD INDEX idx_motorcycle (motorcycle_id),
ADD INDEX idx_vehicle_type (vehicle_type);

-- Note: Vehicle type validation (either car_id or motorcycle_id, not both) is handled at application level

-- Add motorcycle support to financial_transactions
ALTER TABLE financial_transactions 
ADD COLUMN motorcycle_id BIGINT COMMENT 'Reference to motorcycles (if transaction is for motorcycle)' AFTER car_id,
ADD COLUMN vehicle_type VARCHAR(20) DEFAULT 'CAR' COMMENT 'CAR or MOTORCYCLE' AFTER motorcycle_id,
ADD CONSTRAINT fk_transaction_motorcycle 
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
ADD INDEX idx_motorcycle (motorcycle_id),
ADD INDEX idx_vehicle_type (vehicle_type);

-- Note: Vehicle type validation (either car_id or motorcycle_id, not both) is handled at application level

-- Add motorcycle support to events (optional vehicle linking)
ALTER TABLE events 
ADD COLUMN motorcycle_id BIGINT COMMENT 'Reference to motorcycles (if event is for motorcycle)' AFTER car_id,
ADD COLUMN vehicle_type VARCHAR(20) COMMENT 'CAR or MOTORCYCLE' AFTER motorcycle_id,
ADD CONSTRAINT fk_event_motorcycle 
    FOREIGN KEY (motorcycle_id) REFERENCES motorcycles(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
ADD INDEX idx_motorcycle (motorcycle_id);

-- Note: Events can have no vehicle (car_id and motorcycle_id both NULL), so no check constraint needed
