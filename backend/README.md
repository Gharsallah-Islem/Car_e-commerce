# Car Parts Backend - Spring Boot + PostgreSQL

A RESTful API backend for a car parts e-commerce platform, built with Spring Boot 3.2, Kotlin, and PostgreSQL.

## 📋 Overview

This backend provides a complete API for:
- User authentication and authorization with JWT
- Product catalog management
- Shopping cart operations
- Order processing and tracking
- Payment integration (Stripe)
- Email verification with OTP
- Password reset functionality

## 🏗️ Architecture

### Project Structure
```
backend/src/main/kotlin/com/integmobile/backend/
├── BackendApplication.kt           # Main application entry point
├── config/                          # Configuration classes
│   ├── CorsConfig.kt               # CORS configuration
│   ├── DataSeeder.kt               # Initial data seeding
│   └── SecurityConfig.kt           # Security & JWT configuration
├── controller/                      # REST Controllers
│   ├── AuthController.kt           # Authentication endpoints
│   ├── CartController.kt           # Cart management
│   ├── OrderController.kt          # Order operations
│   └── ProductController.kt        # Product catalog
├── model/                           # Data models
│   ├── entity/                     # JPA Entities
│   │   ├── CartItem.kt
│   │   ├── Order.kt
│   │   ├── OtpVerification.kt
│   │   ├── Product.kt
│   │   └── User.kt
│   ├── request/                    # Request DTOs
│   │   ├── AuthRequests.kt
│   │   ├── CartRequests.kt
│   │   ├── OrderRequests.kt
│   │   └── ProductRequests.kt
│   └── response/                   # Response DTOs
│       ├── AuthResponses.kt
│       └── CartResponses.kt
├── repository/                      # JPA Repositories
│   ├── CartItemRepository.kt
│   ├── OrderRepository.kt
│   ├── OtpVerificationRepository.kt
│   ├── ProductRepository.kt
│   └── UserRepository.kt
├── security/                        # Security components
│   ├── JwtAuthenticationFilter.kt  # JWT filter
│   └── JwtTokenProvider.kt         # JWT utilities
├── service/                         # Business logic
│   ├── AuthService.kt
│   ├── CartService.kt
│   ├── OrderService.kt
│   └── ProductService.kt
└── util/                            # Utilities
    └── ResponseUtil.kt
```

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Language**: Kotlin 1.9.20
- **Database**: PostgreSQL
- **ORM**: Hibernate/JPA
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle 8.x

## 🚀 Quick Start

### Prerequisites
- **Java**: JDK 17 or higher
- **PostgreSQL**: Version 15 or higher
- **Gradle**: 8.0+ (or use included wrapper)

### 1. Install PostgreSQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
```

**macOS:**
```bash
brew install postgresql@15
brew services start postgresql@15
```

**Windows:**
Download from [postgresql.org](https://www.postgresql.org/download/windows/)

### 2. Create Database

```bash
# Access PostgreSQL
sudo -u postgres psql

# Create database and user
CREATE DATABASE carparts;
CREATE USER carparts_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE carparts TO carparts_user;

# Exit
\q
```

Or simply:
```bash
createdb carparts
```

### 3. Configure Application

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/carparts
    username: postgres  # or carparts_user
    password: postgres  # change to your password
    
jwt:
  secret: YourVeryLongSecretKeyForJWTMustBeAtLeast256BitsLong
  expiration: 86400000  # 24 hours

cors:
  allowed-origins: http://localhost:3000,http://10.0.2.2:8080
```

### 4. Build and Run

```bash
# Navigate to backend directory
cd backend

# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The server will start at: **http://localhost:8080/api**

### 5. Verify Installation

```bash
# Test health (if endpoint exists)
curl http://localhost:8080/api/products

# Should return 401 Unauthorized (expected, as auth is required)
```

## 📡 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "phone": "+1234567890"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Registration successful. Please verify your email.",
  "data": {
    "userId": 1,
    "email": "john@example.com"
  }
}
```

#### Verify Email
```http
POST /api/auth/verify-email
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "emailVerified": true
    }
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    }
  }
}
```

#### Request Password Reset
```http
POST /api/auth/request-password-reset
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePass123"
}
```

### Product Endpoints

#### Get All Products
```http
GET /api/products
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Brake Pads",
      "description": "High-quality ceramic brake pads",
      "price": 49.99,
      "category": "Brakes",
      "brand": "Brembo",
      "imageUrl": "https://example.com/brake-pads.jpg",
      "stock": 50
    }
  ]
}
```

#### Get Product by ID
```http
GET /api/products/1
Authorization: Bearer <token>
```

#### Search Products
```http
GET /api/products/search?query=brake
Authorization: Bearer <token>
```

#### Filter Products
```http
POST /api/products/filter
Authorization: Bearer <token>
Content-Type: application/json

{
  "category": "Brakes",
  "minPrice": 0,
  "maxPrice": 100,
  "brand": "Brembo"
}
```

### Cart Endpoints

#### Get Cart
```http
GET /api/cart
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "Brake Pads",
          "price": 49.99
        },
        "quantity": 2,
        "subtotal": 99.98
      }
    ],
    "total": 99.98
  }
}
```

