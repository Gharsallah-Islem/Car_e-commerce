# Backend Documentation

> **Spring Boot 3.5.6 REST API** for the AutoParts Store e-commerce platform

## Table of Contents
- [Project Structure](#project-structure)
- [Entity Models](#entity-models)
- [Controllers](#controllers)
- [Services](#services)
- [Security](#security)
- [Configuration](#configuration)

---

## Project Structure

```
Backend/
├── src/main/java/com/example/Backend/
│   ├── BackendApplication.java          # Main application entry point
│   ├── config/                           # Configuration classes
│   │   ├── SecurityConfig.java           # Spring Security configuration
│   │   ├── WebSocketConfig.java          # WebSocket/STOMP configuration
│   │   ├── StripeConfig.java             # Stripe payment configuration
│   │   ├── SwaggerConfig.java            # OpenAPI documentation
│   │   ├── WebConfig.java                # CORS and web settings
│   │   ├── JacksonConfig.java            # JSON serialization
│   │   └── PasswordEncoderConfig.java    # BCrypt encoder
│   ├── controller/                       # REST API controllers (26)
│   ├── service/                          # Business logic layer (29)
│   │   └── impl/                         # Service implementations (23)
│   ├── repository/                       # JPA repositories (28)
│   ├── entity/                           # Domain entities (33)
│   ├── dto/                              # Data Transfer Objects (30)
│   ├── security/                         # Security components (7)
│   ├── exception/                        # Custom exceptions (3)
│   └── util/                             # Utility classes (2)
├── src/main/resources/
│   ├── application.properties            # Application configuration
│   └── data.sql                          # Seed data
└── pom.xml                               # Maven dependencies
```

---

## Entity Models

### Core Business Entities

#### User
The central user entity supporting multiple roles.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `username` | String | Unique username (3-100 chars) |
| `email` | String | Unique email address |
| `password` | String | BCrypt hashed password |
| `fullName` | String | Display name |
| `phone` | String | Contact number |
| `profilePicture` | String | Base64 or URL |
| `role` | Role | User role (FK) |
| `isEmailVerified` | Boolean | Email verification status |
| `isActive` | Boolean | Account status |

**Relationships:**
- `OneToMany` → Vehicle, Order, Reclamation, Conversation, Recommendation
- `OneToOne` → Cart

---

#### Product
Car parts available for purchase.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `name` | String | Product name |
| `description` | String | Product details |
| `price` | BigDecimal | Unit price |
| `stock` | Integer | Available quantity |
| `sku` | String | Stock Keeping Unit |
| `category` | Category | Product category (FK) |
| `brand` | Brand | Product brand (FK) |
| `isActive` | Boolean | Listing status |

**Relationships:**
- `ManyToOne` → Category, Brand
- `OneToMany` → ProductImage, OrderItem, CartItem

---

#### Order
Customer purchase orders.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `user` | User | Customer (FK) |
| `totalPrice` | BigDecimal | Order total |
| `status` | String | Order status |
| `deliveryAddress` | String | Shipping address |
| `paymentMethod` | String | STRIPE / CASH_ON_DELIVERY |
| `paymentStatus` | String | PENDING / COMPLETED / FAILED |
| `trackingNumber` | String | Delivery tracking |
| `createdAt` | LocalDateTime | Order timestamp |

**Order Statuses:**
- `PENDING` → Initial state
- `CONFIRMED` → Payment processed
- `SHIPPED` → With driver
- `DELIVERED` → Completed
- `CANCELLED` → Cancelled
- `DELIVERY_FAILED` → Failed attempt

---

#### Delivery
Delivery tracking and management.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `order` | Order | Associated order (FK) |
| `trackingNumber` | String | Unique tracking ID |
| `status` | String | Delivery status |
| `driver` | Driver | Assigned driver (FK) |
| `currentLatitude` | Double | GPS latitude |
| `currentLongitude` | Double | GPS longitude |
| `estimatedDelivery` | LocalDateTime | ETA |
| `actualDelivery` | LocalDateTime | Completion time |

**Delivery Statuses:**
- `PROCESSING` → Order confirmed
- `IN_TRANSIT` → En route
- `OUT_FOR_DELIVERY` → Final mile
- `DELIVERED` → Completed
- `FAILED` → Delivery failed

---

#### Driver
Delivery personnel management.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `name` | String | Driver name |
| `phone` | String | Contact number |
| `email` | String | Email address |
| `licenseNumber` | String | License ID |
| `vehicleType` | String | Vehicle category |
| `vehiclePlate` | String | License plate |
| `status` | String | AVAILABLE / ON_DELIVERY / OFFLINE |
| `currentLatitude` | Double | Current GPS lat |
| `currentLongitude` | Double | Current GPS long |

---

### Inventory Management Entities

#### Supplier
Distributors/vendors for stock.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `name` | String | Company name |
| `contactPerson` | String | Contact name |
| `email` | String | Email |
| `phone` | String | Phone |
| `address` | String | Business address |
| `isActive` | Boolean | Status |

---

#### PurchaseOrder
Orders to suppliers for restocking.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `supplier` | Supplier | Vendor (FK) |
| `orderNumber` | String | PO number |
| `status` | String | DRAFT / ORDERED / RECEIVED |
| `totalAmount` | BigDecimal | Order total |
| `orderDate` | LocalDateTime | Order timestamp |

---

#### StockMovement
Inventory transaction tracking.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `product` | Product | Affected product (FK) |
| `type` | String | IN / OUT / ADJUSTMENT |
| `quantity` | Integer | Change amount |
| `reason` | String | Movement reason |
| `createdAt` | LocalDateTime | Timestamp |

---

#### ReorderSetting
Automated restocking rules.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `product` | Product | Target product (FK) |
| `supplier` | Supplier | Preferred supplier (FK) |
| `minimumStock` | Integer | Low stock threshold |
| `reorderQuantity` | Integer | Auto-order quantity |
| `isAutoReorder` | Boolean | Automation enabled |

---

### Support System Entities

#### Reclamation
Customer support tickets.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `user` | User | Customer (FK) |
| `subject` | String | Ticket title |
| `description` | String | Issue details |
| `category` | String | Ticket category |
| `status` | String | OPEN / IN_PROGRESS / RESOLVED / CLOSED |
| `priority` | String | LOW / MEDIUM / HIGH / URGENT |
| `createdAt` | LocalDateTime | Submission time |

---

#### Conversation
AI chatbot conversations.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `user` | User | Customer (FK) |
| `startedAt` | LocalDateTime | Start time |
| `endedAt` | LocalDateTime | End time |
| `status` | String | Conversation status |

---

#### Message
Chat messages within conversations.

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Primary key |
| `conversation` | Conversation | Parent conversation (FK) |
| `content` | String | Message text |
| `sender` | String | USER / AI / SUPPORT |
| `createdAt` | LocalDateTime | Timestamp |

---

### Additional Entities

| Entity | Purpose |
|--------|---------|
| `Role` | User roles (CLIENT, ADMIN, SUPER_ADMIN, SUPPORT, DRIVER) |
| `Category` | Product categories |
| `Brand` | Product brands |
| `Cart` | Shopping cart |
| `CartItem` | Cart line items |
| `OrderItem` | Order line items |
| `Address` | User addresses |
| `Vehicle` | User's registered vehicles |
| `Payment` | Payment records |
| `ProductImage` | Product images |
| `AdminNotification` | Admin alerts |
| `UserActivity` | User behavior tracking |
| `Recommendation` | AI recommendations |
| `Report` | Generated reports |

---

## Controllers

### Authentication & Users

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `AuthController` | `/api/auth` | Login, register, OAuth, password reset |
| `UserController` | `/api/users` | User CRUD, profile management |
| `AdminController` | `/api/admin` | Admin-specific operations |
| `SuperAdminController` | `/api/super-admin` | Role management |

### E-Commerce

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `ProductController` | `/api/products` | Product CRUD, search, filter |
| `CategoryController` | `/api/categories` | Category management |
| `BrandController` | `/api/brands` | Brand management |
| `CartController` | `/api/cart` | Shopping cart operations |
| `OrderController` | `/api/orders` | Order management |
| `PaymentController` | `/api/payments` | Stripe integration |

### Delivery System

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `DeliveryController` | `/api/deliveries` | Delivery CRUD, tracking |
| `DriverController` | `/api/drivers` | Driver management |
| `LocationWebSocketController` | WebSocket | Real-time location updates |

### Inventory Management

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `InventoryController` | `/api/inventory` | Stock, suppliers, movements |

### Support System

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `ChatController` | `/api/chat` | AI conversations |
| `ReclamationController` | `/api/reclamations` | Ticket management |

### AI Integration

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `IAController` | `/api/ai` | Proxy to AI module |
| `RecommendationController` | `/api/recommendations` | Product recommendations |

### Analytics & Reporting

| Controller | Base Path | Purpose |
|------------|-----------|---------|
| `AnalyticsController` | `/api/analytics` | Dashboard metrics |
| `ReportController` | `/api/reports` | Report generation |
| `AdminNotificationController` | `/api/admin/notifications` | Admin alerts |
| `UserActivityController` | `/api/user-activity` | Behavior tracking |

---

## Services

### Core Business Services

| Service | Responsibilities |
|---------|-----------------|
| `OrderService` | Order lifecycle, status transitions |
| `CartService` | Cart operations, checkout preparation |
| `ProductService` | Product CRUD, stock checks |
| `PaymentService` | Stripe integration, payment processing |
| `DeliveryService` | Delivery creation, tracking |
| `DriverService` | Driver assignment, availability |

### Inventory Services

| Service | Responsibilities |
|---------|-----------------|
| `InventoryStatsService` | Stock levels, alerts |
| `StockMovementService` | Inventory transactions |
| `SupplierService` | Supplier management |
| `PurchaseOrderService` | Supplier orders |
| `ReorderSettingService` | Automation rules |

### Support Services

| Service | Responsibilities |
|---------|-----------------|
| `ChatService` | Conversation management |
| `ReclamationService` | Ticket handling |
| `GeminiService` | AI chatbot integration |

### Infrastructure Services

| Service | Responsibilities |
|---------|-----------------|
| `EmailService` | Transactional emails (53KB of templates!) |
| `AdminNotificationService` | Real-time admin alerts |
| `DeliverySimulationService` | Demo delivery simulation |
| `RecommendationService` | AI recommendation proxy |
| `UserActivityService` | Behavior tracking |
| `AnalyticsService` | Dashboard data generation |

---

## Security

### Authentication Methods

#### JWT Authentication
```java
// JWT Configuration
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation1234567890
jwt.expiration=3600000  // 1 hour
```

#### OAuth2 (Google)
```java
// Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.scope=profile,email
```

### Role Hierarchy

```
SUPER_ADMIN
    └── All permissions + role management
ADMIN
    └── All CLIENT + admin dashboard, inventory, orders, deliveries
SUPPORT
    └── View/manage reclamations, review AI conversations
DRIVER
    └── View assigned deliveries, update location/status
CLIENT
    └── Browse, purchase, track orders, chat support
```

### Security Configuration

Key security rules implemented in `SecurityConfig.java`:

```java
// Public endpoints
/api/auth/**           → permitAll
/api/products (GET)    → permitAll
/api/categories (GET)  → permitAll
/api/brands (GET)      → permitAll
/ws/**                 → permitAll (WebSocket)

// Protected endpoints
/api/orders/**         → authenticated
/api/cart/**           → authenticated
/api/profile/**        → authenticated

// Role-restricted
/api/admin/**          → ADMIN, SUPER_ADMIN
/api/inventory/**      → ADMIN, SUPER_ADMIN
/api/drivers/**        → ADMIN, SUPER_ADMIN
/api/support/**        → SUPPORT, ADMIN, SUPER_ADMIN
```

---

## Configuration

### application.properties Key Settings

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommercespareparts
spring.jpa.hibernate.ddl-auto=update

# JWT
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation1234567890
jwt.expiration=3600000

# File Upload
spring.servlet.multipart.max-file-size=10MB

# Stripe
stripe.api.key=sk_test_YOUR_KEY
stripe.webhook.secret=whsec_YOUR_SECRET

# AI Module
ai.module.url=http://localhost:5000
ai.module.timeout=30000

# Email (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587

# Frontend URL
app.frontend.url=http://localhost:4200
```

### Environment Variables for Production

| Variable | Description |
|----------|-------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing secret |
| `STRIPE_API_KEY` | Stripe secret key |
| `GOOGLE_CLIENT_ID` | OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | OAuth2 client secret |
| `AI_MODULE_URL` | AI module base URL |

---

## API Examples

### Authentication

**Login:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "john_doe",
    "email": "user@example.com",
    "role": "CLIENT"
  }
}
```

### Products

**Get Products with Filters:**
```http
GET /api/products?category=brakes&brand=bosch&minPrice=50&maxPrice=200&page=0&size=20
Authorization: Bearer <token>  // Optional

Response:
{
  "content": [...],
  "totalElements": 45,
  "totalPages": 3,
  "number": 0
}
```

### Orders

**Create Order:**
```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "deliveryAddress": "123 Main St, City",
  "paymentMethod": "STRIPE",
  "notes": "Please call before delivery"
}
```

---

## Running the Backend

```bash
# Development
cd Backend
mvn spring-boot:run

# Production build
mvn clean package
java -jar target/Backend-0.0.1-SNAPSHOT.jar

# With profiles
java -jar target/Backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Server runs on: `http://localhost:8080`

API Documentation: `http://localhost:8080/swagger-ui.html`
