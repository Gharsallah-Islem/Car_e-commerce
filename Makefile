# ==============================================================================
# AutoParts Store - Docker Commands
# ==============================================================================
# Usage: Run these commands from the project root directory
# ==============================================================================

# ------------------------------------------------------------------------------
# QUICK START
# ------------------------------------------------------------------------------

# Build and start all services
docker-start:
	docker-compose up -d --build

# Stop all services
docker-stop:
	docker-compose down

# View logs (all services)
docker-logs:
	docker-compose logs -f

# View logs for specific service
docker-logs-backend:
	docker-compose logs -f backend

docker-logs-frontend:
	docker-compose logs -f frontend

docker-logs-ai:
	docker-compose logs -f ai-module

docker-logs-db:
	docker-compose logs -f postgres

# ------------------------------------------------------------------------------
# BUILD COMMANDS
# ------------------------------------------------------------------------------

# Build all images (no cache)
docker-build-fresh:
	docker-compose build --no-cache

# Build individual services
docker-build-backend:
	docker-compose build backend

docker-build-frontend:
	docker-compose build frontend

docker-build-ai:
	docker-compose build ai-module

# ------------------------------------------------------------------------------
# MANAGEMENT COMMANDS
# ------------------------------------------------------------------------------

# Start with pgAdmin (for database management)
docker-start-with-tools:
	docker-compose --profile tools up -d

# Restart a service
docker-restart-backend:
	docker-compose restart backend

docker-restart-frontend:
	docker-compose restart frontend

docker-restart-ai:
	docker-compose restart ai-module

# Check service health
docker-health:
	docker-compose ps

# ------------------------------------------------------------------------------
# CLEANUP COMMANDS
# ------------------------------------------------------------------------------

# Stop and remove containers, networks
docker-clean:
	docker-compose down

# Stop and remove everything including volumes (DATA LOSS!)
docker-clean-all:
	docker-compose down -v

# Remove unused Docker resources
docker-prune:
	docker system prune -f

# ------------------------------------------------------------------------------
# DATABASE COMMANDS
# ------------------------------------------------------------------------------

# Access PostgreSQL CLI
docker-db-shell:
	docker-compose exec postgres psql -U lasmer -d ecommercespareparts

# Backup database
docker-db-backup:
	docker-compose exec postgres pg_dump -U lasmer ecommercespareparts > backup_$$(date +%Y%m%d_%H%M%S).sql

# ------------------------------------------------------------------------------
# DEVELOPMENT HELPERS
# ------------------------------------------------------------------------------

# Shell into containers
docker-shell-backend:
	docker-compose exec backend sh

docker-shell-frontend:
	docker-compose exec frontend sh

docker-shell-ai:
	docker-compose exec ai-module sh