#### Add to Cart
```http
POST /api/cart
Authorization: Bearer <token>
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

#### Update Cart Item
```http
PUT /api/cart/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "quantity": 3
}
```

#### Remove from Cart
```http
DELETE /api/cart/1
Authorization: Bearer <token>
```

#### Clear Cart
```http
DELETE /api/cart
Authorization: Bearer <token>
```

### Order Endpoints

#### Get All Orders
```http
GET /api/orders
Authorization: Bearer <token>
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "orderNumber": "ORD-20231201-001",
      "status": "PENDING",
      "total": 99.98,
      "createdAt": "2023-12-01T10:00:00Z",
      "items": [
        {
          "productName": "Brake Pads",
          "quantity": 2,
          "price": 49.99
        }
      ]
    }
  ]
}
```

#### Get Order by ID
```http
GET /api/orders/1
Authorization: Bearer <token>
```

#### Create Order
```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "deliveryAddress": {
    "street": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "latitude": 40.7128,
    "longitude": -74.0060
  },
  "paymentMethod": "STRIPE",
  "stripePaymentIntentId": "pi_xxxxxxxxxxxxx"
}
```

#### Cancel Order
```http
PUT /api/orders/1/cancel
Authorization: Bearer <token>
```

#### Submit Claim
```http
POST /api/orders/claim
Authorization: Bearer <token>
Content-Type: application/json

{
  "orderId": 1,
  "reason": "Product damaged",
  "description": "The brake pads arrived with visible damage",
  "images": ["https://example.com/damage1.jpg"]
}
```

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Products Table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    brand VARCHAR(100),
    image_url VARCHAR(500),
    stock INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Cart Items Table
```sql
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, product_id)
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(50) NOT NULL,
    total DECIMAL(10, 2) NOT NULL,
    delivery_address JSONB,
    payment_method VARCHAR(50),
    payment_intent_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### OTP Verifications Table
```sql
CREATE TABLE otp_verifications (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔧 Configuration

### Application Properties

**Database Configuration:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/carparts
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update  # Use 'validate' in production
    show-sql: true      # Disable in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

**Server Configuration:**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api
```

**JWT Configuration:**
```yaml
jwt:
  secret: YourVeryLongSecretKeyMustBeAtLeast256BitsForHS256Algorithm
  expiration: 86400000  # 24 hours in milliseconds
```

**CORS Configuration:**
```yaml
cors:
  allowed-origins: http://localhost:3000,http://10.0.2.2:8080
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```

**Email Configuration (Optional):**
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

**Stripe Configuration:**
```yaml
stripe:
  api-key: sk_test_your_stripe_secret_key
```

## 🔐 Security

### JWT Authentication
- All endpoints except `/api/auth/**` require authentication
- JWT token must be included in Authorization header
- Token format: `Bearer <token>`
- Token expiration: 24 hours (configurable)

### Password Security
- Passwords hashed with BCrypt
- Minimum password length enforced
- Password reset via OTP

### CORS
- Configured for mobile app access
- Allows credentials
- Customizable origins

## 📦 Sample Data

On first run, the application seeds 8 sample products:
1. Brake Pads - $49.99
2. Oil Filter - $12.99
3. Air Filter - $15.99
4. Spark Plugs - $8.99
5. Wiper Blades - $19.99
6. Battery - $89.99
7. Headlight Bulbs - $24.99
8. Cabin Air Filter - $18.99

## 🧪 Testing

### Manual Testing with cURL

**Register and Login:**
```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "phone": "+1234567890"
  }'

# Check console for OTP, then verify
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "otp": "123456"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Use JWT Token:**
```bash
# Save token from login response
TOKEN="your_jwt_token_here"

# Get products
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer $TOKEN"

# Add to cart
curl -X POST http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'

# Get cart
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: Bearer $TOKEN"
```

### Automated Tests
```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## 📱 Mobile App Integration

### Android Configuration

Update `Constants.kt` in your Android app:

```kotlin
object Constants {
    // For Android Emulator
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    
    // For Physical Device (replace with your computer's IP)
    // const val BASE_URL = "http://192.168.1.100:8080/api/"
}
```

### Network Security Config

For development, add to `AndroidManifest.xml`:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config">
```

Create `res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.1.100</domain>
    </domain-config>
</network-security-config>
```

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8081
```

### Database Connection Error
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Start PostgreSQL
sudo systemctl start postgresql

# Test connection
psql -U postgres -d carparts -c "SELECT 1;"
```

**Common Issues:**
- Wrong username/password in `application.yml`
- Database doesn't exist: `createdb carparts`
- PostgreSQL not running
- Firewall blocking port 5432

### CORS Errors from Mobile App
```yaml
# Add your device IP to allowed-origins
cors:
  allowed-origins: http://10.0.2.2:8080,http://192.168.1.100:8080
```

### JWT Token Issues
- **Token expired**: Tokens expire after 24 hours by default
- **Invalid token**: Check token format and secret key
- **No token**: Ensure Authorization header is set

### Hibernate/JPA Errors
```yaml
# Enable SQL logging
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

### Build Errors
```bash
# Clean build
./gradlew clean build

# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build --refresh-dependencies
```

## 🚀 Production Deployment

### Checklist
- [ ] Change `ddl-auto` to `validate` or `none`
- [ ] Disable SQL logging (`show-sql: false`)
- [ ] Use strong JWT secret (256+ bits)
- [ ] Configure production database
- [ ] Set up SSL/TLS
- [ ] Configure production CORS origins
- [ ] Set up email service (SMTP)
- [ ] Configure Stripe production keys
- [ ] Set up monitoring and logging
- [ ] Configure backup strategy

### Environment Variables
```bash
export DB_URL=jdbc:postgresql://prod-db:5432/carparts
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=very_long_production_secret_key
export STRIPE_API_KEY=sk_live_your_key
```

### Build for Production
```bash
# Build JAR
./gradlew bootJar

# Run
java -jar build/libs/backend-1.0.0.jar
```

## 📄 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT.io](https://jwt.io/)
- [Stripe API](https://stripe.com/docs/api)

## 📞 Support

For project overview, see [../README.md](../README.md)

For Android app documentation, see [../app/README.md](../app/README.md)
