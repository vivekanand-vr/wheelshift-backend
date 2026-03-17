-- V15__Merge_Car_Related_Tables.sql
-- Simplifies car structure by merging car_detailed_specs and car_features into cars table
-- - Adds detailed specs columns directly to cars table
-- - Converts car_features to JSON column for flexible key-value storage
-- - Migrates existing data and drops redundant tables

-- ============================================================================
-- STEP 1: Add new columns to cars table
-- ============================================================================

-- Add detailed specs columns (from car_detailed_specs)
ALTER TABLE cars
ADD COLUMN doors INT COMMENT 'Number of doors',
ADD COLUMN seats INT COMMENT 'Seating capacity',
ADD COLUMN cargo_capacity_liters INT COMMENT 'Cargo capacity in liters',
ADD COLUMN acceleration_0_100 DECIMAL(5, 2) COMMENT '0-100 km/h acceleration time in seconds',
ADD COLUMN top_speed_kmh INT COMMENT 'Maximum speed in km/h';

-- Add features JSON column (replaces car_features table)
ALTER TABLE cars
ADD COLUMN features JSON COMMENT 'Car features as JSON key-value pairs';

-- ============================================================================
-- STEP 2: Migrate data from car_detailed_specs to cars
-- ============================================================================

UPDATE cars c
INNER JOIN car_detailed_specs cds ON c.id = cds.car_id
SET 
    c.doors = cds.doors,
    c.seats = cds.seats,
    c.cargo_capacity_liters = cds.cargo_capacity_liters,
    c.acceleration_0_100 = cds.acceleration_0_100,
    c.top_speed_kmh = cds.top_speed_kmh;

-- ============================================================================
-- STEP 3: Migrate data from car_features to cars (as JSON)
-- ============================================================================

-- Build JSON object from car_features and update cars table
UPDATE cars c
SET c.features = (
    SELECT JSON_OBJECTAGG(cf.feature_name, cf.feature_value)
    FROM car_features cf
    WHERE cf.car_id = c.id
)
WHERE EXISTS (
    SELECT 1 FROM car_features cf WHERE cf.car_id = c.id
);

-- ============================================================================
-- STEP 4: Drop redundant tables
-- ============================================================================

-- Drop car_detailed_specs table (data migrated to cars)
DROP TABLE IF EXISTS car_detailed_specs;

-- Drop car_features table (data migrated to cars as JSON)
DROP TABLE IF EXISTS car_features;

-- ============================================================================
-- STEP 5: Add constraints for new columns
-- ============================================================================

ALTER TABLE cars
ADD CONSTRAINT chk_doors_positive CHECK (doors IS NULL OR doors >= 0),
ADD CONSTRAINT chk_seats_positive CHECK (seats IS NULL OR seats >= 0),
ADD CONSTRAINT chk_cargo_positive CHECK (cargo_capacity_liters IS NULL OR cargo_capacity_liters >= 0),
ADD CONSTRAINT chk_top_speed_positive CHECK (top_speed_kmh IS NULL OR top_speed_kmh >= 0);
