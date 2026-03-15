-- rollback_V15.sql
-- Rollback script to restore car_detailed_specs and car_features tables
-- Use this if you need to revert V15__Merge_Car_Related_Tables.sql

-- ============================================================================
-- STEP 1: Recreate car_detailed_specs table
-- ============================================================================

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

-- ============================================================================
-- STEP 2: Recreate car_features table
-- ============================================================================

CREATE TABLE car_features (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id BIGINT NOT NULL,
    feature_name VARCHAR(64) NOT NULL,
    feature_value VARCHAR(128),
    CONSTRAINT fk_car_feature FOREIGN KEY (car_id) REFERENCES cars(id) ON DELETE CASCADE,
    CONSTRAINT uk_car_feature UNIQUE (car_id, feature_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- STEP 3: Migrate data from cars back to car_detailed_specs
-- ============================================================================

INSERT INTO car_detailed_specs (car_id, doors, seats, cargo_capacity_liters, acceleration_0_100, top_speed_kmh)
SELECT id, doors, seats, cargo_capacity_liters, acceleration_0_100, top_speed_kmh
FROM cars
WHERE doors IS NOT NULL 
   OR seats IS NOT NULL 
   OR cargo_capacity_liters IS NOT NULL 
   OR acceleration_0_100 IS NOT NULL 
   OR top_speed_kmh IS NOT NULL;

-- ============================================================================
-- STEP 4: Migrate data from cars features JSON back to car_features table
-- ============================================================================

-- Note: This requires manual handling or a stored procedure due to JSON parsing complexity
-- Example for MySQL 5.7+:
INSERT INTO car_features (car_id, feature_name, feature_value)
SELECT 
    c.id,
    j.feature_key,
    JSON_UNQUOTE(JSON_EXTRACT(c.features, CONCAT('$.', j.feature_key)))
FROM cars c
CROSS JOIN JSON_TABLE(
    CONCAT('[', 
        REPLACE(REPLACE(JSON_KEYS(c.features), '[', ''), ']', ''),
    ']'),
    '$[*]' COLUMNS (feature_key VARCHAR(64) PATH '$')
) j
WHERE c.features IS NOT NULL;

-- ============================================================================
-- STEP 5: Drop columns from cars table
-- ============================================================================

ALTER TABLE cars
DROP CONSTRAINT IF EXISTS chk_doors_positive,
DROP CONSTRAINT IF EXISTS chk_seats_positive,
DROP CONSTRAINT IF EXISTS chk_cargo_positive,
DROP CONSTRAINT IF EXISTS chk_top_speed_positive;

ALTER TABLE cars
DROP COLUMN IF EXISTS doors,
DROP COLUMN IF EXISTS seats,
DROP COLUMN IF EXISTS cargo_capacity_liters,
DROP COLUMN IF EXISTS acceleration_0_100,
DROP COLUMN IF EXISTS top_speed_kmh,
DROP COLUMN IF EXISTS features;
