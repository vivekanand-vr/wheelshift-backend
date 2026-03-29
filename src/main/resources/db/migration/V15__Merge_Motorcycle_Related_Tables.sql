-- V16__Merge_Motorcycle_Related_Tables.sql
-- Merges motorcycle_detailed_specs into motorcycles table
-- Similar to V15 for cars, simplifies motorcycle structure

-- ============================================================================
-- STEP 1: Add detailed specs columns to motorcycles table
-- ============================================================================

ALTER TABLE motorcycles
-- Engine Specifications
ADD COLUMN engine_type VARCHAR(50) COMMENT 'Engine type description',
ADD COLUMN max_power_bhp DECIMAL(6, 2) COMMENT 'Maximum power in BHP',
ADD COLUMN max_torque_nm DECIMAL(6, 2) COMMENT 'Maximum torque in Nm',
ADD COLUMN cooling_system VARCHAR(30) COMMENT 'Cooling system type (AIR_COOLED, LIQUID_COOLED, etc.)',
ADD COLUMN fuel_tank_capacity DECIMAL(5, 2) COMMENT 'Fuel tank capacity in liters',
ADD COLUMN claimed_mileage_kmpl DECIMAL(5, 2) COMMENT 'Claimed mileage in km/l',

-- Dimensions
ADD COLUMN length_mm INT COMMENT 'Length in millimeters',
ADD COLUMN width_mm INT COMMENT 'Width in millimeters',
ADD COLUMN height_mm INT COMMENT 'Height in millimeters',
ADD COLUMN wheelbase_mm INT COMMENT 'Wheelbase in millimeters',
ADD COLUMN ground_clearance_mm INT COMMENT 'Ground clearance in millimeters',
ADD COLUMN kerb_weight_kg INT COMMENT 'Kerb weight in kilograms',

-- Braking System
ADD COLUMN front_brake_type VARCHAR(50) COMMENT 'Front brake type',
ADD COLUMN rear_brake_type VARCHAR(50) COMMENT 'Rear brake type',
ADD COLUMN abs_available BOOLEAN DEFAULT FALSE COMMENT 'ABS available',

-- Suspension
ADD COLUMN front_suspension VARCHAR(100) COMMENT 'Front suspension type',
ADD COLUMN rear_suspension VARCHAR(100) COMMENT 'Rear suspension type',

-- Tires
ADD COLUMN front_tyre_size VARCHAR(30) COMMENT 'Front tyre size',
ADD COLUMN rear_tyre_size VARCHAR(30) COMMENT 'Rear tyre size',

-- Features
ADD COLUMN has_electric_start BOOLEAN DEFAULT TRUE COMMENT 'Has electric start',
ADD COLUMN has_kick_start BOOLEAN DEFAULT FALSE COMMENT 'Has kick start',
ADD COLUMN has_digital_console BOOLEAN DEFAULT FALSE COMMENT 'Has digital console',
ADD COLUMN has_usb_charging BOOLEAN DEFAULT FALSE COMMENT 'Has USB charging',
ADD COLUMN has_led_lights BOOLEAN DEFAULT FALSE COMMENT 'Has LED lights',
ADD COLUMN additional_features TEXT COMMENT 'Additional features description';

-- ============================================================================
-- STEP 2: Migrate data from motorcycle_detailed_specs to motorcycles
-- ============================================================================

UPDATE motorcycles m
INNER JOIN motorcycle_detailed_specs mds ON m.id = mds.motorcycle_id
SET 
    m.engine_type = mds.engine_type,
    m.max_power_bhp = mds.max_power_bhp,
    m.max_torque_nm = mds.max_torque_nm,
    m.cooling_system = mds.cooling_system,
    m.fuel_tank_capacity = mds.fuel_tank_capacity,
    m.claimed_mileage_kmpl = mds.claimed_mileage_kmpl,
    m.length_mm = mds.length_mm,
    m.width_mm = mds.width_mm,
    m.height_mm = mds.height_mm,
    m.wheelbase_mm = mds.wheelbase_mm,
    m.ground_clearance_mm = mds.ground_clearance_mm,
    m.kerb_weight_kg = mds.kerb_weight_kg,
    m.front_brake_type = mds.front_brake_type,
    m.rear_brake_type = mds.rear_brake_type,
    m.abs_available = mds.abs_available,
    m.front_suspension = mds.front_suspension,
    m.rear_suspension = mds.rear_suspension,
    m.front_tyre_size = mds.front_tyre_size,
    m.rear_tyre_size = mds.rear_tyre_size,
    m.has_electric_start = mds.has_electric_start,
    m.has_kick_start = mds.has_kick_start,
    m.has_digital_console = mds.has_digital_console,
    m.has_usb_charging = mds.has_usb_charging,
    m.has_led_lights = mds.has_led_lights,
    m.additional_features = mds.additional_features;

-- ============================================================================
-- STEP 3: Drop motorcycle_detailed_specs table
-- ============================================================================

DROP TABLE IF EXISTS motorcycle_detailed_specs;

-- ============================================================================
-- STEP 4: Add constraints for new columns
-- ============================================================================

ALTER TABLE motorcycles
ADD CONSTRAINT chk_moto_length_positive CHECK (length_mm IS NULL OR length_mm >= 0),
ADD CONSTRAINT chk_moto_width_positive CHECK (width_mm IS NULL OR width_mm >= 0),
ADD CONSTRAINT chk_moto_height_positive CHECK (height_mm IS NULL OR height_mm >= 0),
ADD CONSTRAINT chk_moto_wheelbase_positive CHECK (wheelbase_mm IS NULL OR wheelbase_mm >= 0),
ADD CONSTRAINT chk_moto_ground_clearance_positive CHECK (ground_clearance_mm IS NULL OR ground_clearance_mm >= 0),
ADD CONSTRAINT chk_moto_kerb_weight_positive CHECK (kerb_weight_kg IS NULL OR kerb_weight_kg >= 0);
