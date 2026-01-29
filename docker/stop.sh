#!/bin/bash
set -e

echo "Stopping Current PostgreSQL and pgAdmin..."
docker compose --env-file ../.env down

echo ""
echo "✓ Services stopped successfully!"
echo ""
echo "Note: Database data is preserved in volumes."
echo ""
echo "To start again:        ./start.sh"
echo "To remove all data:    ./reset.sh"
echo ""
