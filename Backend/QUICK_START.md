# 🚀 Quick Start Guide

## Prerequisites
- ✅ Java 21 installed
- ✅ Maven installed (or use ./mvnw.cmd)
- ✅ PostgreSQL installed and running
- ✅ Database `ecommercespareparts` created

## Database Setup

```sql
-- Connect to PostgreSQL
psql -U postgres

-- Create database and user
CREATE DATABASE ecommercespareparts;
CREATE USER lasmer WITH PASSWORD 'lasmer';
GRANT ALL PRIVILEGES ON DATABASE ecommercespareparts TO lasmer;

-- Connect to the database
\c ecommercespareparts

-- The application will auto-create tables on first run
```

## Configuration

1. **Update Email Settings** (if needed):
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

2. **JWT Secret** (production):
   Change the JWT secret key in `application.properties`

## Running the Application

### Option 1: Using Maven Wrapper (Recommended)
```bash
cd "c:\Users\islem\OneDrive\Bureau\pfe\Projet integration\Code\Backend"
./mvnw.cmd spring-boot:run
```

### Option 2: Using Maven
```bash
cd "c:\Users\islem\OneDrive\Bureau\pfe\Projet integration\Code\Backend"
mvn spring-boot:run
```

### Option 3: Build and Run JAR
```bash
# Build
./mvnw.cmd clean package -DskipTests

# Run
java -jar target/Backend-0.0.1-SNAPSHOT.jar
```

## Verify Application is Running

1. **Health Check:**
   - Open: `http://localhost:8080/actuator/health`
   - Should return: `{"status":"UP"}`

2. **API Documentation:**
   - Open: `http://localhost:8080/swagger-ui.html`
   - Browse all 152+ endpoints

3. **OpenAPI Spec:**
   - Open: `http://localhost:8080/v3/api-docs`

## Initial Setup

### 1. Insert Roles (First Time Only)
```sql
-- Connect to database
psql -U lasmer -d ecommercespareparts

-- Insert roles
INSERT INTO roles (id, name) VALUES 
    (1, 'CLIENT'),
    (2, 'SUPPORT'),
    (3, 'ADMIN'),
    (4, 'SUPER_ADMIN');
```

### 2. Create Super Admin (via API or SQL)
```sql
-- Create super admin directly in database
INSERT INTO super_admins (id, username, email, password, full_name, is_active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'superadmin',
    'admin@ecommercespareparts.com',
    -- Password: "admin123" (BCrypt hashed)
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Super Administrator',
    true,
    NOW(),
    NOW()
);
```

### 3. Register a Test User (via API)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User"
  }'
```

### 4. Login and Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## Testing Endpoints

### Authentication Required Endpoints
Include JWT token in header:
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Public Endpoints (No Auth Required)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/products/**` - Browse products
- `GET /api/public/**` - Public content

## Common Issues

### Issue 1: Database Connection Failed
**Solution:** Verify PostgreSQL is running and credentials are correct
```bash
psql -U lasmer -d ecommercespareparts
```

### Issue 2: Port 8080 Already in Use
**Solution:** Change port in `application.properties`
```properties
server.port=8081
```

### Issue 3: Email Sending Fails
**Solution:** 
- Use Gmail App Password (not regular password)
- Enable 2FA on Gmail account
- Generate App Password at: https://myaccount.google.com/apppasswords

### Issue 4: JWT Token Invalid
**Solution:**
- Check token expiration (24 hours by default)
- Verify JWT secret hasn't changed
- Re-login to get new token

## Project Structure

```
Backend/
├── src/main/java/com/example/Backend/
│   ├── config/          # Security, Swagger, Web configs
│   ├── controller/      # 13 REST controllers (152+ endpoints)
│   ├── dto/             # 13 Data Transfer Objects
│   ├── entity/          # 16 JPA entities
│   ├── exception/       # Custom exceptions & global handler
│   ├── repository/      # 16 Spring Data repositories
│   ├── security/        # JWT & authentication components
│   ├── service/         # 13 service interfaces
│   │   └── impl/        # 13 service implementations
│   └── util/            # Email utility
├── src/main/resources/
│   └── application.properties  # Configuration
└── pom.xml              # Maven dependencies
```

## Available Endpoints Summary

| Category | Count | Base Path | Auth Required |
|----------|-------|-----------|---------------|
| Auth | 4 | /api/auth/** | No |
| Users | 14 | /api/users/** | Yes (CLIENT) |
| Products | 23 | /api/products/** | Some public |
| Cart | 9 | /api/cart/** | Yes (CLIENT) |
| Orders | 19 | /api/orders/** | Yes (CLIENT) |
| Vehicles | 8 | /api/vehicles/** | Yes (CLIENT) |
| Chat | 13 | /api/chat/** | Yes |
| Reclamations | 18 | /api/reclamations/** | Yes |
| Deliveries | 17 | /api/deliveries/** | Yes |
| Admin | 10 | /api/admin/** | Yes (ADMIN) |
| Reports | 12 | /api/reports/** | Yes (ADMIN) |
| IA | 9 | /api/ia/** | Yes |

**Total: 152+ endpoints**

## Development Tips

### Enable SQL Logging
Already enabled in `application.properties`:
```properties
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=DEBUG
```

### Hot Reload (Spring DevTools)
Add dependency in `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### Testing with Postman
1. Import OpenAPI spec: `http://localhost:8080/v3/api-docs`
2. Set up environment variable for JWT token
3. Use pre-request script to auto-add Bearer token

## Support

For issues or questions:
- Check logs in console
- Review `CODE_REVIEW_SUMMARY.md` for detailed documentation
- Check Swagger UI for API documentation
- Review entity relationships in database

---

**Last Updated:** 2025-10-17  
**Application Version:** 0.0.1-SNAPSHOT  
**Spring Boot Version:** 3.5.6  
**Java Version:** 21
