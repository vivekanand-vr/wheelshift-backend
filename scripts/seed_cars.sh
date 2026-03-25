#!/usr/bin/env bash
# =============================================================
# seed_cars.sh
# Seeds 20 sample cars into the cars table via Docker exec.
#
# Usage (from repo root):
#   bash scripts/seed_cars.sh
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
VIN_PREFIX="C2026DV"

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
echo -e "${BOLD}║        WheelShift — Seed Cars Script         ║${RESET}"
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

info "Checking required car_models exist..."
MODEL_COUNT=$(run_sql "SELECT COUNT(*) FROM car_models;")
if [ "$MODEL_COUNT" -lt 10 ]; then
  error "car_models table has only $MODEL_COUNT rows. Run Flyway migrations first."
  exit 1
fi
success "car_models has $MODEL_COUNT rows."

# ── Existing rows check ────────────────────────────────────────
EXISTING=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE '${VIN_PREFIX}%';")
if [ "$EXISTING" -gt 0 ]; then
  warn "$EXISTING seed car(s) already present — INSERT IGNORE will skip duplicates."
fi

# ── Seed SQL ──────────────────────────────────────────────────
info "Seeding 20 cars..."

docker exec -i "$CONTAINER" \
  mysql -u"$USER" -p"$PASS" "$DB" 2>/dev/null <<'SQL'

INSERT IGNORE INTO cars
  (car_model_id, vin_number, registration_number, year, color,
   mileage_km, engine_cc, status, storage_location_id,
   purchase_date, purchase_price, selling_price,
   doors, seats)
VALUES
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Alto K10' AND variant='Lxi' LIMIT 1),
    'C2026DV0000000001','MH12AB1001',2020,'Pearl White',
    28800,998,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='Main Warehouse' LIMIT 1),
    '2023-03-10',290000.00,335000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Alto K10' AND variant='Vxi' LIMIT 1),
    'C2026DV0000000002','MH12AB1002',2021,'Speedy Blue',
    15500,998,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='Main Warehouse' LIMIT 1),
    '2023-07-22',342000.00,389000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Era Mt' LIMIT 1),
    'C2026DV0000000003','MH12AB1003',2019,'Aqua Teal',
    45200,1086,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='Main Warehouse' LIMIT 1),
    '2023-01-15',312000.00,368000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Sportz Mt' LIMIT 1),
    'C2026DV0000000004','DL01CD2001',2021,'Typhoon Silver',
    19000,1086,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='North Branch' LIMIT 1),
    '2023-08-05',458000.00,519000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tiago' AND variant='Revotron Xe' LIMIT 1),
    'C2026DV0000000005','DL01CD2002',2020,'Pearlescent White',
    32500,1199,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='North Branch' LIMIT 1),
    '2023-04-18',422000.00,479000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tiago' AND variant='Revotron Xt' LIMIT 1),
    'C2026DV0000000006','DL01CD2003',2022,'Pure Silver',
    9800,1199,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='North Branch' LIMIT 1),
    '2023-11-30',495000.00,555000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Kwid' AND variant='Rxl 0.8' LIMIT 1),
    'C2026DV0000000007','MH12AB1007',2019,'Fiery Red',
    52000,799,'SOLD',
    (SELECT id FROM storage_locations WHERE name='Main Warehouse' LIMIT 1),
    '2022-09-12',302000.00,342000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Kwid' AND variant='Rxt 1.0' LIMIT 1),
    'C2026DV0000000008','KA05EF3001',2021,'Moonlight Silver',
    21500,999,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='South Storage' LIMIT 1),
    '2023-09-25',404000.00,459000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Eeco' AND variant='7 Str' LIMIT 1),
    'C2026DV0000000009','KA05EF3002',2020,'White',
    38000,1193,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='South Storage' LIMIT 1),
    '2023-02-28',352000.00,400000.00,5,7
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='G' LIMIT 1),
    'C2026DV0000000010','KA05EF3003',2019,'Platinum White Pearl',
    58000,1198,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='South Storage' LIMIT 1),
    '2022-11-14',482000.00,548000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='Gd' LIMIT 1),
    'C2026DV0000000011','TN09GH4001',2020,'Grey Metallic',
    41000,1364,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='East Facility' LIMIT 1),
    '2023-05-07',562000.00,628000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Ignis' AND variant='Delta 1.2 Mt' LIMIT 1),
    'C2026DV0000000012','TN09GH4002',2021,'Premium Silver',
    18800,1197,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='East Facility' LIMIT 1),
    '2023-07-19',552000.00,622000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Triber' AND variant='Rxl' LIMIT 1),
    'C2026DV0000000013','DL01CD2004',2022,'Orange',
    8200,999,'RESERVED',
    (SELECT id FROM storage_locations WHERE name='North Branch' LIMIT 1),
    '2024-01-08',532000.00,594000.00,5,7
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Triber' AND variant='Rxt' LIMIT 1),
    'C2026DV0000000014','KA05EF3004',2021,'Fiery Red',
    22000,999,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='South Storage' LIMIT 1),
    '2023-06-14',582000.00,648000.00,5,7
  ),
  (
    (SELECT id FROM car_models WHERE make='Datsun' AND model='Go' AND variant='T' LIMIT 1),
    'C2026DV0000000015','MH12AB1015',2020,'Red Alert',
    35500,1198,'SOLD',
    (SELECT id FROM storage_locations WHERE name='Main Warehouse' LIMIT 1),
    '2022-08-30',382000.00,428000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Datsun' AND model='Redi-Go' AND variant='D' LIMIT 1),
    'C2026DV0000000016','TN09GH4003',2019,'White',
    49000,799,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='East Facility' LIMIT 1),
    '2022-10-22',262000.00,299000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Celerio X' AND variant='Vxi' LIMIT 1),
    'C2026DV0000000017','MH12AB1017',2020,'Magma Grey',
    29000,998,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='Main Warehouse' LIMIT 1),
    '2023-04-02',432000.00,488000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Celerio Tour' AND variant='H2' LIMIT 1),
    'C2026DV0000000018','DL01CD2005',2021,'White',
    24000,998,'RESERVED',
    (SELECT id FROM storage_locations WHERE name='North Branch' LIMIT 1),
    '2023-10-11',402000.00,448000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Magna Mt' LIMIT 1),
    'C2026DV0000000019','KA05EF3005',2020,'Typhoon Silver',
    33500,1086,'AVAILABLE',
    (SELECT id FROM storage_locations WHERE name='South Storage' LIMIT 1),
    '2023-03-27',422000.00,478000.00,5,5
  ),
  (
    (SELECT id FROM car_models WHERE make='Premier' AND model='Rio' AND variant='Ex' LIMIT 1),
    'C2026DV0000000020','TN09GH4004',2018,'White',
    72000,1493,'SOLD',
    (SELECT id FROM storage_locations WHERE name='East Facility' LIMIT 1),
    '2022-06-05',492000.00,542000.00,5,5
  );

