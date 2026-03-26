-- V20__Split_Vehicle_Count_By_Type.sql
-- Replaces the single current_vehicle_count column with separate
-- current_car_count and current_motorcycle_count columns for precise tracking.
-- Counts are maintained at the application/service layer — no triggers.
--
-- Steps:
--   1. Add current_car_count and current_motorcycle_count columns
--   2. Back-fill from live car/motorcycle data
--   3. Drop old CHECK constraints that reference current_vehicle_count
--   4. Drop current_vehicle_count column
--   5. Add new CHECK constraints
--   6. Drop V19 triggers (no longer needed)

-- ============================================================
-- STEP 1: Add new columns
-- ============================================================
ALTER TABLE storage_locations
  ADD COLUMN current_car_count        INT NOT NULL DEFAULT 0 COMMENT 'Active (non-SOLD) cars at this location',
  ADD COLUMN current_motorcycle_count INT NOT NULL DEFAULT 0 COMMENT 'Active (non-SOLD) motorcycles at this location';

-- ============================================================
-- STEP 2: Back-fill counts from existing vehicle data
-- ============================================================
UPDATE storage_locations sl
SET
  current_car_count = (
    SELECT COUNT(*) FROM cars c
    WHERE c.storage_location_id = sl.id AND c.status <> 'SOLD'
  ),
  current_motorcycle_count = (
    SELECT COUNT(*) FROM motorcycles m
    WHERE m.storage_location_id = sl.id AND m.status <> 'SOLD'
  );

-- ============================================================
-- STEP 3: Drop old CHECK constraints that depend on the column
-- ============================================================
ALTER TABLE storage_locations
  DROP CHECK chk_capacity,
  DROP CHECK chk_vehicle_count_positive;

-- ============================================================
-- STEP 4: Drop the old column
-- ============================================================
ALTER TABLE storage_locations
  DROP COLUMN current_vehicle_count;

-- ============================================================
-- STEP 5: Add new CHECK constraints
-- ============================================================
ALTER TABLE storage_locations
  ADD CONSTRAINT chk_car_count_non_negative   CHECK (current_car_count >= 0),
  ADD CONSTRAINT chk_moto_count_non_negative  CHECK (current_motorcycle_count >= 0),
  ADD CONSTRAINT chk_capacity_total           CHECK ((current_car_count + current_motorcycle_count) <= total_capacity);

-- ============================================================
-- STEP 6: Drop V19 triggers (counts managed in application layer)
-- ============================================================
DROP TRIGGER IF EXISTS trg_cars_after_insert;
DROP TRIGGER IF EXISTS trg_cars_after_update;
DROP TRIGGER IF EXISTS trg_cars_after_delete;
DROP TRIGGER IF EXISTS trg_motorcycles_after_insert;
DROP TRIGGER IF EXISTS trg_motorcycles_after_update;
DROP TRIGGER IF EXISTS trg_motorcycles_after_delete;
