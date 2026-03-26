-- V21__Add_Vehicle_Description.sql
-- Adds a description column to the cars table.
-- Resizes the motorcycles description column from TEXT to VARCHAR(600)
-- to match the 600-character limit enforced at the application layer.

ALTER TABLE cars
  ADD COLUMN description VARCHAR(600) NULL;

ALTER TABLE motorcycles
  MODIFY COLUMN description VARCHAR(600) NULL;
