#!/usr/bin/env bash
# =============================================================
# seed_motorcycles.sh
# Seeds 20 sample motorcycles into the motorcycles table via Docker exec.
#
# Usage (from repo root):
#   bash scripts/seed_motorcycles.sh
#
# Prerequisites:
#   - Docker must be running
#   - Container 'wheelshift-mysql' must be up
#   - Flyway migrations V1-V17 must have been applied
# =============================================================

set -euo pipefail

# ── Config ────────────────────────────────────────────────────
CONTAINER="wheelshift-mysql"
DB="wheelshift_db"
USER="wheelshift_user"
PASS="wheelshift_pass_2025"
VIN_PREFIX="M2026DV"

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
echo -e "${BOLD}║     WheelShift — Seed Motorcycles Script     ║${RESET}"
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

info "Checking required motorcycle_models exist..."
MODEL_COUNT=$(run_sql "SELECT COUNT(*) FROM motorcycle_models;")
if [ "$MODEL_COUNT" -lt 10 ]; then
  error "motorcycle_models table has only $MODEL_COUNT rows. Run Flyway migrations first."
  exit 1
fi
success "motorcycle_models has $MODEL_COUNT rows."

# ── Existing rows check ────────────────────────────────────────
EXISTING=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE '${VIN_PREFIX}%';")
if [ "$EXISTING" -gt 0 ]; then
  warn "$EXISTING seed motorcycle(s) already present — INSERT IGNORE will skip duplicates."
fi

# ── Seed SQL ──────────────────────────────────────────────────
info "Seeding 20 motorcycles..."

docker exec -i "$CONTAINER" \
  mysql -u"$USER" -p"$PASS" "$DB" 2>/dev/null <<'SQL'

-- Pre-fetch storage location IDs to avoid MySQL error 1442
-- (trigger cannot update a table already referenced by the INSERT statement)
SET @loc_mg_road   = (SELECT id FROM storage_locations WHERE name='MG Road Hub' LIMIT 1);
SET @loc_hebbal    = (SELECT id FROM storage_locations WHERE name='Hebbal Branch' LIMIT 1);
SET @loc_elec_city = (SELECT id FROM storage_locations WHERE name='Electronic City Storage' LIMIT 1);
SET @loc_whitefield= (SELECT id FROM storage_locations WHERE name='Whitefield Facility' LIMIT 1);

INSERT IGNORE INTO motorcycles
  (vin_number, registration_number, engine_number, chassis_number,
   motorcycle_model_id, color, mileage_km, manufacture_year, registration_date,
   status, storage_location_id,
   purchase_price, purchase_date, selling_price, minimum_price,
   previous_owners, insurance_expiry_date, pollution_certificate_expiry,
   is_financed, is_accidental, description)
