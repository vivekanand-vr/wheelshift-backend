#!/usr/bin/env bash
# =============================================================
# seed_clients.sh
# Seeds 6 additional clients into the clients table via Docker exec.
#
# Usage (from repo root):
#   bash scripts/seed_clients.sh
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

# ── Existing rows check ────────────────────────────────────────
EXISTING=$(run_sql "SELECT COUNT(*) FROM clients WHERE email LIKE '%@example.com' AND email REGEXP 'rahul|priya|vikram|anjali|mohammed|pooja';")
if [ "$EXISTING" -gt 0 ]; then
  warn "$EXISTING seed client(s) already present — INSERT IGNORE will skip duplicates."
fi

# ── Seed SQL ──────────────────────────────────────────────────
info "Seeding 6 clients..."

docker exec -i "$CONTAINER" \
  mysql -u"$USER" -p"$PASS" "$DB" 2>/dev/null <<'SQL'

INSERT IGNORE INTO clients
  (name, email, phone, location, status, total_purchases, last_purchase)
VALUES
  ('Rahul Sharma',    'rahul.sharma@example.com',    '+91-98765-11111', 'Mumbai',    'ACTIVE', 1, '2025-08-14'),
  ('Priya Patel',     'priya.patel@example.com',     '+91-98765-22222', 'Delhi',     'ACTIVE', 0, NULL),
  ('Vikram Singh',    'vikram.singh@example.com',    '+91-98765-33333', 'Bangalore', 'ACTIVE', 2, '2025-11-30'),
  ('Anjali Gupta',    'anjali.gupta@example.com',    '+91-98765-44444', 'Chennai',   'ACTIVE', 0, NULL),
  ('Mohammed Rizwan', 'mohammed.rizwan@example.com', '+91-98765-55555', 'Hyderabad', 'ACTIVE', 1, '2025-06-22'),
  ('Pooja Nair',      'pooja.nair@example.com',      '+91-98765-66666', 'Pune',      'ACTIVE', 3, '2026-01-10');

SQL

# ── Report ────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  SEED REPORT — CLIENTS${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"

TOTAL=$(run_sql "SELECT COUNT(*) FROM clients;")
ACTIVE=$(run_sql "SELECT COUNT(*) FROM clients WHERE status='ACTIVE';")
WITH_PURCHASES=$(run_sql "SELECT COUNT(*) FROM clients WHERE total_purchases > 0;")

printf "  %-25s %s\n" "Total clients in DB:"    "$TOTAL"
printf "  %-25s %s\n" "Active:"                  "$ACTIVE"
printf "  %-25s %s\n" "Have purchase history:"   "$WITH_PURCHASES"

echo ""
echo -e "${BOLD}  All clients:${RESET}"
run_sql "
  SELECT CONCAT(
    '  ', RPAD(name, 22, ' '),
    RPAD(location, 12, ' '),
    total_purchases, ' purchase(s)'
  )
  FROM clients
  ORDER BY id;
"

echo ""
success "All done — clients seeded successfully."
echo ""
