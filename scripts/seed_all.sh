#!/usr/bin/env bash
# =============================================================
# seed_all.sh
# Master seed runner — executes all seed scripts in dependency order.
#
# Order:
#   1. seed_cars.sh        — 50 cars + PURCHASE financial transactions
#   2. seed_motorcycles.sh — 50 motorcycles + PURCHASE financial transactions
#   3. seed_clients.sh     — 6 clients + Sales records + SALE financial
#                            transactions + client purchase counters
#
# Usage (from repo root):
#   bash scripts/seed_all.sh
#
# Prerequisites:
#   - Docker must be running
#   - Container 'wheelshift-mysql' must be up
#   - Flyway migrations V1–V24 must have been applied
# =============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── Colour helpers ────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'

info()    { echo -e "${CYAN}  ℹ  $*${RESET}"; }
success() { echo -e "${GREEN}  ✔  $*${RESET}"; }
warn()    { echo -e "${YELLOW}  ⚠  $*${RESET}"; }
header()  { echo -e "\n${BOLD}$*${RESET}"; }

CONTAINER="wheelshift-mysql"
DB="wheelshift_db"
USER="wheelshift_user"
PASS="wheelshift_pass_2025"

run_sql() {
  docker exec -i "$CONTAINER" \
    mysql -u"$USER" -p"$PASS" --silent --skip-column-names "$DB" \
    -e "$1" 2>/dev/null
}

# ── Banner ────────────────────────────────────────────────────
echo ""
echo -e "${BOLD}╔════════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}║   WheelShift Pro — Master Seed Runner          ║${RESET}"
echo -e "${BOLD}║   Runs all seed scripts in dependency order     ║${RESET}"
echo -e "${BOLD}╚════════════════════════════════════════════════╝${RESET}"
echo ""

# ── Pre-flight ────────────────────────────────────────────────
info "Checking Docker container '$CONTAINER'..."
if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
  echo -e "${RED}  ✖  Container '$CONTAINER' is not running.${RESET}" >&2
  echo -e "${RED}     Start it with: docker compose -f docker-compose-dev.yml up -d mysql${RESET}" >&2
  exit 1
fi
success "Container is running."

START_TIME=$(date +%s)

# ── Step 1 — Cars ─────────────────────────────────────────────
header "━━━ [1/3] Seeding cars ────────────────────────────────────────"
bash "$SCRIPT_DIR/seed_cars.sh"

# ── Step 2 — Motorcycles ──────────────────────────────────────
header "━━━ [2/3] Seeding motorcycles ─────────────────────────────────"
bash "$SCRIPT_DIR/seed_motorcycles.sh"

# ── Step 3 — Clients + Sales ──────────────────────────────────
header "━━━ [3/3] Seeding clients, sales & financial transactions ─────"
bash "$SCRIPT_DIR/seed_clients.sh"

# ── Step 4 — Inquiries ────────────────────────────────────────
header "━━━ [4/4] Seeding inquiries ─────────────────────────────────"
bash "$SCRIPT_DIR/seed_inquiries.sh"

# ── Final summary ─────────────────────────────────────────────
END_TIME=$(date +%s)
ELAPSED=$(( END_TIME - START_TIME ))

echo ""
echo -e "${BOLD}╔════════════════════════════════════════════════╗${RESET}"
echo -e "${BOLD}║   MASTER SEED COMPLETE                         ║${RESET}"
echo -e "${BOLD}╚════════════════════════════════════════════════╝${RESET}"
echo ""

printf "  %-30s %s\n" "Cars:"                  "$(run_sql 'SELECT COUNT(*) FROM cars;')"
printf "  %-30s %s\n" "Motorcycles:"           "$(run_sql 'SELECT COUNT(*) FROM motorcycles;')"
printf "  %-30s %s\n" "Clients:"               "$(run_sql 'SELECT COUNT(*) FROM clients;')"
printf "  %-30s %s\n" "Sales:"                 "$(run_sql 'SELECT COUNT(*) FROM sales;')"
printf "  %-30s %s\n" "Financial transactions:" "$(run_sql 'SELECT COUNT(*) FROM financial_transactions;')"
printf "  %-30s %s\n" "  — PURCHASE:"          "$(run_sql "SELECT COUNT(*) FROM financial_transactions WHERE transaction_type='PURCHASE';")"
printf "  %-30s %s\n" "  — SALE:"              "$(run_sql "SELECT COUNT(*) FROM financial_transactions WHERE transaction_type='SALE';")"
printf "  %-30s %s\n" "Inquiries:"             "$(run_sql 'SELECT COUNT(*) FROM inquiries;')"

echo ""
success "All seed scripts completed in ${ELAPSED}s."
echo ""