SQL

# ── Report ────────────────────────────────────────────────────
# Note: storage_locations.current_vehicle_count is maintained automatically
# by the database triggers installed in V18__Add_Storage_Location_Triggers.sql
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  SEED REPORT — CARS${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

TOTAL=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE '${VIN_PREFIX}%';")
AVAILABLE=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE '${VIN_PREFIX}%' AND status='AVAILABLE';")
RESERVED=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE '${VIN_PREFIX}%' AND status='RESERVED';")
SOLD=$(run_sql "SELECT COUNT(*) FROM cars WHERE vin_number LIKE '${VIN_PREFIX}%' AND status='SOLD';")

printf "  %-20s %s\n" "Total seeded:" "$TOTAL"
printf "  %-20s %s\n" "Available:"    "$AVAILABLE"
printf "  %-20s %s\n" "Reserved:"     "$RESERVED"
printf "  %-20s %s\n" "Sold:"         "$SOLD"

echo ""
echo -e "${BOLD}  Storage location counts:${RESET}"
run_sql "
  SELECT
    CONCAT('  ', RPAD(sl.name, 20, ' '), sl.current_vehicle_count, ' active vehicle(s)')
  FROM storage_locations sl
  ORDER BY sl.name;
"

echo ""
echo -e "${BOLD}  Breakdown by make:${RESET}"
run_sql "
  SELECT CONCAT('  ', RPAD(cm.make, 22, ' '), COUNT(*), ' car(s)')
  FROM cars c
  JOIN car_models cm ON c.car_model_id = cm.id
  WHERE c.vin_number LIKE '${VIN_PREFIX}%'
  GROUP BY cm.make
  ORDER BY cm.make;
"

echo ""
success "Done! Run scripts/seed_motorcycles.sh to seed motorcycles next."
echo ""
