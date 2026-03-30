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

-- Pre-fetch storage location IDs to avoid MySQL error 1442
-- (trigger cannot update a table already referenced by the INSERT statement)
SET @loc_mg_road   = (SELECT id FROM storage_locations WHERE name='MG Road Hub' LIMIT 1);
SET @loc_hebbal    = (SELECT id FROM storage_locations WHERE name='Hebbal Branch' LIMIT 1);
SET @loc_elec_city = (SELECT id FROM storage_locations WHERE name='Electronic City Storage' LIMIT 1);
SET @loc_whitefield= (SELECT id FROM storage_locations WHERE name='Whitefield Facility' LIMIT 1);

INSERT IGNORE INTO cars
  (car_model_id, vin_number, registration_number, year, color,
   mileage_km, engine_cc, status, storage_location_id,
   purchase_date, purchase_price, selling_price,
   doors, seats, description)
VALUES
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Alto K10' AND variant='Lxi' LIMIT 1),
    'C2026DV0000000001','MH12AB1001',2020,'Pearl White',
    28800,998,'AVAILABLE',@loc_mg_road,
    '2023-03-10',290000.00,335000.00,5,5,
    'Well-maintained 2020 Maruti Alto K10 Lxi, low mileage, excellent city fuel economy.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Alto K10' AND variant='Vxi' LIMIT 1),
    'C2026DV0000000002','MH12AB1002',2021,'Speedy Blue',
    15500,998,'AVAILABLE',@loc_mg_road,
    '2023-07-22',342000.00,389000.00,5,5,
    '2021 Maruti Alto K10 Vxi with power windows and alloy wheels, great daily commuter.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Era Mt' LIMIT 1),
    'C2026DV0000000003','MH12AB1003',2019,'Aqua Teal',
    45200,1086,'AVAILABLE',@loc_mg_road,
    '2023-01-15',312000.00,368000.00,5,5,
    '2019 Hyundai Santro Era, spacious hatchback with comfortable ride and good mileage.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Sportz Mt' LIMIT 1),
    'C2026DV0000000004','DL01CD2001',2021,'Typhoon Silver',
    19000,1086,'AVAILABLE',@loc_hebbal,
    '2023-08-05',458000.00,519000.00,5,5,
    '2021 Hyundai Santro Sportz, well-equipped hatchback with sporty bumpers, low use.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tiago' AND variant='Revotron Xe' LIMIT 1),
    'C2026DV0000000005','DL01CD2002',2020,'Pearlescent White',
    32500,1199,'AVAILABLE',@loc_hebbal,
    '2023-04-18',422000.00,479000.00,5,5,
    '2020 Tata Tiago entry-level, dependable and fuel-efficient compact city car.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tiago' AND variant='Revotron Xt' LIMIT 1),
    'C2026DV0000000006','DL01CD2003',2022,'Pure Silver',
    9800,1199,'AVAILABLE',@loc_hebbal,
    '2023-11-30',495000.00,555000.00,5,5,
    '2022 Tata Tiago Xt, near-new condition with modern safety features and full warranty.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Kwid' AND variant='Rxl 0.8' LIMIT 1),
    'C2026DV0000000007','MH12AB1007',2019,'Fiery Red',
    52000,799,'SOLD',@loc_mg_road,
    '2022-09-12',302000.00,342000.00,5,5,
    '2019 Renault Kwid 0.8 Rxl, budget-friendly compact hatchback, sold as-is.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Kwid' AND variant='Rxt 1.0' LIMIT 1),
    'C2026DV0000000008','KA05EF3001',2021,'Moonlight Silver',
    21500,999,'AVAILABLE',@loc_elec_city,
    '2023-09-25',404000.00,459000.00,5,5,
    '2021 Renault Kwid Rxt 1.0, peppy performance with touchscreen infotainment system.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Eeco' AND variant='7 Str' LIMIT 1),
    'C2026DV0000000009','KA05EF3002',2020,'White',
    38000,1193,'AVAILABLE',@loc_elec_city,
    '2023-02-28',352000.00,400000.00,5,7,
    '2020 Maruti Eeco 7-seater, practical family van ideal for large groups or cargo.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='G' LIMIT 1),
    'C2026DV0000000010','KA05EF3003',2019,'Platinum White Pearl',
    58000,1198,'AVAILABLE',@loc_elec_city,
    '2022-11-14',482000.00,548000.00,5,5,
    '2019 Toyota Etios Liva petrol, strong build quality and proven long-term reliability.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='Gd' LIMIT 1),
    'C2026DV0000000011','TN09GH4001',2020,'Grey Metallic',
    41000,1364,'AVAILABLE',@loc_whitefield,
    '2023-05-07',562000.00,628000.00,5,5,
    '2020 Toyota Etios Liva diesel, excellent fuel economy ideal for long-distance travel.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Ignis' AND variant='Delta 1.2 Mt' LIMIT 1),
    'C2026DV0000000012','TN09GH4002',2021,'Premium Silver',
    18800,1197,'AVAILABLE',@loc_whitefield,
    '2023-07-19',552000.00,622000.00,5,5,
    '2021 Maruti Ignis Delta, compact crossover with premium interior and minimal use.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Triber' AND variant='Rxl' LIMIT 1),
    'C2026DV0000000013','DL01CD2004',2022,'Orange',
    8200,999,'RESERVED',@loc_hebbal,
    '2024-01-08',532000.00,594000.00,5,7,
    '2022 Renault Triber Rxl, versatile 7-seater MPV, nearly new, currently reserved.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Triber' AND variant='Rxt' LIMIT 1),
    'C2026DV0000000014','KA05EF3004',2021,'Fiery Red',
    22000,999,'AVAILABLE',@loc_elec_city,
    '2023-06-14',582000.00,648000.00,5,7,
    '2021 Renault Triber Rxt, fully loaded 7-seater with foldable seats and touchscreen.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Datsun' AND model='Go' AND variant='T' LIMIT 1),
    'C2026DV0000000015','MH12AB1015',2020,'Red Alert',
    35500,1198,'SOLD',@loc_mg_road,
    '2022-08-30',382000.00,428000.00,5,5,
    '2020 Datsun Go T, budget-friendly hatchback with spacious cabin, sold as-is.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Datsun' AND model='Redi-Go' AND variant='D' LIMIT 1),
    'C2026DV0000000016','TN09GH4003',2019,'White',
    49000,799,'AVAILABLE',@loc_whitefield,
    '2022-10-22',262000.00,299000.00,5,5,
    '2019 Datsun Redi-Go, lightweight city car with best-in-class fuel efficiency.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Celerio X' AND variant='Vxi' LIMIT 1),
    'C2026DV0000000017','MH12AB1017',2020,'Magma Grey',
    29000,998,'AVAILABLE',@loc_mg_road,
    '2023-04-02',432000.00,488000.00,5,5,
    '2020 Maruti Celerio X Vxi, crossover-styled hatchback with alloy wheels and Bluetooth.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Celerio Tour' AND variant='H2' LIMIT 1),
    'C2026DV0000000018','DL01CD2005',2021,'White',
    24000,998,'RESERVED',@loc_hebbal,
    '2023-10-11',402000.00,448000.00,5,5,
    '2021 Maruti Celerio Tour H2, CNG-ready compact perfect for high-mileage daily use.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Magna Mt' LIMIT 1),
    'C2026DV0000000019','KA05EF3005',2020,'Typhoon Silver',
    33500,1086,'AVAILABLE',@loc_elec_city,
    '2023-03-27',422000.00,478000.00,5,5,
    '2020 Hyundai Santro Magna, well-equipped mid-variant with dual airbags and rear AC.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Premier' AND model='Rio' AND variant='Ex' LIMIT 1),
    'C2026DV0000000020','TN09GH4004',2018,'White',
    72000,1493,'SOLD',@loc_whitefield,
    '2022-06-05',492000.00,542000.00,5,5,
    '2018 Premier Rio diesel, powerful engine and spacious family car, sold as-is.'
  ),
  -- ── Cars #21 – #50 ────────────────────────────────────────────────────────
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Swift' AND variant='Vxi' LIMIT 1),
    'C2026DV0000000021','MH13BB1001',2021,'Ocean Blue',
    16200,1197,'AVAILABLE',@loc_mg_road,
    '2023-05-01',510000.00,585000.00,5,5,
    '2021 Maruti Swift Vxi, sporty hatchback with smart drive technology and alloy wheels.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Swift' AND variant='Zxi' LIMIT 1),
    'C2026DV0000000022','KA03HJ1001',2022,'Solid Red',
    6800,1197,'AVAILABLE',@loc_hebbal,
    '2024-02-10',648000.00,725000.00,5,5,
    '2022 Maruti Swift Zxi, premium hatchback with LED projector headlamps and reversing camera.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Dzire' AND variant='Vxi' LIMIT 1),
    'C2026DV0000000023','MH14CC1001',2021,'Silky Silver',
    22000,1197,'AVAILABLE',@loc_elec_city,
    '2023-09-15',625000.00,695000.00,4,5,
    '2021 Maruti Dzire Vxi, India most popular compact sedan with premium features.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Dzire' AND variant='Ldi' LIMIT 1),
    'C2026DV0000000024','AP09AA1001',2020,'Dignity Brown',
    32000,1248,'AVAILABLE',@loc_whitefield,
    '2023-04-20',668000.00,748000.00,4,5,
    '2020 Maruti Dzire Ldi diesel, top-tier fuel economy ideal for long-distance intercity use.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Elite I20' AND variant='Era 1.2' LIMIT 1),
    'C2026DV0000000025','MH13BB1002',2021,'Typhoon Silver',
    18000,1197,'AVAILABLE',@loc_mg_road,
    '2023-08-01',540000.00,615000.00,5,5,
    '2021 Hyundai Elite i20 Era, premium feel with refined ride quality and stylish exterior.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Elite I20' AND variant='Magna Plus' LIMIT 1),
    'C2026DV0000000026','KA03HJ1002',2020,'Fiery Red',
    30000,1197,'AVAILABLE',@loc_hebbal,
    '2023-03-15',612000.00,689000.00,5,5,
    '2020 Hyundai Elite i20 Magna Plus, well-equipped with dual airbags and rear AC vents.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Aura' AND variant='S 1.2 Petrol' LIMIT 1),
    'C2026DV0000000027','MH14CC1002',2022,'Polar White',
    8500,1197,'AVAILABLE',@loc_elec_city,
    '2024-01-20',622000.00,698000.00,4,5,
    '2022 Hyundai Aura S, stylish compact sedan with segment-best space and fuel efficiency.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tiago' AND variant='Revotron Xz' LIMIT 1),
    'C2026DV0000000028','AP09AA1002',2021,'Teal Blue',
    14000,1199,'RESERVED',@loc_whitefield,
    '2023-10-10',538000.00,609000.00,5,5,
    '2021 Tata Tiago Xz, fully loaded variant with sunroof and connected car technology.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Altroz' AND variant='Xe Petrol' LIMIT 1),
    'C2026DV0000000029','MH13BB1003',2022,'Calgary White',
    7200,1199,'AVAILABLE',@loc_mg_road,
    '2024-03-05',499000.00,568000.00,5,5,
    '2022 Tata Altroz Xe petrol, entry-level premium hatchback with 5-star GNCAP safety.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tigor' AND variant='Revotron Xe' LIMIT 1),
    'C2026DV0000000030','KA03HJ1003',2021,'Magnetic Red',
    19500,1199,'AVAILABLE',@loc_hebbal,
    '2023-07-22',548000.00,619000.00,4,5,
    '2021 Tata Tigor Xe, compact fastback sedan with 5-star safety and spacious boot.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Honda' AND model='Jazz' AND variant='V Petrol' LIMIT 1),
    'C2026DV0000000031','MH14CC1003',2021,'Lunar Silver Metallic',
    17800,1199,'AVAILABLE',@loc_elec_city,
    '2023-06-18',705000.00,789000.00,5,5,
    '2021 Honda Jazz V petrol, versatile hatchback with magic seat technology and superior cabin space.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Honda' AND model='Amaze' AND variant='E Mt Petrol' LIMIT 1),
    'C2026DV0000000032','AP09AA1003',2021,'Radiant Red',
    23000,1199,'AVAILABLE',@loc_whitefield,
    '2023-05-25',638000.00,712000.00,4,5,
    '2021 Honda Amaze E petrol, dependable compact sedan with proven Honda reliability.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Honda' AND model='Amaze' AND variant='V Mt Diesel' LIMIT 1),
    'C2026DV0000000033','MH13BB1004',2020,'Golden Brown Metallic',
    42000,1498,'SOLD',@loc_mg_road,
    '2022-11-10',795000.00,875000.00,4,5,
    '2020 Honda Amaze V diesel, efficient diesel sedan with Honda CVT, sold as-is.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Volkswagen' AND model='Polo' AND variant='Comfortline 1.0 (P)' LIMIT 1),
    'C2026DV0000000034','KA03HJ1004',2021,'Flash Red',
    20000,999,'AVAILABLE',@loc_hebbal,
    '2023-08-28',635000.00,712000.00,5,5,
    '2021 Volkswagen Polo Comfortline, European build with dual airbags and premium interiors.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Volkswagen' AND model='Polo' AND variant='Highline Plus 1.0 (P)' LIMIT 1),
    'C2026DV0000000035','MH14CC1004',2020,'Reflex Silver Metallic',
    36000,999,'AVAILABLE',@loc_elec_city,
    '2023-02-14',725000.00,815000.00,5,5,
    '2020 VW Polo Highline Plus, loaded with touchscreen, sunroof and premium sound system.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Mahindra' AND model='Kuv100 Nxt' AND variant='K4+ 6Str' LIMIT 1),
    'C2026DV0000000036','AP09AA1004',2020,'Aquamarine',
    42500,1198,'AVAILABLE',@loc_whitefield,
    '2023-01-09',542000.00,615000.00,5,6,
    '2020 Mahindra KUV100 Nxt K4+ 6-seater, compact yet spacious with bold masculine styling.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Datsun' AND model='Go' AND variant='A' LIMIT 1),
    'C2026DV0000000037','MH13BB1005',2019,'Coral Red',
    55000,1198,'SOLD',@loc_mg_road,
    '2022-07-20',445000.00,495000.00,5,5,
    '2019 Datsun Go A, budget hatchback with spacious 5-seater cabin, sold as-is.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Datsun' AND model='Redi-Go' AND variant='T' LIMIT 1),
    'C2026DV0000000038','KA03HJ1005',2021,'White',
    16000,799,'AVAILABLE',@loc_hebbal,
    '2023-09-10',328000.00,375000.00,5,5,
    '2021 Datsun Redi-Go T, ultra-lightweight city car with best fuel efficiency in class.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Kwid' AND variant='Rxt 0.8' LIMIT 1),
    'C2026DV0000000039','MH14CC1005',2022,'Fiery Red',
    9500,799,'AVAILABLE',@loc_elec_city,
    '2024-01-15',388000.00,435000.00,5,5,
    '2022 Renault Kwid Rxt 0.8, stylish entry segment hatchback with digital instrument cluster.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Renault' AND model='Kwid' AND variant='Climber 1.0 Mt' LIMIT 1),
    'C2026DV0000000040','AP09AA1005',2021,'Zanskar Blue',
    18500,999,'AVAILABLE',@loc_whitefield,
    '2023-07-30',428000.00,485000.00,5,5,
    '2021 Renault Kwid Climber, rugged crossover styling with raised ground clearance.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='V' LIMIT 1),
    'C2026DV0000000041','MH13BB1006',2021,'White',
    22000,1197,'AVAILABLE',@loc_mg_road,
    '2023-06-25',542000.00,612000.00,5,5,
    '2021 Toyota Etios Liva V, mid-variant hatchback with Toyota legendary reliability.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='Vxd' LIMIT 1),
    'C2026DV0000000042','KA03HJ1006',2020,'Silver Metallic',
    38000,1364,'AVAILABLE',@loc_hebbal,
    '2023-03-01',662000.00,742000.00,5,5,
    '2020 Toyota Etios Liva Vxd diesel, top-spec variant with excellent highway diesel economy.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Xcent Prime' AND variant='T' LIMIT 1),
    'C2026DV0000000043','MH14CC1006',2020,'Fiery Red',
    35000,1197,'AVAILABLE',@loc_elec_city,
    '2023-01-20',498000.00,562000.00,4,5,
    '2020 Hyundai Xcent Prime T, compact fleet-grade sedan with spacious rear cabin.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Hyundai' AND model='Santro' AND variant='Asta Mt' LIMIT 1),
    'C2026DV0000000044','AP09AA1006',2022,'Typhoon Silver',
    7000,1086,'RESERVED',@loc_whitefield,
    '2024-02-28',528000.00,595000.00,5,5,
    '2022 Hyundai Santro Asta top-spec, packed with 3 airbags, touchscreen and wireless charging.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Eeco' AND variant='5 Str' LIMIT 1),
    'C2026DV0000000045','MH13BB1007',2021,'White',
    28000,1193,'AVAILABLE',@loc_mg_road,
    '2023-05-12',332000.00,378000.00,5,5,
    '2021 Maruti Eeco 5-seater, practical family van with unmatched versatility and low running cost.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Tiago' AND variant='Revotron Xza' LIMIT 1),
    'C2026DV0000000046','KA03HJ1007',2022,'Flame Orange',
    5800,1199,'AVAILABLE',@loc_hebbal,
    '2024-03-10',588000.00,659000.00,5,5,
    '2022 Tata Tiago Xza AMT, near-new automatic hatchback with iRA connected car tech.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Tata' AND model='Altroz' AND variant='Xm Diesel' LIMIT 1),
    'C2026DV0000000047','MH14CC1007',2021,'Sonic Silver',
    24000,1497,'AVAILABLE',@loc_elec_city,
    '2023-10-05',735000.00,825000.00,5,5,
    '2021 Tata Altroz Xm diesel, premium diesel hatchback with superior NVH refinement.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Honda' AND model='Jazz' AND variant='Vx Petrol' LIMIT 1),
    'C2026DV0000000048','AP09AA1007',2020,'Premium White Pearl',
    31000,1199,'RESERVED',@loc_whitefield,
    '2023-02-20',737000.00,822000.00,5,5,
    '2020 Honda Jazz Vx, top-spec with Honda Sensing safety suite and moonroof, currently reserved.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Maruti Suzuki' AND model='Dzire' AND variant='Zdi' LIMIT 1),
    'C2026DV0000000049','MH13BB1008',2020,'Dignity Brown',
    41000,1248,'SOLD',@loc_mg_road,
    '2022-10-15',768000.00,852000.00,4,5,
    '2020 Maruti Dzire Zdi diesel top-spec, premium compact sedan with AMT option, sold as-is.'
  ),
  (
    (SELECT id FROM car_models WHERE make='Toyota' AND model='Etios Liva' AND variant='Vx' LIMIT 1),
    'C2026DV0000000050','KA03HJ1008',2021,'Platinum White Pearl',
    19000,1197,'RESERVED',@loc_hebbal,
    '2023-08-22',598000.00,672000.00,5,5,
    '2021 Toyota Etios Liva Vx, well-rounded hatchback with alloys and projector headlamps.'
  );

