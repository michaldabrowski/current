# PostgreSQL Database Management

## Quick Start

```bash
# Start database and pgAdmin
cd docker
./start.sh

# Stop services (data persists)
./stop.sh

# View logs
./logs.sh

# Reset database (WARNING: deletes all data)
./reset.sh
```

## Connection Information

### PostgreSQL Database

- **Host:** localhost
- **Port:** 5432
- **Database:** current
- **Username:** current_user
- **Password:** See `.env` file in project root

**JDBC URL:**
```
jdbc:postgresql://localhost:5432/current
```

### pgAdmin Web Interface

- **URL:** http://localhost:5050
- **Login:** Not required (dev mode - authentication disabled)

**To connect to PostgreSQL in pgAdmin:**
1. Right-click "Servers" → Register → Server
2. **General:** Name = `Current Local`
3. **Connection:**
   - Host: `postgres` ⚠️ **Use `postgres`, NOT `localhost`**
   - Port: `5432`
   - Database: `current`
   - Username: `current_user`
   - Password: See `.env` file in project root
4. Save

## Common Operations

### Seed Data (Demo/Development)

**Create seed data from current database:**
```bash
# 1. Add demo data through your application or pgAdmin
# 2. Export data to seed file (data only, no schema)
./export-seed.sh

# This creates: docker/seed/seed-data_YYYYMMDD_HHMMSS.sql
```

**Load seed data into database:**
```bash
# Load from specific seed file
./seed.sh ./seed/seed-data_20260129_120000.sql

# Or copy to default location and load
cp ./seed/seed-data_*.sql ./seed-data.sql
./seed.sh
```

**Typical workflow for new developers:**
```bash
# 1. Reset database to clean state
./reset.sh

# 2. Start backend (creates schema via Hibernate)
cd ..
./gradlew runBackend
# Wait for application to start, then stop it (Ctrl+C)

# 3. Load demo data
cd docker
./seed.sh ./seed-data.sql
```

### Connect via Command Line

```bash
# Connect to PostgreSQL using psql
cd docker
docker compose exec postgres psql -U current_user -d current

# List all tables
\dt

# Describe a table
\d account
\d transaction

# Exit psql
\q
```

### View Logs

```bash
# All services
./logs.sh

# PostgreSQL only
./logs.sh postgres

# pgAdmin only
./logs.sh pgadmin
```

### Backup Database

```bash
# Create backup
./backup.sh

# Backups are stored in: docker/backups/postgres_backup_YYYYMMDD_HHMMSS.sql
```

### Restore Database

```bash
# List available backups
./restore.sh

# Restore from specific backup (keeps connections alive)
./restore.sh ./backups/postgres_backup_20240129_120000.sql

# Clean restore (drops/recreates database, disconnects all clients)
./restore-clean.sh ./backups/postgres_backup_20240129_120000.sql
```

**Two restore modes:**

- **`./restore.sh`** - Live restore (recommended)
  - Truncates tables and restores data
  - Keeps database connections alive
  - Backend stays connected
  - Use for data-only restores

- **`./restore-clean.sh`** - Clean restore
  - Drops and recreates database
  - Terminates all connections
  - Backend auto-reconnects
  - Use when restoring schema changes

### Reset Database

```bash
# WARNING: This deletes ALL data!
./reset.sh
```

This will:
1. Stop the containers
2. Delete all volumes (including data)
3. Start fresh containers with empty database

## Using pgAdmin

1. Open http://localhost:5050 in your browser
2. Login with credentials above
3. Add server connection:
   - **Right-click "Servers" → Register → Server**
   - **General tab:**
     - Name: Current Local
   - **Connection tab:**
     - Host: postgres (use the service name, not localhost)
     - Port: 5432
     - Database: current
     - Username: current_user
     - Password: (from docker/.env file)
   - **Save**

## Volume Management

PostgreSQL data is stored in a Docker named volume for persistence.

### View Volume Information

```bash
# List volumes
docker volume ls | grep current

# Inspect volume
docker volume inspect current-postgres-data
```

### Backup Volume (Alternative Method)

```bash
# Create tarball backup of volume
docker run --rm \
  -v current-postgres-data:/data \
  -v $(pwd):/backup \
  ubuntu tar czf /backup/postgres-volume-backup.tar.gz -C /data .
```

### Restore Volume (Alternative Method)

```bash
# Restore from tarball
docker run --rm \
  -v current-postgres-data:/data \
  -v $(pwd):/backup \
  ubuntu tar xzf /backup/postgres-volume-backup.tar.gz -C /data
```

### Delete Volume (Clean Slate)

```bash
# Stop containers and remove volumes
docker compose down -v
```

## Troubleshooting

### Port Already in Use

If port 5432 or 5050 is already in use:

1. Edit `docker/.env`
2. Change `POSTGRES_PORT` or `PGADMIN_PORT`
3. Restart: `./stop.sh && ./start.sh`

### Cannot Connect to Database

```bash
# Check if container is running
docker compose ps

# Check container logs
./logs.sh postgres

# Verify database is ready
docker compose exec postgres pg_isready -U current_user -d current
```

### Permission Denied on Scripts

```bash
# Make scripts executable
chmod +x *.sh
```

### Reset Everything

```bash
# Stop and remove everything
docker compose down -v

# Remove containers
docker rm -f current-postgres current-pgadmin

# Remove volumes
docker volume rm current-postgres-data current-pgadmin-data

# Start fresh
./start.sh
```

## Database Schema

The database schema is managed by Spring Boot JPA with `ddl-auto: update`.

Tables are automatically created when you run the backend application:
- `account` - User accounts
- `transaction` - Financial transactions

## Security Notes

- Database password is stored in `docker/.env` (gitignored)
- Default pgAdmin credentials should be changed for production
- PostgreSQL is only accessible on localhost (not exposed externally)
- For production deployments, use the `production` Spring profile with environment variables

## Production Deployment

For production, use environment variables instead of the `.env` file:

```bash
export DATABASE_URL=jdbc:postgresql://prod-host:5432/current
export DATABASE_USERNAME=current_user
export DATABASE_PASSWORD=secure_production_password

# Run with production profile
./gradlew bootRun --args='--spring.profiles.active=production'
```

## Additional Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [pgAdmin Documentation](https://www.pgadmin.org/docs/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