VALUES
  (
    'M2026DV0000000001','MH12AC5001','ENHFD20000001','CHHFD20000001',
    (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='HF Deluxe' LIMIT 1),
    'Black',35000,2020,'2020-04-10','AVAILABLE',@loc_mg_road,
    39000.00,'2023-06-15',52000.00,44000.00,
    2,'2026-04-09','2026-04-09',FALSE,FALSE,
    '2020 Hero HF Deluxe, reliable daily commuter with proven engine and great fuel economy.'
  ),
  (
    'M2026DV0000000002','MH12AC5002','ENSPL21000002','CHSPL21000002',
    (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='Splendor +' LIMIT 1),
    'Black Red',22000,2021,'2021-05-20','AVAILABLE',@loc_mg_road,
    59000.00,'2023-09-08',78000.00,68000.00,
    1,'2026-05-19','2026-05-19',FALSE,FALSE,
    '2021 Hero Splendor Plus, best-selling commuter motorcycle in India, low mileage.'
  ),
  (
    'M2026DV0000000003','MH12AC5003','ENP15022000003','CHP15022000003',
    (SELECT id FROM motorcycle_models WHERE make='Bajaj' AND model='Pulsar 150' LIMIT 1),
    'Black Red',8500,2022,'2022-03-08','AVAILABLE',@loc_mg_road,
    88000.00,'2024-01-20',109000.00,95000.00,
    1,'2027-03-07','2027-03-07',FALSE,FALSE,
    '2022 Bajaj Pulsar 150, sporty twin-disc setup with punchy performance, near new.'
  ),
  (
    'M2026DV0000000004','MH12AC5004','ENPLT20000004','CHPLT20000004',
    (SELECT id FROM motorcycle_models WHERE make='Bajaj' AND model='Platina 110' LIMIT 1),
    'Black Blue',28000,2020,'2020-06-25','AVAILABLE',@loc_mg_road,
    55000.00,'2023-07-10',71000.00,62000.00,
    2,'2026-06-24','2026-06-24',FALSE,FALSE,
    '2020 Bajaj Platina 110, superior ride comfort with SNS suspension technology.'
  ),
  (
    'M2026DV0000000005','MH12AC5005','ENJUP21000005','CHJUP21000005',
    (SELECT id FROM motorcycle_models WHERE make='TVS' AND model='Jupiter' LIMIT 1),
    'Matte Grey',18000,2021,'2021-07-14','AVAILABLE',@loc_mg_road,
    63000.00,'2023-08-22',82000.00,72000.00,
    1,'2026-07-13','2026-07-13',FALSE,FALSE,
    '2021 TVS Jupiter scooter, spacious under-seat storage and comfortable city ride.'
  ),
  (
    'M2026DV0000000006','MH12AC5006','ENFAS22000006','CHFAS22000006',
    (SELECT id FROM motorcycle_models WHERE make='Yamaha' AND model='Fascino 125' LIMIT 1),
    'Metallic White',6500,2022,'2022-08-01','RESERVED',@loc_mg_road,
    66000.00,'2024-02-14',85000.00,75000.00,
    1,'2027-07-31','2027-07-31',FALSE,FALSE,
    '2022 Yamaha Fascino 125, stylish premium scooter with fuel injection, currently reserved.'
  ),
  (
    'M2026DV0000000007','DL01CE3001','ENCBS20000007','CHCBS20000007',
    (SELECT id FROM motorcycle_models WHERE make='Honda' AND model='CB Shine' LIMIT 1),
    'Pearl Igneous Black',32000,2020,'2020-09-18','AVAILABLE',@loc_hebbal,
    63000.00,'2023-05-30',81000.00,71000.00,
    2,'2026-09-17','2026-09-17',FALSE,FALSE,
    '2020 Honda CB Shine, smooth and refined commuter engine with proven reliability.'
  ),
  (
    'M2026DV0000000008','DL01CE3002','ENSP121000008','CHSP121000008',
    (SELECT id FROM motorcycle_models WHERE make='Honda' AND model='SP 125' LIMIT 1),
    'Imperial Red Metallic',15000,2021,'2021-10-05','AVAILABLE',@loc_hebbal,
    72000.00,'2023-11-18',92000.00,81000.00,
    1,'2026-10-04','2026-10-04',FALSE,FALSE,
    '2021 Honda SP 125, feature-rich commuter with CBS braking and fuel injection.'
  ),
  (
    'M2026DV0000000009','DL01CE3003','ENGIX22000009','CHGIX22000009',
    (SELECT id FROM motorcycle_models WHERE make='Suzuki' AND model='Gixxer' LIMIT 1),
    'Metallic Triton Blue',7500,2022,'2022-01-27','AVAILABLE',@loc_hebbal,
    112000.00,'2024-03-05',138000.00,122000.00,
    1,'2027-01-26','2027-01-26',FALSE,FALSE,
    '2022 Suzuki Gixxer, sporty naked bike with modern LED styling and oil-cooled engine.'
  ),
  (
    'M2026DV0000000010','DL01CE3004','ENNTQ21000010','CHNTQ21000010',
    (SELECT id FROM motorcycle_models WHERE make='TVS' AND model='Ntorq 125' LIMIT 1),
    'Race Red',21000,2021,'2021-03-22','AVAILABLE',@loc_hebbal,
    71000.00,'2023-10-12',90000.00,79000.00,
    1,'2026-03-21','2026-03-21',FALSE,FALSE,
    '2021 TVS Ntorq 125, performance scooter with SmartXonnect Bluetooth connectivity.'
  ),
  (
    'M2026DV0000000011','DL01CE3005','ENSSP20000011','CHSSP20000011',
    (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='Super Splendor' LIMIT 1),
    'Glory Black',30000,2020,'2020-11-12','SOLD',@loc_hebbal,
    59000.00,'2022-12-20',73000.00,64000.00,
    2,'2026-11-11','2026-11-11',FALSE,FALSE,
    '2020 Hero Super Splendor, premium 125cc commuter with comfortable ride, sold as-is.'
  ),
  (
    'M2026DV0000000012','KA05FG6001','ENGLM21000012','CHGLM21000012',
    (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='Glamour 125' LIMIT 1),
    'Champion Black Blue',19000,2021,'2021-06-30','AVAILABLE',@loc_elec_city,
    68000.00,'2023-09-25',87000.00,76000.00,
    1,'2026-06-29','2026-06-29',FALSE,FALSE,
    '2021 Hero Glamour 125, stylish commuter with i3S auto stop-start technology.'
  ),
  (
    'M2026DV0000000013','KA05FG6002','ENP12522000013','CHP12522000013',
    (SELECT id FROM motorcycle_models WHERE make='Bajaj' AND model='Pulsar 125' LIMIT 1),
    'Burnt Red',11000,2022,'2022-04-15','AVAILABLE',@loc_elec_city,
    66000.00,'2024-01-08',85000.00,74000.00,
    1,'2027-04-14','2027-04-14',FALSE,FALSE,
    '2022 Bajaj Pulsar 125, entry-level sporty motorcycle with single disc brake option.'
  ),
  (
    'M2026DV0000000014','KA05FG6003','ENRDR22000014','CHRDR22000014',
    (SELECT id FROM motorcycle_models WHERE make='TVS' AND model='Raider' LIMIT 1),
    'Matte Red',9000,2022,'2022-02-18','AVAILABLE',@loc_elec_city,
    78000.00,'2024-02-28',99000.00,87000.00,
    1,'2027-02-17','2027-02-17',FALSE,FALSE,
    '2022 TVS Raider 125, refreshing sporty design with segment-best torque output.'
  ),
  (
    'M2026DV0000000015','KA05FG6004','ENCD120000015','CHCD120000015',
    (SELECT id FROM motorcycle_models WHERE make='Honda' AND model='CD 110 Dream' LIMIT 1),
    'Imperial Red Metallic',38000,2020,'2020-07-08','AVAILABLE',@loc_elec_city,
    57000.00,'2023-04-19',72000.00,63000.00,
    2,'2026-07-07','2026-07-07',FALSE,FALSE,
    '2020 Honda CD 110 Dream, affordable and dependable daily rider with kick start.'
  ),
  (
    'M2026DV0000000016','KA05FG6005','ENRYZ22000016','CHRYZ22000016',
    (SELECT id FROM motorcycle_models WHERE make='Yamaha' AND model='Ray-ZR 125FI' LIMIT 1),
    'Cyan Burst',7000,2022,'2022-09-12','RESERVED',@loc_elec_city,
    69000.00,'2024-02-05',88000.00,77000.00,
    1,'2027-09-11','2027-09-11',FALSE,FALSE,
    '2022 Yamaha Ray-ZR 125FI, sporty disc-brake scooter with Bluetooth connectivity.'
  ),
  (
    'M2026DV0000000017','TN09HJ7001','ENSPT19000017','CHSPT19000017',
    (SELECT id FROM motorcycle_models WHERE make='TVS' AND model='Sport' LIMIT 1),
    'Black White',51000,2019,'2019-08-22','AVAILABLE',@loc_whitefield,
    42000.00,'2022-07-14',54000.00,47000.00,
    3,'2026-08-21','2026-08-21',FALSE,FALSE,
    '2019 TVS Sport, budget commuter built for tough road conditions and long durability.'
  ),
  (
    'M2026DV0000000018','TN09HJ7002','ENCT120000018','CHCT120000018',
    (SELECT id FROM motorcycle_models WHERE make='Bajaj' AND model='CT 110' LIMIT 1),
    'Black',42000,2020,'2020-05-05','SOLD',@loc_whitefield,
    37000.00,'2022-10-28',44000.00,40000.00,
    2,'2026-05-04','2026-05-04',FALSE,FALSE,
    '2020 Bajaj CT 110, tough and economical entry-level motorcycle, sold as-is.'
  ),
  (
    'M2026DV0000000019','TN09HJ7003','ENPPI21000019','CHPPI21000019',
    (SELECT id FROM motorcycle_models WHERE make='Hero' AND model='Passion Pro i3S' LIMIT 1),
    'Black Red',26000,2021,'2021-08-17','AVAILABLE',@loc_whitefield,
    61000.00,'2023-12-06',78000.00,68000.00,
    1,'2026-08-16','2026-08-16',FALSE,FALSE,
    '2021 Hero Passion Pro i3S, fuel-saving auto stop-start system for smart commuting.'
  ),
  (
    'M2026DV0000000020','TN09HJ7004','ENLIV21000020','CHLIV21000020',
    (SELECT id FROM motorcycle_models WHERE make='Honda' AND model='Livo' LIMIT 1),
    'Pearl Fadeless White',20000,2021,'2021-12-03','AVAILABLE',@loc_whitefield,
    65000.00,'2023-10-30',84000.00,73000.00,
    1,'2026-12-02','2026-12-02',FALSE,FALSE,
    '2021 Honda Livo, stylish commuter with LED headlamp and CBS safety braking system.'
  );

