-- V25__Make_Sale_Car_Id_Nullable.sql
-- Enables motorcycle-only sale records by relaxing the NOT NULL constraint
-- on sales.car_id.  Mirrors V24 which did the same for financial_transactions.
--
-- Root cause
-- ----------
--   When the sales table was first created it was car-only, so car_id was
--   made NOT NULL.  Motorcycle sale functionality was added later (V9+) but
--   the car_id constraint was never relaxed, making motorcycle-only sale rows
--   impossible at the database level.
--
-- Changes
-- -------
--   1. Make car_id nullable  → motorcycle-only sales can be persisted.
--   2. Back-fill any NULL vehicle_type rows to 'CAR', then enforce NOT NULL
--      so every row always carries an explicit vehicle discriminator.
--
-- Note: a database-level XOR CHECK (exactly one of car/motorcycle non-null)
-- cannot be added because fk_sale_motorcycle uses ON UPDATE CASCADE, which
-- triggers MySQL error 3823 for CHECK constraints referencing that column.
-- The XOR rule is enforced in the service layer (SaleServiceImpl).

-- ── 1. Allow motorcycle-only sale records ────────────────────────────────────
ALTER TABLE sales
  MODIFY COLUMN car_id BIGINT NULL
    COMMENT 'FK → cars.id; NULL when the sale is for a motorcycle';

-- ── 2a. Back-fill any NULL vehicle_type rows ─────────────────────────────────
UPDATE sales
   SET vehicle_type = 'CAR'
 WHERE vehicle_type IS NULL;

-- ── 2b. Enforce NOT NULL — application layer must always supply vehicle_type ──
ALTER TABLE sales
  MODIFY COLUMN vehicle_type VARCHAR(20) NOT NULL
    COMMENT 'CAR or MOTORCYCLE — must be set explicitly; never left NULL';
