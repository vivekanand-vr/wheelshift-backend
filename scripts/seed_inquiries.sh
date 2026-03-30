#!/usr/bin/env bash
# =============================================================
# seed_inquiries.sh
# Seeds 15 client inquiries for UNSOLD cars and motorcycles.
#
# Distribution is intentionally asymmetric to provide useful
# variance for the AI recommendations service:
#
#   Anjali Gupta     — 5 inquiries  (research-heavy, multi-car comparisons)
#   Priya Patel      — 4 inquiries  (actively browsing upgrade options)
#   Vikram Singh     — 3 inquiries  (premium & sport interest)
#   Rahul Sharma     — 1 inquiry    (minimal; already purchased)
#   Mohammed Rizwan  — 1 inquiry    (budget focused; already satisfied)
#   Pooja Nair       — 1 inquiry    (conservative browser)
#
# Status spread:
#   OPEN        — 7   (no response yet, some unassigned)
#   IN_PROGRESS — 3   (assigned to employee, being handled)
#   RESPONDED   — 2   (employee replied, client not closed)
#   CLOSED      — 3   (conversation ended, with or without sale)
#
# Idempotent: each inquiry is guarded by NOT EXISTS on
# (client_id, car_id|motorcycle_id, inquiry_type).
#
# Usage (from repo root):
#   bash scripts/seed_inquiries.sh
#
# Prerequisites:
#   - Docker must be running
#   - Container 'wheelshift-mysql' must be up
#   - seed_cars.sh, seed_motorcycles.sh, seed_clients.sh must have run first
#     (or use seed_all.sh which handles ordering)
# =============================================================

set -euo pipefail

# ── Config ────────────────────────────────────────────────────
CONTAINER="wheelshift-mysql"
DB="wheelshift_db"
USER="wheelshift_user"
PASS="wheelshift_pass_2025"
VIN_CAR_PREFIX="C2026DV"
VIN_MOTO_PREFIX="M2026DV"

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
echo -e "${BOLD}║    WheelShift — Seed Inquiries Script        ║${RESET}"
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
info "Checking prerequisite: clients must exist..."
CLIENT_COUNT=$(run_sql "SELECT COUNT(*) FROM clients WHERE email IN ('rahul.sharma@example.com','priya.patel@example.com','vikram.singh@example.com','anjali.gupta@example.com','mohammed.rizwan@example.com','pooja.nair@example.com');")
if [ "$CLIENT_COUNT" -lt 6 ]; then
  error "Expected 6 seed clients but found $CLIENT_COUNT. Run seed_clients.sh first."
  exit 1
fi
success "$CLIENT_COUNT seed clients found."

info "Checking prerequisite: unsold vehicles must exist..."
UNSOLD_CARS=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE '${VIN_CAR_PREFIX}%' AND status <> 'SOLD';")
UNSOLD_MOTOS=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE '${VIN_MOTO_PREFIX}%' AND status <> 'SOLD';")
if [ "$UNSOLD_CARS" -lt 10 ] || [ "$UNSOLD_MOTOS" -lt 4 ]; then
  error "Not enough unsold vehicles (cars=$UNSOLD_CARS, motorcycles=$UNSOLD_MOTOS). Run seed_cars.sh and seed_motorcycles.sh first."
  exit 1
fi
success "$UNSOLD_CARS unsold cars and $UNSOLD_MOTOS unsold motorcycles found."

# ── Existing rows check ────────────────────────────────────────
EXISTING=$(run_sql "SELECT COUNT(*) FROM inquiries i JOIN clients cl ON cl.id=i.client_id WHERE cl.email LIKE '%@example.com';")
if [ "$EXISTING" -gt 0 ]; then
  warn "$EXISTING seed inquiry/inquiries already present — NOT EXISTS guards will skip duplicates."
fi

# ── Full seed SQL ─────────────────────────────────────────────
info "Seeding 15 inquiries..."

docker exec -i "$CONTAINER" \
  mysql -u"$USER" -p"$PASS" "$DB" 2>/dev/null <<'SQL'

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 1 — Cache IDs
-- ════════════════════════════════════════════════════════════════════════════

