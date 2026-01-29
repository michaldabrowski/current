#!/bin/bash

if [ -z "$1" ]; then
    echo "Showing logs for all services (ctrl+c to exit)..."
    echo ""
    docker compose --env-file ../.env logs -f
else
    echo "Showing logs for $1 (ctrl+c to exit)..."
    echo ""
    docker compose --env-file ../.env logs -f "$1"
fi
