-- Rollback script for V9__Add_Motorcycle_Tables.sql
-- Fixed: Removed 'IF EXISTS' from ALTER statements for compatibility with older MySQL versions or specific strict modes.
-- INSTRUCTIONS: Run these commands against your database. 
-- NOTE: If a command fails with "Error 1091: Can't DROP 'xyz'; check that column/key exists", IT IS SAFE TO IGNORE. 
--       It simply means that part of the migration hadn't applied yet.

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Drop foreign keys
-- If you get an error that the key doesn't exist, move to the next one.
ALTER TABLE events DROP FOREIGN KEY fk_event_motorcycle;
ALTER TABLE financial_transactions DROP FOREIGN KEY fk_transaction_motorcycle;
ALTER TABLE sales DROP FOREIGN KEY fk_sale_motorcycle;
ALTER TABLE reservations DROP FOREIGN KEY fk_reservation_motorcycle;
ALTER TABLE inquiries DROP FOREIGN KEY fk_inquiry_motorcycle;

-- 2. Drop columns
-- If you get an error that the column doesn't exist, move to the next one.
ALTER TABLE events DROP COLUMN vehicle_type;
ALTER TABLE events DROP COLUMN motorcycle_id;

ALTER TABLE financial_transactions DROP COLUMN vehicle_type;
ALTER TABLE financial_transactions DROP COLUMN motorcycle_id;

ALTER TABLE sales DROP COLUMN vehicle_type;
ALTER TABLE sales DROP COLUMN motorcycle_id;

ALTER TABLE reservations DROP COLUMN vehicle_type;
ALTER TABLE reservations DROP COLUMN motorcycle_id;

ALTER TABLE inquiries DROP COLUMN vehicle_type;
ALTER TABLE inquiries DROP COLUMN motorcycle_id;

-- 3. Drop new tables
-- These commands use IF EXISTS, so they should run without error even if tables are missing.
DROP TABLE IF EXISTS motorcycle_inspections;
DROP TABLE IF EXISTS motorcycle_detailed_specs;
DROP TABLE IF EXISTS motorcycles;
DROP TABLE IF EXISTS motorcycle_models;

SET FOREIGN_KEY_CHECKS = 1;
