#!/bin/bash
set -e

SEED_FILE="${1:-./seed-data.sql}"

echo "Loading seed data into PostgreSQL..."
echo ""

# Check if PostgreSQL is running
if ! docker compose --env-file ../.env ps postgres | grep -q "Up"; then
    echo "Error: PostgreSQL container is not running."
    echo "Start it with: ./start.sh"
    exit 1
fi

# Check if seed file exists
if [ ! -f "$SEED_FILE" ]; then
    echo "Error: Seed file not found: $SEED_FILE"
    echo ""
    echo "Usage:"
    echo "  ./seed.sh [path/to/seed-data.sql]"
    echo ""
    echo "To create seed data:"
    echo "  1. Add some demo data through your application"
    echo "  2. Run: ./backup.sh"
    echo "  3. Copy backup to: cp ./backups/postgres_backup_*.sql ./seed-data.sql"
    echo "  4. Run: ./seed.sh"
    exit 1
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Seed Data File: $SEED_FILE"
echo "Database:       current"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Import seed data
docker compose --env-file ../.env exec -T postgres psql -U current_user -d current < "$SEED_FILE"

echo ""
echo "✓ Seed data loaded successfully!"
echo ""
