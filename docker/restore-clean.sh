#!/bin/bash
set -e

BACKUP_DIR="./backups"

# Check if backup file is provided
if [ -z "$1" ]; then
    echo "Usage: ./restore-clean.sh <backup_file>"
    echo ""
    echo "This script performs a CLEAN restore by dropping and recreating the database."
    echo "Use this when you need to restore schema changes or start completely fresh."
    echo ""
    echo "⚠️  WARNING: This will disconnect all active database connections!"
    echo "   Your backend application will need to reconnect."
    echo ""
    echo "For a live restore (keeps connections), use: ./restore.sh"
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
echo "WARNING: CLEAN RESTORE - Will drop and recreate database!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Backup file: $BACKUP_FILE"
echo "Size:        $(du -h "$BACKUP_FILE" | cut -f1)"
echo ""
echo "⚠️  This will:"
echo "   • Disconnect ALL active database connections"
echo "   • Drop the entire database"
echo "   • Recreate it from scratch"
echo "   • Restore all data and schema"
echo ""
echo "Your backend application will automatically reconnect after restore."
echo ""
read -p "Are you sure you want to proceed? (yes/no): " confirm

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
echo "Terminating active connections..."
docker compose --env-file ../.env exec -T postgres psql -U current_user -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'current' AND pid <> pg_backend_pid();" > /dev/null 2>&1 || true

echo "Dropping and recreating database..."
docker compose --env-file ../.env exec -T postgres psql -U current_user -d postgres -c "DROP DATABASE IF EXISTS current;"
docker compose --env-file ../.env exec -T postgres psql -U current_user -d postgres -c "CREATE DATABASE current;"

echo ""
echo "Restoring from backup: $BACKUP_FILE"
docker compose --env-file ../.env exec -T postgres psql -U current_user -d current < "$BACKUP_FILE"

echo ""
echo "✓ Database restored successfully!"
echo ""
echo "Your database has been completely restored from:"
echo "  $BACKUP_FILE"
echo ""
echo "Your backend application will automatically reconnect on the next request."
echo ""
