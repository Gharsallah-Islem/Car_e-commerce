# Deployment Documentation

> Environment setup and deployment guide for the AutoParts Store platform

## Table of Contents
- [Prerequisites](#prerequisites)
- [Development Setup](#development-setup)
- [Docker Deployment](#docker-deployment)
- [Production Deployment](#production-deployment)
- [Environment Variables](#environment-variables)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java JDK** | 21 | Backend runtime |
| **Node.js** | 18+ | Frontend build |
| **Python** | 3.10+ | AI module |
| **PostgreSQL** | 15+ | Database |
| **Maven** | 3.9+ | Java build |
| **npm** | 9+ | Node packages |
| **Git** | 2.40+ | Version control |
| **Docker** | 24+ | Containerization (optional) |
| **Docker Compose** | 2.20+ | Multi-container (optional) |

### System Requirements

| Environment | RAM | CPU | Storage |
|-------------|-----|-----|---------|
| **Development** | 8 GB | 4 cores | 20 GB |
| **Production** | 16 GB | 8 cores | 100 GB |

---

## Development Setup

### 1. Clone Repository

```bash
git clone https://github.com/Gharsallah-Islem/Car_e-commerce.git
cd Car_e-commerce
```

### 2. Database Setup

**Install PostgreSQL and create database:**

```bash
# Windows (with PostgreSQL installed)
psql -U postgres

# Create database
CREATE DATABASE ecommercespareparts;
CREATE USER lasmer WITH PASSWORD 'lasmer';
GRANT ALL PRIVILEGES ON DATABASE ecommercespareparts TO lasmer;
\q
```

### 3. Backend Setup

```bash
cd Backend

# Build project
mvn clean install -DskipTests

# Run application
mvn spring-boot:run
```

**Backend runs on:** `http://localhost:8080`

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

### 4. AI Module Setup

```bash
cd ai-module

# Create virtual environment
python -m venv venv

# Activate (Windows)
.\venv\Scripts\activate

# Activate (Linux/Mac)
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run server
python -m uvicorn src.api.main:app --port 5000 --reload
```

**AI Module runs on:** `http://localhost:5000`

**API Docs:** `http://localhost:5000/docs`

### 5. Frontend Setup

```bash
cd frontend-web

# Install dependencies
npm install

# Run development server
ng serve
# or
npm run start
```

**Frontend runs on:** `http://localhost:4200`

### 6. Mobile Setup (Android)

1. Open `mobile-app/CarPartsEcom` in Android Studio
2. Wait for Gradle sync
3. Update `Constants.kt` with your backend URL:
   - Emulator: `http://10.0.2.2:8080/api/`
   - Physical device: `http://YOUR_PC_IP:8080/api/`
4. Run on device/emulator

---

## Docker Deployment

### Quick Start

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Services Started

| Service | Port | URL |
|---------|------|-----|
| **Frontend** | 4200 | http://localhost:4200 |
| **Backend** | 8080 | http://localhost:8080 |
| **AI Module** | 5000 | http://localhost:5000 |
| **PostgreSQL** | 5432 | localhost:5432 |
| **Redis** | 6379 | localhost:6379 |
| **pgAdmin** | 5050 | http://localhost:5050 |

### Docker Compose Configuration

```yaml
# docker-compose.yml (key sections)
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: car-ecommerce-db
    environment:
      POSTGRES_DB: ecommercespareparts
      POSTGRES_USER: lasmer
      POSTGRES_PASSWORD: lasmer
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U lasmer -d ecommercespareparts"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: car-ecommerce-backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ecommercespareparts
      SPRING_DATASOURCE_USERNAME: lasmer
      SPRING_DATASOURCE_PASSWORD: lasmer
      JWT_SECRET: ${JWT_SECRET:-your-super-secret-jwt-key}
      AI_MODULE_URL: http://ai-module:5000
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend-web
      dockerfile: Dockerfile
    container_name: car-ecommerce-frontend
    ports:
      - "4200:80"
    depends_on:
      - backend

  ai-module:
    build:
      context: ./ai-module
      dockerfile: Dockerfile
    container_name: car-ecommerce-ai
    ports:
      - "5000:5000"
    volumes:
      - ai_models:/app/models

  redis:
    image: redis:7-alpine
    container_name: car-ecommerce-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: car-ecommerce-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@carecommerce.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    profiles:
      - tools

volumes:
  postgres_data:
  redis_data:
  ai_models:
```

### Building Individual Images

```bash
# Backend
cd Backend
docker build -t car-ecommerce-backend .

# Frontend
cd frontend-web
docker build -t car-ecommerce-frontend .

# AI Module
cd ai-module
docker build -t car-ecommerce-ai .
```

---

## Production Deployment

### Environment Configuration

Create `.env` file for production:

```bash
# Database
DB_HOST=your-db-host.com
DB_PORT=5432
DB_NAME=ecommercespareparts
DB_USER=production_user
DB_PASSWORD=strong_password_here

# JWT
JWT_SECRET=generate-a-long-random-secret-at-least-256-bits

# Stripe
STRIPE_API_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Google OAuth
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# AI Module
AI_MODULE_URL=http://ai-module:5000

# Frontend URL (for email links)
FRONTEND_URL=https://your-domain.com
```

### Production Checklist

- [ ] Change all default passwords
- [ ] Generate secure JWT secret (256+ bits)
- [ ] Use production Stripe keys
- [ ] Configure real SMTP credentials
- [ ] Set up SSL/TLS certificates
- [ ] Configure proper CORS origins
- [ ] Enable rate limiting
- [ ] Set up logging and monitoring
- [ ] Configure database backups
- [ ] Test all integrations

### Nginx Configuration (Reverse Proxy)

```nginx
# /etc/nginx/sites-available/car-ecommerce
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /etc/letsencrypt/live/your-domain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain.com/privkey.pem;

    # Frontend
    location / {
        proxy_pass http://localhost:4200;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }

    # AI Module (internal only or secured)
    location /ai-api {
        proxy_pass http://localhost:5000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
    }
}
```

---

## Environment Variables

### Backend (application.properties / Environment)

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/ecommercespareparts` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `lasmer` |
| `JWT_SECRET` | JWT signing key | (configured in properties) |
| `JWT_EXPIRATION` | Token expiry (ms) | `3600000` |
| `STRIPE_API_KEY` | Stripe secret key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook secret | - |
| `AI_MODULE_URL` | AI module base URL | `http://localhost:5000` |
| `SPRING_MAIL_USERNAME` | SMTP username | - |
| `SPRING_MAIL_PASSWORD` | SMTP password | - |
| `APP_FRONTEND_URL` | Frontend URL for emails | `http://localhost:4200` |

### Frontend (environment.ts)

| Variable | Description | Default |
|----------|-------------|---------|
| `apiUrl` | Backend API URL | `http://localhost:8080/api` |
| `wsUrl` | WebSocket URL | `ws://localhost:8080/ws` |
| `stripePublicKey` | Stripe publishable key | `pk_test_...` |

### AI Module (.env)

| Variable | Description | Default |
|----------|-------------|---------|
| `API_HOST` | Server host | `0.0.0.0` |
| `API_PORT` | Server port | `5000` |
| `MODEL_PATH` | Model files directory | `models/` |
| `BACKEND_URL` | Spring Boot URL | `http://localhost:8080` |
| `ALLOWED_ORIGINS` | CORS origins | `http://localhost:4200,http://localhost:8080` |

---

## Troubleshooting

### Common Issues

#### Database Connection Failed

```
Error: Connection refused to localhost:5432
```

**Solution:**
1. Ensure PostgreSQL is running
2. Check database credentials
3. Verify port is not blocked

```bash
# Check PostgreSQL status (Windows)
pg_isready -U postgres

# Restart PostgreSQL (Windows Services)
net stop postgresql-x64-15
net start postgresql-x64-15
```

#### Port Already in Use

```
Error: Port 8080 is already in use
```

**Solution:**
```bash
# Find process using port (Windows)
netstat -ano | findstr :8080

# Kill process
taskkill /PID <PID> /F
```

#### AI Module Model Not Found

```
Error: Model not found at models/baseline_model.h5
```

**Solution:**
1. Ensure model file exists in `ai-module/models/`
2. Download pre-trained model from releases
3. Check file permissions

#### CORS Errors

```
Error: CORS policy: No 'Access-Control-Allow-Origin' header
```

**Solution:**
1. Check WebConfig.java CORS settings
2. Verify allowed origins include frontend URL
3. For development, all localhost variants should be allowed

#### JWT Token Expired

```
Error: JWT token has expired
```

**Solution:**
1. Clear browser storage and login again
2. Increase token expiration in application.properties
3. Implement refresh token mechanism

### Logs Location

| Service | Log Location |
|---------|--------------|
| Backend | Console / `logs/` directory |
| Frontend | Browser console |
| AI Module | Console / uvicorn logs |
| Docker | `docker-compose logs <service>` |

### Health Checks

```bash
# Backend
curl http://localhost:8080/actuator/health

# AI Module
curl http://localhost:5000/health

# Database
psql -U postgres -c "SELECT 1"
```

---

## Backup & Recovery

### Database Backup

```bash
# Create backup
pg_dump -U postgres -h localhost ecommercespareparts > backup_$(date +%Y%m%d).sql

# Restore backup
psql -U postgres -h localhost ecommercespareparts < backup_YYYYMMDD.sql
```

### Docker Volumes Backup

```bash
# Backup postgres data
docker run --rm -v car_e-commerce_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz /data

# Backup AI models
docker run --rm -v car_e-commerce_ai_models:/data -v $(pwd):/backup alpine tar czf /backup/ai_models_backup.tar.gz /data
```
