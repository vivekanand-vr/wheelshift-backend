-- V18__Add_Storage_Location_Triggers.sql
-- Adds MySQL triggers to automatically maintain storage_locations.current_vehicle_count
-- when vehicles are inserted, updated (status/location changes), or deleted.
--
-- Logic:
--   "Active" means status is NOT 'SOLD'  (covers AVAILABLE, RESERVED, MAINTENANCE, etc.)
--   Inserting a non-SOLD vehicle at a location  → +1
--   Deleting a non-SOLD vehicle from a location → -1
--   Updating status/location handles all transition cases
--
-- Six triggers total: AFTER INSERT/UPDATE/DELETE on cars + motorcycles

-- ============================================================
-- DROP existing triggers (idempotent re-run safety)
-- ============================================================

DROP TRIGGER IF EXISTS trg_cars_after_insert;
DROP TRIGGER IF EXISTS trg_cars_after_update;
DROP TRIGGER IF EXISTS trg_cars_after_delete;
DROP TRIGGER IF EXISTS trg_motorcycles_after_insert;
DROP TRIGGER IF EXISTS trg_motorcycles_after_update;
DROP TRIGGER IF EXISTS trg_motorcycles_after_delete;

-- ============================================================
-- CARS — AFTER INSERT
-- Increment count when a non-SOLD car is assigned a location
-- ============================================================
CREATE TRIGGER trg_cars_after_insert
AFTER INSERT ON cars
FOR EACH ROW
  UPDATE storage_locations
  SET current_vehicle_count = GREATEST(0, current_vehicle_count + 1)
  WHERE id = NEW.storage_location_id
    AND NEW.storage_location_id IS NOT NULL
    AND NEW.status <> 'SOLD';

-- ============================================================
-- CARS — AFTER DELETE
-- Decrement count when a non-SOLD car is removed
-- ============================================================
CREATE TRIGGER trg_cars_after_delete
AFTER DELETE ON cars
FOR EACH ROW
  UPDATE storage_locations
  SET current_vehicle_count = GREATEST(0, current_vehicle_count - 1)
  WHERE id = OLD.storage_location_id
    AND OLD.storage_location_id IS NOT NULL
    AND OLD.status <> 'SOLD';

-- ============================================================
-- CARS — AFTER UPDATE
-- Single-statement approach using CASE WHEN so no BEGIN...END
-- is needed (avoids Flyway MySQL parser issues).
-- Handles all transition combinations atomically:
--   same/different location, any status change (SOLD ↔ active)
-- ============================================================
CREATE TRIGGER trg_cars_after_update
AFTER UPDATE ON cars
FOR EACH ROW
  UPDATE storage_locations
  SET current_vehicle_count = GREATEST(0,
    current_vehicle_count
    + CASE WHEN id = NEW.storage_location_id AND NEW.status <> 'SOLD' THEN  1 ELSE 0 END
    - CASE WHEN id = OLD.storage_location_id AND OLD.status <> 'SOLD' THEN  1 ELSE 0 END
  )
  WHERE id IN (OLD.storage_location_id, NEW.storage_location_id);

-- ============================================================
-- MOTORCYCLES — AFTER INSERT
-- ============================================================
CREATE TRIGGER trg_motorcycles_after_insert
AFTER INSERT ON motorcycles
FOR EACH ROW
  UPDATE storage_locations
  SET current_vehicle_count = GREATEST(0, current_vehicle_count + 1)
  WHERE id = NEW.storage_location_id
    AND NEW.storage_location_id IS NOT NULL
    AND NEW.status <> 'SOLD';

-- ============================================================
-- MOTORCYCLES — AFTER DELETE
-- ============================================================
CREATE TRIGGER trg_motorcycles_after_delete
AFTER DELETE ON motorcycles
FOR EACH ROW
  UPDATE storage_locations
  SET current_vehicle_count = GREATEST(0, current_vehicle_count - 1)
  WHERE id = OLD.storage_location_id
    AND OLD.storage_location_id IS NOT NULL
    AND OLD.status <> 'SOLD';

-- ============================================================
-- MOTORCYCLES — AFTER UPDATE
-- ============================================================
CREATE TRIGGER trg_motorcycles_after_update
AFTER UPDATE ON motorcycles
FOR EACH ROW
  UPDATE storage_locations
  SET current_vehicle_count = GREATEST(0,
    current_vehicle_count
    + CASE WHEN id = NEW.storage_location_id AND NEW.status <> 'SOLD' THEN  1 ELSE 0 END
    - CASE WHEN id = OLD.storage_location_id AND OLD.status <> 'SOLD' THEN  1 ELSE 0 END
  )
  WHERE id IN (OLD.storage_location_id, NEW.storage_location_id);
