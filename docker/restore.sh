#!/bin/bash
set -e

BACKUP_DIR="./backups"

# Check if backup file is provided
if [ -z "$1" ]; then
    echo "Usage: ./restore.sh <backup_file>"
    echo ""
    echo "Available backups:"
    if [ -d "$BACKUP_DIR" ] && [ -n "$(ls -A $BACKUP_DIR/*.sql 2>/dev/null)" ]; then
        ls -lh "$BACKUP_DIR"/*.sql 2>/dev/null | awk '{print "  " $9 " (" $5 ", " $6 " " $7 " " $8 ")"}'
    else
        echo "  No backups found in $BACKUP_DIR"
    fi
    echo ""
    exit 1
fi

BACKUP_FILE="$1"

# Check if backup file exists
if [ ! -f "$BACKUP_FILE" ]; then
    echo "Error: Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "WARNING: This will REPLACE ALL current database data!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Backup file: $BACKUP_FILE"
echo "Size:        $(du -h "$BACKUP_FILE" | cut -f1)"
echo ""
read -p "Are you sure you want to restore? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo ""
    echo "Restore cancelled."
    exit 0
fi

echo ""
echo "Checking if PostgreSQL is running..."
if ! docker compose --env-file ../.env ps postgres | grep -q "Up"; then
    echo "PostgreSQL is not running. Starting it now..."
    docker compose --env-file ../.env up -d postgres
    
    echo "Waiting for PostgreSQL to be ready..."
    timeout=30
    elapsed=0
    while [ $elapsed -lt $timeout ]; do
        if docker compose --env-file ../.env exec -T postgres pg_isready -U current_user -d current > /dev/null 2>&1; then
            break
        fi
        sleep 1
        elapsed=$((elapsed + 1))
    done
fi

echo ""
echo "Preparing data-only restore..."

# Create temporary file with only data statements (no schema)
TEMP_DATA_FILE=$(mktemp)
trap "rm -f $TEMP_DATA_FILE" EXIT

# Extract data sections from backup
# This includes: COPY statements, the data lines, INSERT statements, and SET statements needed for data import
awk '
    /^COPY / { print; copying=1; next }
    /^\\.$/ { if (copying) { print; copying=0 } next }
    copying { print; next }
    /^INSERT / { print; next }
    /^SET standard_conforming_strings/ { print; next }
    /^SET search_path/ { print; next }
' "$BACKUP_FILE" > "$TEMP_DATA_FILE"

# Check if we got any data
DATA_LINE_COUNT=$(wc -l < "$TEMP_DATA_FILE" | tr -d ' ')
if [ "$DATA_LINE_COUNT" -lt 1 ]; then
    echo ""
    echo "⚠️  Warning: No data found in backup file."
    echo "   The backup appears to be from an empty database."
    echo ""
    read -p "Continue anyway? (yes/no): " continue_empty
    if [ "$continue_empty" != "yes" ]; then
        echo "Restore cancelled."
        exit 0
    fi
fi

echo "Clearing existing data (keeping connections alive)..."
# Truncate all tables in the public schema (preserves connections)
docker compose --env-file ../.env exec -T postgres psql -U current_user -d current -c "
DO \$\$ 
DECLARE 
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE';
    END LOOP;
END \$\$;
" 2>/dev/null || true

if [ "$DATA_LINE_COUNT" -gt 0 ]; then
    echo ""
    echo "Restoring data from backup: $BACKUP_FILE"
    docker compose --env-file ../.env exec -T postgres psql -U current_user -d current < "$TEMP_DATA_FILE"
else
    echo ""
    echo "Skipping data import (backup is empty)."
fi

echo ""
echo "✓ Database restored successfully!"
echo ""
echo "Your database has been restored from:"
echo "  $BACKUP_FILE"
echo ""
