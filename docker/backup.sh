#!/bin/bash
set -e

BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/postgres_backup_${TIMESTAMP}.sql"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

echo "Creating PostgreSQL backup..."
echo ""

# Check if PostgreSQL is running
if ! docker compose --env-file ../.env ps postgres | grep -q "Up"; then
    echo "Error: PostgreSQL container is not running."
    echo "Start it with: ./start.sh"
    exit 1
fi

# Create backup
docker compose --env-file ../.env exec -T postgres pg_dump -U current_user -d current > "$BACKUP_FILE"

# Check if backup was successful
if [ -s "$BACKUP_FILE" ]; then
    echo "✓ Backup created successfully!"
    echo ""
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "Backup Details:"
    echo "  File:      $BACKUP_FILE"
    echo "  Size:      $(du -h "$BACKUP_FILE" | cut -f1)"
    echo "  Timestamp: $(date)"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo ""
    echo "To restore this backup:"
    echo "  ./restore.sh $BACKUP_FILE"
    echo ""
else
    echo "Error: Backup file is empty or backup failed."
    rm -f "$BACKUP_FILE"
    exit 1
fi