-- ── Recalculate car counts for all storage locations ─────────────────────────
-- (no DB triggers since V20 — counts managed in application layer)
UPDATE storage_locations sl
SET current_car_count = (
  SELECT COUNT(*) FROM cars c
  WHERE c.storage_location_id = sl.id AND c.status <> 'SOLD'
);

-- ── Insert PURCHASE financial transactions for all seeded cars ────────────────
-- Idempotent: skips rows where a PURCHASE transaction already exists.
INSERT INTO financial_transactions
  (car_id, motorcycle_id, vehicle_type, transaction_type, amount, transaction_date, description)
SELECT
  c.id,
  NULL,
  'CAR',
  'PURCHASE',
  c.purchase_price,
  CONCAT(c.purchase_date, ' 09:00:00'),
  CONCAT('Vehicle purchase - Car VIN: ', c.vin_number)
FROM cars c
WHERE c.vin_number LIKE 'C2026DV%'
  AND c.purchase_price IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM financial_transactions ft
    WHERE ft.car_id = c.id AND ft.transaction_type = 'PURCHASE'
  );

-- SALE financial transactions are created by seed_clients.sh
-- (sale_price = actual negotiated price, not the asking selling_price).

SQL

# ── Report ────────────────────────────────────────────────────
# Note: storage_locations.current_car_count and current_motorcycle_count are maintained
# at the application/service layer (no DB triggers since V20).
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
    CONCAT('  ', RPAD(sl.name, 20, ' '), (sl.current_car_count + sl.current_motorcycle_count), ' active vehicle(s)')
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
echo -e "${BOLD}  Financial transactions:${RESET}"
run_sql "
  SELECT CONCAT('  ', RPAD(ft.transaction_type, 12, ' '), COUNT(*), ' transaction(s)')
  FROM financial_transactions ft
  JOIN cars c ON c.id = ft.car_id
  WHERE c.vin_number LIKE '${VIN_PREFIX}%'
  GROUP BY ft.transaction_type
  ORDER BY ft.transaction_type;
"

echo ""
success "Done! Run scripts/seed_motorcycles.sh to seed motorcycles next."
echo ""
