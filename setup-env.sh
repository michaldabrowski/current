#!/bin/bash
set -e

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Current Finance App - Environment Setup"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Check if .env already exists
if [ -f ".env" ]; then
    echo "✓ .env file already exists"
    echo ""
    echo "If you need to regenerate it, delete .env and run this script again."
    exit 0
fi

# Generate a secure random password
echo "Generating secure database password..."
DB_PASSWORD=$(openssl rand -base64 32 | tr -d "=+/" | cut -c1-32)

# Create .env file
cat > .env << EOF
# Database Configuration
# Used by both Docker Compose and Spring Boot application

# PostgreSQL Database
POSTGRES_DB=current
POSTGRES_USER=current_user
POSTGRES_PASSWORD=$DB_PASSWORD
POSTGRES_PORT=5432

# pgAdmin Configuration
PGADMIN_DEFAULT_EMAIL=admin@example.com
PGADMIN_DEFAULT_PASSWORD=admin
PGADMIN_PORT=5050
EOF

echo "✓ Created .env file with secure password"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Environment Setup Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "Database Configuration:"
echo "  Host:     localhost"
echo "  Port:     5432"
echo "  Database: current"
echo "  Username: current_user"
echo "  Password: $DB_PASSWORD"
echo ""
echo "Next steps:"
echo "  1. Start database:  cd docker && ./start.sh"
echo "  2. Run backend:     ./gradlew runBackend"
echo "  3. Run frontend:    ./gradlew runFrontend"
echo ""