-- Clients
SET @cl_anjali   = (SELECT id FROM clients WHERE email = 'anjali.gupta@example.com'    LIMIT 1);
SET @cl_priya    = (SELECT id FROM clients WHERE email = 'priya.patel@example.com'     LIMIT 1);
SET @cl_vikram   = (SELECT id FROM clients WHERE email = 'vikram.singh@example.com'    LIMIT 1);
SET @cl_rahul    = (SELECT id FROM clients WHERE email = 'rahul.sharma@example.com'    LIMIT 1);
SET @cl_mohammed = (SELECT id FROM clients WHERE email = 'mohammed.rizwan@example.com' LIMIT 1);
SET @cl_pooja    = (SELECT id FROM clients WHERE email = 'pooja.nair@example.com'      LIMIT 1);

-- Employees
SET @emp_sales   = (SELECT id FROM employees WHERE email = 'sales.manager@wheelshift.com' LIMIT 1);
SET @emp_finance = (SELECT id FROM employees WHERE email = 'finance@wheelshift.com'        LIMIT 1);
SET @emp_store   = (SELECT id FROM employees WHERE email = 'store@wheelshift.com'          LIMIT 1);

-- Cars (all UNSOLD)
SET @car_001 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000001' LIMIT 1); -- Maruti Alto K10 Lxi
SET @car_002 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000002' LIMIT 1); -- Maruti Alto K10 Vxi
SET @car_003 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000003' LIMIT 1); -- Hyundai Santro Era
SET @car_004 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000004' LIMIT 1); -- Hyundai Santro Sportz
SET @car_005 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000005' LIMIT 1); -- Tata Tiago Xe
SET @car_006 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000006' LIMIT 1); -- Tata Tiago Xt
SET @car_008 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000008' LIMIT 1); -- Renault Kwid Rxt 1.0
SET @car_009 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000009' LIMIT 1); -- Maruti Eeco 7 Str
SET @car_011 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000011' LIMIT 1); -- Toyota Etios Liva Gd
SET @car_014 = (SELECT id FROM cars WHERE vin_number = 'C2026DV0000000014' LIMIT 1); -- Renault Triber Rxt

-- Motorcycles (all UNSOLD)
SET @moto_002 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000002' LIMIT 1); -- Hero Splendor+
SET @moto_003 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000003' LIMIT 1); -- Bajaj Pulsar 150
SET @moto_004 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000004' LIMIT 1); -- Bajaj Platina 110
SET @moto_005 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000005' LIMIT 1); -- TVS Jupiter
SET @moto_009 = (SELECT id FROM motorcycles WHERE vin_number = 'M2026DV0000000009' LIMIT 1); -- Suzuki Gixxer

-- ════════════════════════════════════════════════════════════════════════════
-- STEP 2 — Insert 15 inquiries
--
-- Idempotency: NOT EXISTS check on (client_id, car_id|motorcycle_id, inquiry_type)
-- which is logically unique across all 15 rows in this seed set.
--
-- ── Anjali Gupta — 5 inquiries (most active, research-heavy) ────────────────
-- ════════════════════════════════════════════════════════════════════════════

-- #1  Anjali / C001 Alto K10 Lxi / VEHICLE_INFO / OPEN
--     Browsing for a second affordable family hatchback — no urgency
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_001, NULL, 'CAR', @cl_anjali, NULL,
  'VEHICLE_INFO',
  'Hi, I am interested in the Alto K10 Lxi listed. Could you share its complete service history and any past accident record? Looking for a second car for my parents — preferably low maintenance.',
  'OPEN', NULL, NULL, '2025-10-12 09:15:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_anjali AND car_id=@car_001 AND inquiry_type='VEHICLE_INFO');

-- #2  Anjali / C003 Santro Era / TEST_DRIVE_REQUEST / IN_PROGRESS (assigned to sales)
--     Active request — sales team is scheduling
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_003, NULL, 'CAR', @cl_anjali, @emp_sales,
  'TEST_DRIVE_REQUEST',
  'I would like to test drive the Hyundai Santro Era. Available Saturday or Sunday morning. Please confirm availability and location of the test drive.',
  'IN_PROGRESS', NULL, NULL, '2025-11-05 14:30:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_anjali AND car_id=@car_003 AND inquiry_type='TEST_DRIVE_REQUEST');

