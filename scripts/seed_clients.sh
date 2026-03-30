#!/usr/bin/env bash
# =============================================================
# seed_clients.sh
# Seeds 6 clients together with:
#   • Sale records for every SOLD car and motorcycle
#   • SALE financial transactions keyed to actual negotiated price
#   • client.total_purchases / last_purchase counts
#
# Client–vehicle distribution is intentionally uneven so that the
# AI recommendations service has realistic behavioural variance.
#
# Purchase distribution:
#   Rahul Sharma     — 2 vehicles  (1 car,  1 motorcycle)
#   Priya Patel      — 1 vehicle   (1 car)
#   Vikram Singh     — 2 vehicles  (1 car,  1 motorcycle, loyal Honda buyer)
#   Anjali Gupta     — 2 vehicles  (2 cars, multi-car buyer)
#   Mohammed Rizwan  — 2 vehicles  (2 motorcycles, budget focused)
#   Pooja Nair       — 1 vehicle   (1 car)
#
# Usage (from repo root):
#   bash scripts/seed_clients.sh
#
# Prerequisites:
#   - Docker must be running
#   - Container 'wheelshift-mysql' must be up
#   - seed_cars.sh and seed_motorcycles.sh must have run first
#     (or use seed_all.sh which handles ordering)
# =============================================================

set -euo pipefail

# ── Config ────────────────────────────────────────────────────
CONTAINER="wheelshift-mysql"
DB="wheelshift_db"
USER="wheelshift_user"
PASS="wheelshift_pass_2025"

# ── Colour helpers ────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

info()    { echo -e "${CYAN}  ℹ  $*${RESET}"; }
success() { echo -e "${GREEN}  ✔  $*${RESET}"; }
warn()    { echo -e "${YELLOW}  ⚠  $*${RESET}"; }
error()   { echo -e "${RED}  ✖  $*${RESET}" >&2; }

run_sql() {
  docker exec -i "$CONTAINER" \
    mysql -u"$USER" -p"$PASS" --silent --skip-column-names "$DB" \
    -e "$1" 2>/dev/null
}

# ── Pre-flight checks ─────────────────────────────────────────
echo ""
echo -e "${BOLD}╔══════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}║      WheelShift — Seed Clients Script        ║${RESET}"
echo -e "${BOLD}╚══════════════════════════════════════════════╝${RESET}"
echo ""

info "Checking Docker container '$CONTAINER'..."
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  error "Container '$CONTAINER' is not running."
  error "Start it with:  docker compose -f docker-compose-dev.yml up -d mysql"
  exit 1
fi
success "Container is running."

info "Verifying database connection..."
if ! run_sql "SELECT 1" > /dev/null; then
  error "Cannot connect to MySQL inside container."
  exit 1
fi
success "Database connection OK."

# ── Prerequisites check ───────────────────────────────────────
info "Checking prerequisite: SOLD cars must exist..."
SOLD_CARS=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE 'C2026DV%' AND status='SOLD';")
if [ "$SOLD_CARS" -lt 6 ]; then
  error "Expected ≥6 SOLD cars but found $SOLD_CARS. Run seed_cars.sh first."
  exit 1
fi
success "$SOLD_CARS SOLD cars found."

info "Checking prerequisite: SOLD motorcycles must exist..."
SOLD_MOTOS=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE 'M2026DV%' AND status='SOLD';")
if [ "$SOLD_MOTOS" -lt 4 ]; then
  error "Expected ≥4 SOLD motorcycles but found $SOLD_MOTOS. Run seed_motorcycles.sh first."
  exit 1
fi
success "$SOLD_MOTOS SOLD motorcycles found."

# ── Existing rows check ────────────────────────────────────────
EXISTING=$(run_sql "SELECT COUNT(*) FROM clients WHERE email IN ('rahul.sharma@example.com','priya.patel@example.com','vikram.singh@example.com','anjali.gupta@example.com','mohammed.rizwan@example.com','pooja.nair@example.com');")
if [ "$EXISTING" -gt 0 ]; then
  warn "$EXISTING seed client(s) already present — INSERT IGNORE will skip duplicates."
fi

# ── Full seed SQL ─────────────────────────────────────────────
info "Seeding clients, sales, financial transactions, and client counters..."

