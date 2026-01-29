#!/bin/bash
set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "WARNING: This will DELETE ALL database data!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
read -p "Are you sure you want to reset the database? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo ""
    echo "Reset cancelled."
    exit 0
fi

echo ""
echo "Stopping services and removing volumes..."
docker compose --env-file ../.env down -v

echo ""
echo "Starting fresh services..."
docker compose --env-file ../.env up -d

echo ""
echo "Waiting for PostgreSQL to be healthy..."
timeout=30
elapsed=0
while [ $elapsed -lt $timeout ]; do
    if docker compose --env-file ../.env exec -T postgres pg_isready -U current_user -d current > /dev/null 2>&1; then
        break
    fi
    sleep 1
    elapsed=$((elapsed + 1))
done

echo ""
echo "✓ Database has been reset!"
echo ""
echo "All data has been permanently deleted."
echo "A fresh empty database is now running."
echo ""
