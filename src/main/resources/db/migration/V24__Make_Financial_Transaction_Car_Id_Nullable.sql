-- V24__Make_Financial_Transaction_Car_Id_Nullable.sql
-- Establishes proper dual-vehicle support in financial_transactions so that
-- PURCHASE / SALE / REPAIR / FEE transactions can be linked to either a Car
-- OR a Motorcycle — never both, never neither.
--
-- Root cause
-- ----------
--   V1  created car_id as NOT NULL (car-only system at the time).
--   V9  added motorcycle_id + vehicle_type columns for motorcycle support
--       but did NOT relax car_id, making motorcycle-only transactions
--       impossible at the database level even though the JPA entity already
--       maps Car as nullable (no @Column(nullable = false)).
--
-- Changes
-- -------
--   1. Make car_id nullable → motorcycle-only transactions can be persisted.
--   2. Back-fill any legacy NULL vehicle_type rows to 'CAR', then mark the
--      column NOT NULL so the application layer must always supply it.
--      (The JPA entity already defaults to VehicleType.CAR via @Builder.Default.)
--   3. Add CHECK chk_ft_one_vehicle — enforces exactly one vehicle reference
--      per row (car XOR motorcycle).  The service layer is the primary guard;
--      this constraint is the database-level safety net.

-- ── 1. Allow motorcycle-only transactions ────────────────────────────────────
ALTER TABLE financial_transactions
  MODIFY COLUMN car_id BIGINT NULL
    COMMENT 'FK → cars.id; NULL when the transaction belongs to a motorcycle';

-- ── 2a. Back-fill any NULL vehicle_type rows from the V9 migration window ───
UPDATE financial_transactions
   SET vehicle_type = 'CAR'
 WHERE vehicle_type IS NULL;

-- ── 2b. Enforce NOT NULL — application layer must always supply vehicle_type ─
ALTER TABLE financial_transactions
  MODIFY COLUMN vehicle_type VARCHAR(20) NOT NULL
    COMMENT 'CAR or MOTORCYCLE — must be set explicitly; never left NULL';

-- Note: a database-level XOR CHECK constraint cannot be added here because
-- motorcycle_id is referenced by FK fk_transaction_motorcycle which has a
-- referential action (MySQL error 3823 prevents CHECK on such columns).
-- The XOR rule — exactly one of (car_id, motorcycle_id) must be non-null —
-- is enforced exclusively in the service layer.
