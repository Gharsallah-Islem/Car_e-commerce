# API Reference

> Complete REST API documentation for the AutoParts Store backend

## Base URL
- **Development**: `http://localhost:8080/api`
- **Production**: `https://your-domain.com/api`

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## Table of Contents
- [Authentication](#authentication-endpoints)
- [Products](#products-endpoints)
- [Orders](#orders-endpoints)
- [Cart](#cart-endpoints)
- [Delivery](#delivery-endpoints)
- [Inventory](#inventory-endpoints)
- [Support](#support-endpoints)
- [AI Integration](#ai-integration-endpoints)
- [Admin](#admin-endpoints)

---

## Authentication Endpoints

### POST `/auth/login`
Authenticate user and receive JWT token.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "john_doe",
    "email": "user@example.com",
    "fullName": "John Doe",
    "role": { "id": 1, "name": "CLIENT" }
  }
}
```

---

### POST `/auth/register`
Register a new user account.

**Request:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "securePass123",
  "fullName": "John Doe"
}
```

**Response (201):**
```json
{
  "message": "Registration successful. Please verify your email."
}
```

---

### POST `/auth/forgot-password`
Request password reset email.

**Request:**
```json
{
  "email": "user@example.com"
}
```

---

### POST `/auth/reset-password`
Reset password with token from email.

**Request:**
```json
{
  "token": "reset-token-from-email",
  "newPassword": "newSecurePass123"
}
```

---

### GET `/auth/verify-email?token={token}`
Verify email address.

---

### GET `/auth/oauth2/google`
Initiate Google OAuth2 login flow.

---

## Products Endpoints

### GET `/products`
Get paginated product list with filters.

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `page` | int | Page number (default: 0) |
| `size` | int | Page size (default: 20) |
| `category` | UUID | Filter by category ID |
| `brand` | UUID | Filter by brand ID |
| `minPrice` | decimal | Minimum price |
| `maxPrice` | decimal | Maximum price |
| `search` | string | Search term |
| `inStock` | boolean | Only in-stock items |
| `sort` | string | Sort field (price, name, createdAt) |
| `direction` | string | ASC or DESC |

**Response (200):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Brake Pads - Premium",
      "description": "High-performance brake pads",
      "price": 89.99,
      "stock": 50,
      "sku": "BP-001",
      "category": { "id": "uuid", "name": "Brakes" },
      "brand": { "id": "uuid", "name": "Bosch" },
      "images": [{ "id": "uuid", "url": "/images/..." }],
      "isActive": true
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "number": 0,
  "size": 20
}
```

---

### GET `/products/{id}`
Get single product details.

---

### POST `/products` 🔐 ADMIN
Create new product.

**Request:**
```json
{
  "name": "Oil Filter",
  "description": "Premium oil filter for most vehicles",
  "price": 24.99,
  "stock": 100,
  "sku": "OF-002",
  "categoryId": "uuid",
  "brandId": "uuid"
}
```

---

### PUT `/products/{id}` 🔐 ADMIN
Update product.

---

### DELETE `/products/{id}` 🔐 ADMIN
Delete product.

---

### POST `/products/{id}/images` 🔐 ADMIN
Upload product images.

**Request:** `multipart/form-data`
- `files`: Image files

---

## Categories Endpoints

### GET `/categories`
Get all categories.

### POST `/categories` 🔐 ADMIN
Create category.

### PUT `/categories/{id}` 🔐 ADMIN
Update category.

### DELETE `/categories/{id}` 🔐 ADMIN
Delete category.

---

## Brands Endpoints

### GET `/brands`
Get all brands.

### POST `/brands` 🔐 ADMIN
Create brand.

### PUT `/brands/{id}` 🔐 ADMIN
Update brand.

### DELETE `/brands/{id}` 🔐 ADMIN
Delete brand.

---

## Cart Endpoints

### GET `/cart` 🔐
Get current user's cart.

**Response (200):**
```json
{
  "id": "uuid",
  "items": [
    {
      "id": "uuid",
      "product": { "id": "uuid", "name": "Brake Pads", "price": 89.99 },
      "quantity": 2,
      "subtotal": 179.98
    }
  ],
  "totalItems": 2,
  "totalPrice": 179.98
}
```

---

### POST `/cart/items` 🔐
Add item to cart.

**Request:**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "quantity": 2
}
```

---

### PUT `/cart/items/{productId}` 🔐
Update item quantity.

**Request:**
```json
{
  "quantity": 3
}
```

---

### DELETE `/cart/items/{productId}` 🔐
Remove item from cart.

---

### DELETE `/cart` 🔐
Clear entire cart.

---

## Orders Endpoints

### GET `/orders` 🔐
Get user's orders (or all orders for admin).

**Query Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `status` | string | Filter by status |
| `page` | int | Page number |
| `size` | int | Page size |

---

### GET `/orders/{id}` 🔐
Get order details.

**Response (200):**
```json
{
  "id": "uuid",
  "user": { "id": "uuid", "username": "john_doe" },
  "status": "CONFIRMED",
  "paymentMethod": "STRIPE",
  "paymentStatus": "COMPLETED",
  "totalPrice": 179.98,
  "deliveryAddress": "123 Main St, City",
  "trackingNumber": "TRK-20240115-001",
  "orderItems": [
    {
      "product": { "id": "uuid", "name": "Brake Pads" },
      "quantity": 2,
      "price": 89.99
    }
  ],
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### POST `/orders` 🔐
Create order from cart.

**Request:**
```json
{
  "deliveryAddress": "123 Main St, City, 12345",
  "paymentMethod": "STRIPE",
  "notes": "Leave at door"
}
```

---

### PUT `/orders/{id}/status` 🔐 ADMIN
Update order status.

**Request:**
```json
{
  "status": "SHIPPED"
}
```

---

### POST `/orders/{id}/cancel` 🔐
Cancel order (if still pending).

---

## Payment Endpoints

### POST `/payments/create-intent` 🔐
Create Stripe payment intent.

**Request:**
```json
{
  "orderId": "uuid"
}
```

**Response (200):**
```json
{
  "clientSecret": "pi_xxx_secret_xxx"
}
```

---

### POST `/payments/confirm` 🔐
Confirm payment completion.

**Request:**
```json
{
  "orderId": "uuid",
  "paymentIntentId": "pi_xxx"
}
```

---

### POST `/payments/webhook`
Stripe webhook endpoint (called by Stripe).

---

## Delivery Endpoints

### GET `/deliveries` 🔐 ADMIN
Get all deliveries.

---

### GET `/deliveries/{id}` 🔐
Get delivery details.

---

### GET `/deliveries/track/{trackingNumber}`
Track delivery by tracking number (public).

**Response (200):**
```json
{
  "trackingNumber": "TRK-20240115-001",
  "status": "IN_TRANSIT",
  "address": "123 Main St, City",
  "driver": {
    "name": "Mike Driver",
    "phone": "+1234567890"
  },
  "currentLocation": {
    "latitude": 36.8065,
    "longitude": 10.1815
  },
  "estimatedDelivery": "2024-01-15T16:00:00",
  "history": [
    { "status": "PROCESSING", "timestamp": "2024-01-15T10:00:00" },
    { "status": "IN_TRANSIT", "timestamp": "2024-01-15T12:00:00" }
  ]
}
```

---

### POST `/deliveries` 🔐 ADMIN
Create delivery for order.

**Request:**
```json
{
  "orderId": "uuid",
  "driverId": "uuid",
  "estimatedDelivery": "2024-01-15T16:00:00"
}
```

---

### PUT `/deliveries/{id}/assign` 🔐 ADMIN
Assign driver to delivery.

---

### PUT `/deliveries/{id}/status` 🔐 DRIVER
Update delivery status.

---

## Driver Endpoints

### GET `/drivers` 🔐 ADMIN
Get all drivers.

---

### GET `/drivers/{id}` 🔐 ADMIN
Get driver details.

---

### POST `/drivers` 🔐 ADMIN
Create new driver.

**Request:**
```json
{
  "name": "Mike Driver",
  "email": "mike@example.com",
  "phone": "+1234567890",
  "licenseNumber": "DL123456",
  "vehicleType": "Van",
  "vehiclePlate": "ABC-1234"
}
```

---

### GET `/drivers/available` 🔐 ADMIN
Get available drivers.

---

### PUT `/drivers/{id}/location` 🔐 DRIVER
Update driver location.

**Request:**
```json
{
  "latitude": 36.8065,
  "longitude": 10.1815
}
```

---

### PUT `/drivers/{id}/status` 🔐 DRIVER
Update driver status.

**Request:**
```json
{
  "status": "AVAILABLE"
}
```

---

## Inventory Endpoints

### GET `/inventory/stats` 🔐 ADMIN
Get inventory statistics.

**Response (200):**
```json
{
  "totalProducts": 500,
  "lowStockCount": 15,
  "outOfStockCount": 3,
  "totalValue": 125000.00,
  "recentMovements": [...]
}
```

---

### GET `/inventory/suppliers` 🔐 ADMIN
Get all suppliers.

---

### POST `/inventory/suppliers` 🔐 ADMIN
Create supplier.

---

### GET `/inventory/movements` 🔐 ADMIN
Get stock movements.

---

### POST `/inventory/movements` 🔐 ADMIN
Record stock movement.

**Request:**
```json
{
  "productId": "uuid",
  "type": "IN",
  "quantity": 50,
  "reason": "Purchase order received"
}
```

---

### GET `/inventory/purchase-orders` 🔐 ADMIN
Get purchase orders.

---

### POST `/inventory/purchase-orders` 🔐 ADMIN
Create purchase order.

---

### GET `/inventory/reorder-settings` 🔐 ADMIN
Get auto-reorder settings.

---

### PUT `/inventory/reorder-settings/{productId}` 🔐 ADMIN
Update reorder settings.

---

## Support Endpoints

### GET `/reclamations` 🔐
Get reclamations (user's own or all for support).

---

### GET `/reclamations/{id}` 🔐
Get reclamation details.

---

### POST `/reclamations` 🔐
Create new reclamation.

**Request:**
```json
{
  "subject": "Damaged product received",
  "description": "The brake pads were damaged in shipping...",
  "category": "DELIVERY_ISSUE",
  "orderId": "uuid"
}
```

---

### PUT `/reclamations/{id}/status` 🔐 SUPPORT
Update reclamation status.

---

### POST `/reclamations/{id}/respond` 🔐 SUPPORT
Add response to reclamation.

---

## Chat Endpoints

### GET `/chat/conversations` 🔐
Get user's conversations.

---

### POST `/chat/conversations` 🔐
Start new conversation.

---

### GET `/chat/conversations/{id}/messages` 🔐
Get conversation messages.

---

### POST `/chat/conversations/{id}/messages` 🔐
Send message to conversation.

**Request:**
```json
{
  "content": "I need help finding brake pads for my car"
}
```

**Response (200):**
```json
{
  "userMessage": {
    "id": "uuid",
    "content": "I need help finding brake pads...",
    "sender": "USER"
  },
  "aiResponse": {
    "id": "uuid",
    "content": "I'd be happy to help! What make and model is your car?",
    "sender": "AI"
  }
}
```

---

### GET `/chat/conversations/{id}` 🔐 SUPPORT
Get conversation for review (support).

---

## AI Integration Endpoints

### POST `/ai/predict` 🔐
Predict car part from image.

**Request:** `multipart/form-data`
- `image`: Image file

**Response (200):**
```json
{
  "predictions": [
    { "class": "brake_pad", "confidence": 0.95, "confidence_percent": "95.00%" },
    { "class": "brake_disc", "confidence": 0.03, "confidence_percent": "3.00%" }
  ],
  "matchedProducts": [
    { "id": "uuid", "name": "Premium Brake Pads", "price": 89.99 }
  ]
}
```

---

### GET `/recommendations` 🔐
Get personalized recommendations.

---

### GET `/recommendations/product/{productId}`
Get similar products.

---

### GET `/recommendations/trending`
Get trending products.

---

## Admin Endpoints

### GET `/admin/dashboard` 🔐 ADMIN
Get dashboard statistics.

---

### GET `/admin/notifications` 🔐 ADMIN
Get admin notifications.

---

### PUT `/admin/notifications/{id}/read` 🔐 ADMIN
Mark notification as read.

---

## Analytics Endpoints

### GET `/analytics/sales` 🔐 ADMIN
Get sales analytics.

**Query Parameters:**
- `period`: DAY, WEEK, MONTH, YEAR

---

### GET `/analytics/products` 🔐 ADMIN
Get product performance metrics.

---

### GET `/analytics/users` 🔐 ADMIN
Get user analytics.

---

## User Management Endpoints

### GET `/users` 🔐 ADMIN
Get all users (paginated).

---

### GET `/users/{id}` 🔐 ADMIN
Get user by ID.

---

### PUT `/users/{id}/role` 🔐 SUPER_ADMIN
Change user role.

**Request:**
```json
{
  "roleId": 2
}
```

---

### PUT `/users/{id}/status` 🔐 ADMIN
Activate/deactivate user.

---

## Profile Endpoints

### GET `/profile` 🔐
Get current user profile.

---

### PUT `/profile` 🔐
Update profile.

**Request:**
```json
{
  "fullName": "John Smith",
  "phone": "+1234567890"
}
```

---

### PUT `/profile/password` 🔐
Change password.

**Request:**
```json
{
  "currentPassword": "oldPass123",
  "newPassword": "newSecurePass123"
}
```

---

### PUT `/profile/picture` 🔐
Update profile picture.

**Request:** `multipart/form-data`
- `file`: Image file

---

## WebSocket Endpoints

### Connect
```
ws://localhost:8080/ws
```

### Topics

| Topic | Description |
|-------|-------------|
| `/topic/delivery/{trackingNumber}` | Delivery location updates |
| `/topic/driver/{driverId}/location` | Driver GPS broadcasts |
| `/topic/notifications` | System notifications |
| `/user/queue/notifications` | Personal notifications |
| `/user/queue/order-updates` | Order status changes |

---

## Error Responses

All errors follow this format:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/products",
  "details": {
    "name": "Name is required",
    "price": "Price must be greater than 0"
  }
}
```

**Common Status Codes:**

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## Rate Limiting

Currently no rate limiting is implemented. Consider adding for production.

---

## API Versioning

Current version: **v1** (implicit in paths)

Future versions will be accessed via: `/api/v2/...`