docker exec -i "$CONTAINER" \
  mysql -u"$USER" -p"$PASS" "$DB" 2>/dev/null <<'SQL'

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 1 — Clients
-- ════════════════════════════════════════════════════════════════════════════
-- total_purchases / last_purchase are back-filled in Step 4; start at 0/NULL.
INSERT IGNORE INTO clients
  (name, email, phone, location, status, total_purchases, last_purchase)
VALUES
  ('Rahul Sharma',    'rahul.sharma@example.com',    '+91-98765-11111', 'Mumbai',    'ACTIVE', 0, NULL),
  ('Priya Patel',     'priya.patel@example.com',     '+91-98765-22222', 'Delhi',     'ACTIVE', 0, NULL),
  ('Vikram Singh',    'vikram.singh@example.com',    '+91-98765-33333', 'Bangalore', 'ACTIVE', 0, NULL),
  ('Anjali Gupta',    'anjali.gupta@example.com',    '+91-98765-44444', 'Chennai',   'ACTIVE', 0, NULL),
  ('Mohammed Rizwan', 'mohammed.rizwan@example.com', '+91-98765-55555', 'Hyderabad', 'ACTIVE', 0, NULL),
  ('Pooja Nair',      'pooja.nair@example.com',      '+91-98765-66666', 'Pune',      'ACTIVE', 0, NULL);

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 2 — Cache IDs to avoid correlated subqueries in every VALUES row
-- ════════════════════════════════════════════════════════════════════════════

-- Employee
SET @emp_sales = (SELECT id FROM employees WHERE email = 'sales.manager@wheelshift.com' LIMIT 1);

-- Clients
SET @cl_rahul    = (SELECT id FROM clients WHERE email = 'rahul.sharma@example.com'    LIMIT 1);
SET @cl_priya    = (SELECT id FROM clients WHERE email = 'priya.patel@example.com'     LIMIT 1);
SET @cl_vikram   = (SELECT id FROM clients WHERE email = 'vikram.singh@example.com'    LIMIT 1);
SET @cl_anjali   = (SELECT id FROM clients WHERE email = 'anjali.gupta@example.com'    LIMIT 1);
SET @cl_mohammed = (SELECT id FROM clients WHERE email = 'mohammed.rizwan@example.com' LIMIT 1);
SET @cl_pooja    = (SELECT id FROM clients WHERE email = 'pooja.nair@example.com'      LIMIT 1);

-- Cars (SOLD)
SET @car_007 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000007' LIMIT 1);  -- Renault Kwid Rxl 0.8
SET @car_015 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000015' LIMIT 1);  -- Datsun Go T
SET @car_020 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000020' LIMIT 1);  -- Premier Rio Ex
SET @car_033 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000033' LIMIT 1);  -- Honda Amaze V diesel
SET @car_037 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000037' LIMIT 1);  -- Datsun Go A
SET @car_049 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000049' LIMIT 1);  -- Maruti Dzire Zdi

-- Motorcycles (SOLD)
SET @moto_011 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000011' LIMIT 1); -- Hero Super Splendor
SET @moto_018 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000018' LIMIT 1); -- Bajaj CT 110
SET @moto_034 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000034' LIMIT 1); -- Honda Hornet 2.0
SET @moto_039 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000039' LIMIT 1); -- Bajaj Pulsar 220

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 3a — Sales for SOLD cars
--   sale_price  = negotiated price (3–5 % below selling_price)
--   commission  = 2 % mid-range / 2.5 % premium (≥ ₹6 L) / 1.5 % budget
--   payment_method: CASH · LOAN · CHEQUE · ONLINE
--   Idempotent: car_id has a DB-level UNIQUE constraint → INSERT IGNORE works
-- ════════════════════════════════════════════════════════════════════════════
INSERT IGNORE INTO sales
  (car_id, motorcycle_id, vehicle_type, client_id, employee_id,
   sale_date, sale_price, commission_rate, total_commission, payment_method)