-- #3  Anjali / C004 Santro Sportz / TEST_DRIVE_REQUEST / RESPONDED
--     Test drive was scheduled and confirmed by the sales team
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_004, NULL, 'CAR', @cl_anjali, @emp_sales,
  'TEST_DRIVE_REQUEST',
  'Santro Sportz looks great. Can it be driven on the highway for a longer test? Would like to feel the highway stability before deciding.',
  'RESPONDED',
  'Hi Anjali, the test drive has been confirmed for 22-Aug-2025 at 10:00 AM at our MG Road Hub branch. The car can be taken on the ring road stretch as well. Please bring a valid driving licence. See you there!',
  '2025-08-20 11:00:00', '2025-08-18 11:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_anjali AND car_id=@car_004 AND inquiry_type='TEST_DRIVE_REQUEST');

-- #4  Anjali / C005 Tiago Xe / PRICE_NEGOTIATION / CLOSED
--     Price negotiation ended without conversion — important negative signal for AI
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_005, NULL, 'CAR', @cl_anjali, @emp_sales,
  'PRICE_NEGOTIATION',
  'The asking price for the Tiago Xe seems high for the mileage. My budget is ₹3.8 L. Is there any room for negotiation? Happy to do immediate cash payment if we can close today.',
  'CLOSED',
  'Thank you for your interest in the Tata Tiago Xe. Our best price is ₹4.05 L which already factors in a fresh PUC certificate and one-year roadside assistance. We are unable to reduce further. Please let us know if you would like to proceed at this price.',
  '2024-11-13 16:30:00', '2024-11-10 16:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_anjali AND car_id=@car_005 AND inquiry_type='PRICE_NEGOTIATION');

-- #5  Anjali / C008 Kwid Rxt 1.0 / VEHICLE_INFO / OPEN
--     Latest inquiry — still comparing, no engagement yet
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_008, NULL, 'CAR', @cl_anjali, NULL,
  'VEHICLE_INFO',
  'How does this Kwid Rxt 1.0 compare to the standard 0.8 variant? Is the 1.0 engine significantly better for longer drives? Also, are all original accessories still with the vehicle?',
  'OPEN', NULL, NULL, '2026-01-08 10:45:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_anjali AND car_id=@car_008 AND inquiry_type='VEHICLE_INFO');

-- ── Priya Patel — 4 inquiries (actively upgrading, mix of types) ─────────────

-- #6  Priya / C002 Alto K10 Vxi / VEHICLE_INFO / OPEN
--     Considering upgrade from her existing Kwid — early research stage
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_002, NULL, 'CAR', @cl_priya, NULL,
  'VEHICLE_INFO',
  'I currently own a Renault Kwid and am considering upgrading. How does the Alto K10 Vxi compare in terms of interiors and features? Is the infotainment touch-screen or basic?',
  'OPEN', NULL, NULL, '2025-12-20 15:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_priya AND car_id=@car_002 AND inquiry_type='VEHICLE_INFO');

-- #7  Priya / C006 Tiago Xt / TEST_DRIVE_REQUEST / IN_PROGRESS
--     Sales team has picked this up but hasn't responded yet
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_006, NULL, 'CAR', @cl_priya, @emp_sales,
  'TEST_DRIVE_REQUEST',
  'The Tiago Xt looks like a solid step up from my current car. Would love a test drive — can it be arranged at the Hebbal branch? Prefer late afternoon on a weekday.',
  'IN_PROGRESS', NULL, NULL, '2026-01-15 09:30:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_priya AND car_id=@car_006 AND inquiry_type='TEST_DRIVE_REQUEST');

