#!/bin/bash
set -e

SEED_DIR="./seed"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
SEED_FILE="${SEED_DIR}/seed-data_${TIMESTAMP}.sql"

# Create seed directory if it doesn't exist
mkdir -p "$SEED_DIR"

echo "Creating seed data file (data only, no schema)..."
echo ""

# Check if PostgreSQL is running
if ! docker compose --env-file ../.env ps postgres | grep -q "Up"; then
    echo "Error: PostgreSQL container is not running."
    echo "Start it with: ./start.sh"
    exit 1
fi

# Export data only (no CREATE TABLE statements)
docker compose --env-file ../.env exec -T postgres pg_dump \
    -U current_user \
    -d current \
    --data-only \
    --column-inserts \
    > "$SEED_FILE"

# Check if export was successful
if [ -s "$SEED_FILE" ]; then
    echo "✓ Seed data exported successfully!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Seed Data Details:"
    echo "  File:      $SEED_FILE"
    echo "  Size:      $(du -h "$SEED_FILE" | cut -f1)"
    echo "  Format:    Data only (no schema)"
    echo "  Timestamp: $(date)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "This file contains INSERT statements for demo/seed data."
    echo ""
    echo "To use this seed data:"
    echo "  1. Reset database:        ./reset.sh"
    echo "  2. Start application:     ../gradlew runBackend"
    echo "     (This creates the schema)"
    echo "  3. Load seed data:        ./seed.sh $SEED_FILE"
    echo ""
    echo "Or copy it to seed-data.sql for default seed file:"
    echo "  cp $SEED_FILE ./seed-data.sql"
    echo "  ./seed.sh"
    echo ""
else
    echo "Error: Seed data export failed or database is empty."
    rm -f "$SEED_FILE"
    exit 1
fi