VALUES
  -- Rahul — bought a Datsun Go (budget car, CASH)
  (@car_037, NULL, 'CAR', @cl_rahul, @emp_sales,
   '2022-10-05 14:30:00', 475000.00, 2.00,  9500.00, 'CASH'),

  -- Priya — bought a Renault Kwid (entry hatch, first-time buyer, CASH)
  (@car_007, NULL, 'CAR', @cl_priya, @emp_sales,
   '2022-11-20 16:00:00', 328000.00, 2.00,  6560.00, 'CASH'),

  -- Vikram — bought a Honda Amaze diesel (premium, LOAN)
  (@car_033, NULL, 'CAR', @cl_vikram, @emp_sales,
   '2023-02-28 10:30:00', 845000.00, 2.50, 21125.00, 'LOAN'),

  -- Anjali — first car: Datsun Go T (ONLINE payment)
  (@car_015, NULL, 'CAR', @cl_anjali, @emp_sales,
   '2022-10-15 09:30:00', 412000.00, 2.00,  8240.00, 'ONLINE'),

  -- Anjali — second car: Maruti Dzire Zdi (premium, CHEQUE)
  (@car_049, NULL, 'CAR', @cl_anjali, @emp_sales,
   '2023-01-25 15:00:00', 822000.00, 2.50, 20550.00, 'CHEQUE'),

  -- Pooja — bought a Premier Rio diesel (ONLINE)
  (@car_020, NULL, 'CAR', @cl_pooja, @emp_sales,
   '2022-09-18 10:00:00', 518000.00, 2.00, 10360.00, 'ONLINE');

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 3b — Sales for SOLD motorcycles
--   motorcycle_id has no DB-level UNIQUE → use NOT EXISTS guard via DUAL
-- ════════════════════════════════════════════════════════════════════════════

-- Rahul — Bajaj Pulsar 220 (second-hand sport, LOAN)
INSERT INTO sales
  (car_id, motorcycle_id, vehicle_type, client_id, employee_id,
   sale_date, sale_price, commission_rate, total_commission, payment_method)
SELECT NULL, @moto_039, 'MOTORCYCLE', @cl_rahul, @emp_sales,
       '2022-12-08 11:00:00', 126000.00, 2.00, 2520.00, 'LOAN'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sales WHERE motorcycle_id = @moto_039);

-- Vikram — Honda Hornet 2.0 (loyal Honda buyer, CASH)
INSERT INTO sales
  (car_id, motorcycle_id, vehicle_type, client_id, employee_id,
   sale_date, sale_price, commission_rate, total_commission, payment_method)
SELECT NULL, @moto_034, 'MOTORCYCLE', @cl_vikram, @emp_sales,
       '2023-02-15 14:00:00', 132000.00, 2.00, 2640.00, 'CASH'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sales WHERE motorcycle_id = @moto_034);

-- Mohammed — Hero Super Splendor (budget commuter, CASH)
INSERT INTO sales
  (car_id, motorcycle_id, vehicle_type, client_id, employee_id,
   sale_date, sale_price, commission_rate, total_commission, payment_method)
SELECT NULL, @moto_011, 'MOTORCYCLE', @cl_mohammed, @emp_sales,
       '2023-03-10 11:30:00', 69500.00, 1.50, 1042.50, 'CASH'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sales WHERE motorcycle_id = @moto_011);

-- Mohammed — Bajaj CT 110 (entry budget bike, CASH)
INSERT INTO sales
  (car_id, motorcycle_id, vehicle_type, client_id, employee_id,
   sale_date, sale_price, commission_rate, total_commission, payment_method)
SELECT NULL, @moto_018, 'MOTORCYCLE', @cl_mohammed, @emp_sales,
       '2023-01-20 12:00:00', 41500.00, 1.50, 622.50, 'CASH'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sales WHERE motorcycle_id = @moto_018);

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 3c — SALE financial transactions
--   amount = actual sale_price (not asking selling_price) — driven from sales
--   description includes client name for traceability
--   Idempotent: NOT EXISTS guard on (vehicle_id, SALE)
-- ════════════════════════════════════════════════════════════════════════════

-- Cars
INSERT INTO financial_transactions
  (car_id, motorcycle_id, vehicle_type, transaction_type,
   amount, transaction_date, description)
SELECT
  s.car_id, NULL, 'CAR', 'SALE',
  s.sale_price,
  s.sale_date,
  CONCAT('Vehicle sale - Car VIN: ', c.vin_number, ' | Buyer: ', cl.name)