-- #8  Priya / C014 Triber Rxt / FINANCING_INQUIRY / RESPONDED
--     Finance option explored — responded with EMI details
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_014, NULL, 'CAR', @cl_priya, @emp_finance,
  'FINANCING_INQUIRY',
  'Interested in the Renault Triber Rxt but the full amount is a stretch. Do you offer in-house financing or tie-ups with a bank? What would the EMI look like at 36 months? Down payment can be ₹1 L.',
  'RESPONDED',
  'Hi Priya, we have tie-ups with HDFC Bank, ICICI Bank and Bajaj Finserv for used vehicle loans. For the Triber Rxt at ₹6.48 L with ₹1 L down, EMI at 36 months is approximately ₹18,200 at 10.5% p.a. We can initiate the application on the same day of your visit. Shall we set up an appointment?',
  '2025-06-24 10:30:00', '2025-06-22 10:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_priya AND car_id=@car_014 AND inquiry_type='FINANCING_INQUIRY');

-- #9  Priya / M005 TVS Jupiter / VEHICLE_INFO / CLOSED
--     Explored scooter option, decided against — negative cross-category signal
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT NULL, @moto_005, 'MOTORCYCLE', @cl_priya, @emp_sales,
  'VEHICLE_INFO',
  'I am primarily looking at cars but the TVS Jupiter caught my eye for daily office commute. Is it suitable for solo use in heavy city traffic? Comfortable for 15–20 km daily?',
  'CLOSED',
  'The TVS Jupiter is one of the best city scooters in its class — excellent fuel economy, easy to manoeuvre in traffic and very comfortable for daily 15–20 km use. That said, given your interest in family cars, a car may still serve you better long-term. Happy to help with either option any time!',
  '2025-03-12 09:45:00', '2025-03-10 13:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_priya AND motorcycle_id=@moto_005 AND inquiry_type='VEHICLE_INFO');

-- ── Vikram Singh — 3 inquiries (Honda loyalist, now eyeing sports & diesel) ──

-- #10 Vikram / C011 Toyota Etios Liva Gd diesel / VEHICLE_INFO / IN_PROGRESS
--     Loyal Honda buyer exploring the diesel segment — high purchase intent
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_011, NULL, 'CAR', @cl_vikram, @emp_store,
  'VEHICLE_INFO',
  'I own a Honda Amaze and I am exploring used diesel options now. Can you share the full service history and last inspection report for the Etios Liva GD? Also is the timing belt recently replaced?',
  'IN_PROGRESS', NULL, NULL, '2026-02-10 11:30:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_vikram AND car_id=@car_011 AND inquiry_type='VEHICLE_INFO');

-- #11 Vikram / M009 Suzuki Gixxer / VEHICLE_INFO / OPEN
--     New inquiry; matches his Honda Hornet ownership — sport segment interest
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT NULL, @moto_009, 'MOTORCYCLE', @cl_vikram, NULL,
  'VEHICLE_INFO',
  'Already own a Honda Hornet 2.0. Considering the Gixxer as a second sports bike. How does it compare in terms of ride quality and maintenance cost? Any known issues with this unit?',
  'OPEN', NULL, NULL, '2026-03-01 16:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_vikram AND motorcycle_id=@moto_009 AND inquiry_type='VEHICLE_INFO');

-- #12 Vikram / M003 Bajaj Pulsar 150 / PRICE_NEGOTIATION / CLOSED
--     Negotiated hard, could not agree on price — lost deal signal
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT NULL, @moto_003, 'MOTORCYCLE', @cl_vikram, @emp_sales,
  'PRICE_NEGOTIATION',
  'Interested in the Pulsar 150 for my younger cousin. What is your absolute bottom price? Target budget is ₹85,000 — I am ready to pay today in cash if we can agree.',
  'CLOSED',
  'Thank you Vikram for your offer. Our minimum price for the Bajaj Pulsar 150 is ₹92,000 which includes a fresh PUC certificate and 1-month warranty on the engine. The bike is in excellent condition with only one previous owner. Selling below ₹92,000 is unfortunately not possible. We hope you find the right match!',
  '2024-12-07 15:00:00', '2024-12-05 10:00:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_vikram AND motorcycle_id=@moto_003 AND inquiry_type='PRICE_NEGOTIATION');

-- ── Rahul Sharma — 1 inquiry (already purchased; minimal browsing) ────────────

