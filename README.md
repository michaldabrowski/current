# Current - finance landing page

A financial portfolio application playground.

![Current Logo](frontend/src/assets/logo.svg)

## Prerequisites

- **Java 25** (JVM toolchain)
- **Node.js 18+** (for frontend)
- **Docker & Docker Compose** (for PostgreSQL database)

## Quick Start

### 1. Configure Environment Variables

```bash
# Generate .env file with secure password
./setup-env.sh
```

This creates a `.env` file with database credentials used by both Docker Compose and Spring Boot.

### 2. Start PostgreSQL Database

```bash
cd docker
./start.sh
cd ..
```

This will start:
- **PostgreSQL 18** on `localhost:5432`
- **pgAdmin** web interface at `http://localhost:5050`

### 3. Build and Run Application

```bash
# Build entire project
./gradlew buildAll

# Run tests
./gradlew testAll

# Run backend (Spring Boot on port 8080)
./gradlew runBackend

# Run frontend (Vite dev server on port 5173)
./gradlew runFrontend
```

### 4. Access the Application

- **Frontend:** http://localhost:5173
- **Backend API:** http://localhost:8080
- **pgAdmin:** http://localhost:5050 (no login required in dev mode)

#### Connecting to PostgreSQL in pgAdmin

⚠️ **Important:** When connecting pgAdmin to PostgreSQL, use **`postgres`** as the hostname, NOT `localhost`!

**Connection Settings:**
- Host: **`postgres`** (Docker service name)
- Port: `5432`
- Database: `current`
- Username: `current_user`
- Password: See `.env` file

## Database Management

All database scripts are located in the `docker/` directory:

```bash
cd docker

# Start database
./start.sh

# Stop database (data persists)
./stop.sh

# View logs
./logs.sh

# Create backup
./backup.sh

# Restore from backup
./restore.sh ./backups/postgres_backup_YYYYMMDD_HHMMSS.sql

# Clean restore (drops database, disconnects clients)
./restore-clean.sh ./backups/postgres_backup_YYYYMMDD_HHMMSS.sql

# Export seed data (data only, for demo purposes)
./export-seed.sh

# Load seed data
./seed.sh ./seed-data.sql

# Reset database (WARNING: deletes all data)
./reset.sh
```

For detailed database documentation, see [docker/postgres/README.md](docker/postgres/README.md)

## Database Connection

**Environment Variables:**

The application uses a single `.env` file in the project root with PostgreSQL standard variables:
- `POSTGRES_DB` - Database name
- `POSTGRES_USER` - Database username  
- `POSTGRES_PASSWORD` - Database password
- `POSTGRES_PORT` - Database port

These variables are used by:
- **Docker Compose** - To configure PostgreSQL container
- **Spring Boot** - To connect to the database

**Connection Details by Context:**

| Context | Host | Port | Why? |
|---------|------|------|------|
| **Spring Boot (from host)** | `localhost` | `5432` | Port is published to host |
| **pgAdmin (from container)** | `postgres` | `5432` | Use Docker service name |
| **psql (from host)** | `localhost` | `5432` | Port is published to host |

⚠️ **Common Mistake:** When using pgAdmin, you must use `postgres` as the hostname because pgAdmin runs inside a Docker container. Using `localhost` will fail!

**PostgreSQL Connection (from host machine):**
- Host: `localhost`
- Port: `5432` (from `POSTGRES_PORT`)
- Database: `current` (from `POSTGRES_DB`)
- Username: `current_user` (from `POSTGRES_USER`)
- Password: See `.env` file (`POSTGRES_PASSWORD`)

**JDBC URL (auto-constructed):**
```
jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}
```

**First Time Setup:**
```bash
# Generate .env with secure random password
./setup-env.sh
```

## Project Structure

```
current/
├── backend/          # Spring Boot Kotlin backend
├── frontend/         # Vue.js frontend
├── docker/           # Docker Compose & database management
│   ├── postgres/     # PostgreSQL documentation
│   ├── *.sh          # Helper scripts
│   └── .env          # Database configuration
├── gradle/           # Gradle wrapper
└── build.gradle.kts  # Root build configuration
```

## Development Workflow

```bash
# 0. First time setup (generates .env with secure password)
./setup-env.sh

# 1. Start database
cd docker && ./start.sh && cd ..

# 2. Run backend in one terminal (loads credentials from .env)
./gradlew runBackend

# 3. Run frontend in another terminal
./gradlew runFrontend

# 4. Develop and test
# Backend auto-reloads on changes (Spring DevTools)
# Frontend auto-reloads on changes (Vite HMR)

# 5. Stop database when done
cd docker && ./stop.sh
```

## Testing

```bash
# Run all tests
./gradlew testAll

# Run backend tests only
./gradlew :backend:test

# Run with coverage
./gradlew :backend:test --info
```

## Production Build

```bash
# Build everything (includes frontend bundling)
./gradlew buildAll

# Run with production profile
./gradlew :backend:bootRun --args='--spring.profiles.active=production'
```

## Data Persistence

Database data is stored in Docker volumes and persists between restarts:
- `current-postgres-data` - PostgreSQL database files
- `current-pgadmin-data` - pgAdmin configuration

Data survives:
- Container restarts (`docker compose restart`)
- Container removal (`docker compose down`)

Data is deleted only when:
- Explicitly running `./reset.sh`
- Running `docker compose down -v` (removes volumes)

## Troubleshooting

### Port Conflicts

If ports 5432 or 5050 are already in use:
1. Edit `docker/.env`
2. Change `POSTGRES_PORT` or `PGADMIN_PORT`
3. Restart: `cd docker && ./stop.sh && ./start.sh`

### Database Connection Issues

```bash
# Check if containers are running
cd docker && docker compose ps

# View PostgreSQL logs
./logs.sh postgres

# Verify database is ready
docker compose exec postgres pg_isready -U current_user -d current
```

### Reset Everything

```bash
# Reset database (deletes all data)
cd docker && ./reset.sh

# Clean build
./gradlew clean buildAll
```

## License

See [LICENSE](LICENSE) file for details.