-- Recalculate motorcycle counts for all storage locations
-- (no DB triggers since V20 — counts managed in application layer)
UPDATE storage_locations sl
SET current_motorcycle_count = (
  SELECT COUNT(*) FROM motorcycles m
  WHERE m.storage_location_id = sl.id AND m.status <> 'SOLD'
);

SQL

# ── Report ────────────────────────────────────────────────────
# Note: storage_locations.current_car_count and current_motorcycle_count are maintained
# at the application/service layer (no DB triggers since V20).
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  SEED REPORT — MOTORCYCLES${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

TOTAL=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE '${VIN_PREFIX}%';")
AVAILABLE=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE '${VIN_PREFIX}%' AND status='AVAILABLE';")
RESERVED=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE '${VIN_PREFIX}%' AND status='RESERVED';")
SOLD=$(run_sql "SELECT COUNT(*) FROM motorcycles WHERE vin_number LIKE '${VIN_PREFIX}%' AND status='SOLD';")

printf "  %-20s %s\n" "Total seeded:" "$TOTAL"
printf "  %-20s %s\n" "Available:"    "$AVAILABLE"
printf "  %-20s %s\n" "Reserved:"     "$RESERVED"
printf "  %-20s %s\n" "Sold:"         "$SOLD"

echo ""
echo -e "${BOLD}  Storage location counts:${RESET}"
run_sql "
  SELECT
    CONCAT('  ', RPAD(sl.name, 20, ' '), (sl.current_car_count + sl.current_motorcycle_count), ' active vehicle(s)')
  FROM storage_locations sl
  ORDER BY sl.name;
"

echo ""
echo -e "${BOLD}  Breakdown by make:${RESET}"
run_sql "
  SELECT CONCAT('  ', RPAD(mm.make, 22, ' '), COUNT(*), ' motorcycle(s)')
  FROM motorcycles m
  JOIN motorcycle_models mm ON m.motorcycle_model_id = mm.id
  WHERE m.vin_number LIKE '${VIN_PREFIX}%'
  GROUP BY mm.make
  ORDER BY mm.make;
"

echo ""
success "Done! Run scripts/seed_clients.sh to seed clients next."
echo ""