-- #13 Rahul / M002 Hero Splendor+ / VEHICLE_INFO / OPEN
--     Likely replacing the Pulsar he bought with a simpler commuter for office
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT NULL, @moto_002, 'MOTORCYCLE', @cl_rahul, NULL,
  'VEHICLE_INFO',
  'Need a simple office commuter — the Pulsar I have is too heavy for daily traffic. Does this Hero Splendor Plus have any insurance claim history? What is the last service date?',
  'OPEN', NULL, NULL, '2026-03-15 08:45:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_rahul AND motorcycle_id=@moto_002 AND inquiry_type='VEHICLE_INFO');

-- ── Mohammed Rizwan — 1 inquiry (satisfied with his 2 bikes; mild browse) ────

-- #14 Mohammed / M004 Bajaj Platina 110 / VEHICLE_INFO / OPEN
--     Budget-focused; comparing against CT 110 he already owns
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT NULL, @moto_004, 'MOTORCYCLE', @cl_mohammed, NULL,
  'VEHICLE_INFO',
  'I own the Bajaj CT 110. Is the Platina 110 worth the extra cost? What is the mileage difference in real-world city conditions? Also is the SNS suspension much better for bad roads?',
  'OPEN', NULL, NULL, '2026-02-28 12:30:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_mohammed AND motorcycle_id=@moto_004 AND inquiry_type='VEHICLE_INFO');

-- ── Pooja Nair — 1 inquiry (conservative; occasional family-need browse) ──────

-- #15 Pooja / C009 Maruti Eeco 7 Str / VEHICLE_INFO / OPEN
--     Interested in 7-seater after buying the Rio; family transport need
INSERT INTO inquiries (car_id, motorcycle_id, vehicle_type, client_id, assigned_employee_id,
  inquiry_type, message, status, response, response_date, created_at)
SELECT @car_009, NULL, 'CAR', @cl_pooja, NULL,
  'VEHICLE_INFO',
  'The 7-seater Eeco looks ideal for family outings. Does it have a valid fitness certificate? Also can we schedule a physical inspection before deciding? Located in Pune so will need to travel to the branch.',
  'OPEN', NULL, NULL, '2026-03-20 14:15:00'
FROM DUAL WHERE NOT EXISTS (
  SELECT 1 FROM inquiries WHERE client_id=@cl_pooja AND car_id=@car_009 AND inquiry_type='VEHICLE_INFO');

SQL

# ── Report ────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  SEED REPORT — INQUIRIES${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo ""

TOTAL=$(run_sql "SELECT COUNT(*) FROM inquiries i JOIN clients cl ON cl.id=i.client_id WHERE cl.email LIKE '%@example.com';")
printf "  %-32s %s\n" "Total seed inquiries:" "$TOTAL"

echo ""
echo -e "${BOLD}  Status breakdown:${RESET}"
run_sql "
  SELECT CONCAT('  ', RPAD(status, 14, ' '), COUNT(*), ' inquiry/inquiries')
  FROM inquiries i
  JOIN clients cl ON cl.id = i.client_id
  WHERE cl.email LIKE '%@example.com'
  GROUP BY status
  ORDER BY FIELD(status,'OPEN','IN_PROGRESS','RESPONDED','CLOSED');
"

echo ""
echo -e "${BOLD}  Per-client summary:${RESET}"
run_sql "
  SELECT CONCAT(
    '  ', RPAD(cl.name, 22, ' '),
    COUNT(*), ' inquiry/inquiries   types: ',
    GROUP_CONCAT(DISTINCT i.inquiry_type ORDER BY i.inquiry_type SEPARATOR ', ')
  )
  FROM inquiries i
  JOIN clients cl ON cl.id = i.client_id
  WHERE cl.email LIKE '%@example.com'
  GROUP BY cl.id, cl.name
  ORDER BY COUNT(*) DESC;
"

echo ""
echo -e "${BOLD}  Vehicle type split:${RESET}"
run_sql "
  SELECT CONCAT('  ', RPAD(vehicle_type, 14, ' '), COUNT(*), ' inquiry/inquiries')
  FROM inquiries i
  JOIN clients cl ON cl.id = i.client_id
  WHERE cl.email LIKE '%@example.com'
  GROUP BY vehicle_type;
"

echo ""
success "All done — inquiries seeded successfully."
echo ""