FROM sales s
JOIN cars    c  ON c.id  = s.car_id
JOIN clients cl ON cl.id = s.client_id
WHERE c.vin_number LIKE 'C2026DV%'
  AND NOT EXISTS (
    SELECT 1 FROM financial_transactions ft
    WHERE ft.car_id = s.car_id AND ft.transaction_type = 'SALE'
  );

-- Motorcycles
INSERT INTO financial_transactions
  (car_id, motorcycle_id, vehicle_type, transaction_type,
   amount, transaction_date, description)
SELECT
  NULL, s.motorcycle_id, 'MOTORCYCLE', 'SALE',
  s.sale_price,
  s.sale_date,
  CONCAT('Vehicle sale - Motorcycle VIN: ', m.vin_number, ' | Buyer: ', cl.name)
FROM sales s
JOIN motorcycles m  ON m.id  = s.motorcycle_id
JOIN clients     cl ON cl.id = s.client_id
WHERE m.vin_number LIKE 'M2026DV%'
  AND NOT EXISTS (
    SELECT 1 FROM financial_transactions ft
    WHERE ft.motorcycle_id = s.motorcycle_id AND ft.transaction_type = 'SALE'
  );

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 4 — Back-fill client counters from actual sales data
--   Derived directly from the sales table so the counts are always accurate
--   even if this script is re-run (idempotent UPDATE).
-- ════════════════════════════════════════════════════════════════════════════
UPDATE clients cl
SET
  total_purchases = (SELECT COUNT(*)           FROM sales s WHERE s.client_id = cl.id),
  last_purchase   = (SELECT DATE(MAX(s.sale_date)) FROM sales s WHERE s.client_id = cl.id)
WHERE cl.email IN (
  'rahul.sharma@example.com',
  'priya.patel@example.com',
  'vikram.singh@example.com',
  'anjali.gupta@example.com',
  'mohammed.rizwan@example.com',
  'pooja.nair@example.com'
);

SQL

# ── Report ────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  SEED REPORT — CLIENTS & SALES${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

TOTAL=$(run_sql "SELECT COUNT(*) FROM clients;")
ACTIVE=$(run_sql "SELECT COUNT(*) FROM clients WHERE status='ACTIVE';")
WITH_PURCHASES=$(run_sql "SELECT COUNT(*) FROM clients WHERE total_purchases > 0;")

printf "  %-28s %s\n" "Total clients in DB:"      "$TOTAL"
printf "  %-28s %s\n" "Active:"                    "$ACTIVE"
printf "  %-28s %s\n" "Have purchase history:"     "$WITH_PURCHASES"

echo ""
echo -e "${BOLD}  Client purchase summary:${RESET}"
run_sql "
  SELECT CONCAT(
    '  ', RPAD(name, 22, ' '),
    RPAD(location, 12, ' '),
    LPAD(total_purchases, 2, ' '), ' purchase(s)   last: ',
    IFNULL(last_purchase, 'n/a')
  )
  FROM clients
  ORDER BY total_purchases DESC, last_purchase DESC;
"

echo ""
echo -e "${BOLD}  Sales breakdown:${RESET}"
TOTAL_SALES=$(run_sql "SELECT COUNT(*) FROM sales;")
CAR_SALES=$(run_sql "SELECT COUNT(*) FROM sales WHERE vehicle_type='CAR';")
MOTO_SALES=$(run_sql "SELECT COUNT(*) FROM sales WHERE vehicle_type='MOTORCYCLE';")
printf "  %-28s %s\n" "Total sales:" "$TOTAL_SALES"
printf "  %-28s %s\n" "Car sales:"   "$CAR_SALES"
printf "  %-28s %s\n" "Moto sales:"  "$MOTO_SALES"

echo ""
echo -e "${BOLD}  SALE financial transactions:${RESET}"
run_sql "
  SELECT CONCAT(
    '  ', RPAD(ft.vehicle_type, 12, ' '),
    FORMAT(ft.amount, 2), ' INR  | ',
    IFNULL(c.vin_number, m.vin_number)
  )
  FROM financial_transactions ft
  LEFT JOIN cars        c ON c.id = ft.car_id
  LEFT JOIN motorcycles m ON m.id = ft.motorcycle_id
  WHERE ft.transaction_type = 'SALE'
  ORDER BY ft.vehicle_type, ft.transaction_date;
"

echo ""
success "All done — clients, sales, and financial transactions seeded successfully."
echo ""

