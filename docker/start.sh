#!/bin/bash
set -e

echo "Starting Current PostgreSQL and pgAdmin..."
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

if [ $elapsed -ge $timeout ]; then
    echo "Warning: PostgreSQL health check timed out"
else
    echo "PostgreSQL is ready!"
fi

echo ""
echo "✓ Services started successfully!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "PostgreSQL Database:"
echo "  Host:     localhost"
echo "  Port:     5432"
echo "  Database: current"
echo "  User:     current_user"
echo "  Password: (see .env file)"
echo ""
echo "pgAdmin Web Interface:"
echo "  URL:      http://localhost:5050"
echo "  Email:    admin@example.com"
echo "  Password: admin"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Useful commands:"
echo "  View logs:      ./logs.sh"
echo "  Stop services:  ./stop.sh"
echo "  Reset database: ./reset.sh"
echo "  Backup:         ./backup.sh"
echo ""
