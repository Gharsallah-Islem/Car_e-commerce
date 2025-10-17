# 🧪 Comprehensive API Testing Suite

**Date:** 2025-10-17  
**Application:** E-Commerce Spare Parts Backend  
**Base URL:** `http://localhost:8080`  
**Total Endpoints:** 152+  

---

## 📋 Testing Checklist

### ✅ Phase 1: Health & Documentation
- [ ] Application health check
- [ ] Swagger UI accessibility
- [ ] OpenAPI documentation
- [ ] Database connectivity

### ✅ Phase 2: Authentication (Auth Controller - 4 endpoints)
- [ ] User Registration
- [ ] User Login
- [ ] Token Generation
- [ ] Token Validation

### ✅ Phase 3: User Management (User Controller - 14 endpoints)
- [ ] Get user profile
- [ ] Update user profile
- [ ] Change password
- [ ] Get user orders
- [ ] Get user vehicles
- [ ] Get user cart
- [ ] Get user reclamations
- [ ] Get user conversations
- [ ] Get user recommendations
- [ ] Delete user account
- [ ] User search (admin)
- [ ] List all users (admin)
- [ ] Activate/deactivate user (admin)
- [ ] User statistics (admin)

### ✅ Phase 4: Product Management (Product Controller - 23 endpoints)
- [ ] List all products
- [ ] Get product by ID
- [ ] Search products
- [ ] Filter by category
- [ ] Filter by brand
- [ ] Filter by price range
- [ ] Filter by compatibility
- [ ] Get featured products
- [ ] Get top-selling products
- [ ] Get low stock products
- [ ] Create product (admin)
- [ ] Update product (admin)
- [ ] Delete product (admin)
- [ ] Update stock (admin)
- [ ] Bulk update (admin)
- [ ] Product analytics (admin)

### ✅ Phase 5: Shopping Cart (Cart Controller - 9 endpoints)
- [ ] Get user cart
- [ ] Add item to cart
- [ ] Update item quantity
- [ ] Remove item from cart
- [ ] Clear cart
- [ ] Get cart total
- [ ] Apply discount code
- [ ] Save cart for later
- [ ] Merge guest cart

### ✅ Phase 6: Order Management (Order Controller - 19 endpoints)
- [ ] Create order
- [ ] Get order by ID
- [ ] List user orders
- [ ] Get order details
- [ ] Cancel order
- [ ] Update order status (admin)
- [ ] Get orders by status
- [ ] Search orders
- [ ] Order statistics
- [ ] Export orders
- [ ] Process payment
- [ ] Confirm payment
- [ ] Generate invoice
- [ ] Track order
- [ ] Request refund
- [ ] Process refund (admin)
- [ ] Get pending orders (admin)
- [ ] Get revenue report (admin)

### ✅ Phase 7: Vehicle Management (Vehicle Controller - 8 endpoints)
- [ ] Add vehicle
- [ ] List user vehicles
- [ ] Get vehicle by ID
- [ ] Update vehicle
- [ ] Delete vehicle
- [ ] Get compatible products
- [ ] Vehicle statistics (admin)
- [ ] Popular vehicle models (admin)

### ✅ Phase 8: Chat System (Chat Controller - 13 endpoints)
- [ ] Start conversation
- [ ] List user conversations
- [ ] Get conversation by ID
- [ ] Send message
- [ ] Get conversation messages
- [ ] Mark message as read
- [ ] Mark all as read
- [ ] Close conversation
- [ ] Reopen conversation
- [ ] Delete conversation
- [ ] Get unread count
- [ ] Assign to support (support)
- [ ] Get assigned chats (support)

### ✅ Phase 9: Reclamation/Tickets (Reclamation Controller - 18 endpoints)
- [ ] Create reclamation
- [ ] List user reclamations
- [ ] Get reclamation by ID
- [ ] Update reclamation
- [ ] Add response
- [ ] Close reclamation
- [ ] Reopen reclamation
- [ ] Upload attachment
- [ ] Get by status
- [ ] Get by category
- [ ] Search reclamations
- [ ] Assign to support (support)
- [ ] Get assigned tickets (support)
- [ ] Resolve ticket (support)
- [ ] Escalate ticket (support)
- [ ] Get all reclamations (admin)
- [ ] Reclamation statistics (admin)
- [ ] Export reclamations (admin)

### ✅ Phase 10: Delivery Tracking (Delivery Controller - 17 endpoints)
- [ ] Create delivery
- [ ] Get delivery by ID
- [ ] Track by order ID
- [ ] Track by tracking number
- [ ] Update delivery status
- [ ] Assign driver
- [ ] Update location
- [ ] Get delivery history
- [ ] Estimate delivery time
- [ ] Confirm delivery
- [ ] Report delivery issue
- [ ] Reschedule delivery
- [ ] Get active deliveries (admin)
- [ ] Get driver deliveries (admin)
- [ ] Delivery statistics (admin)
- [ ] Failed delivery report (admin)
- [ ] Performance metrics (admin)

### ✅ Phase 11: Admin Operations (Admin Controller - 10 endpoints)
- [ ] Create admin
- [ ] List all admins
- [ ] Get admin by ID
- [ ] Update admin
- [ ] Delete admin
- [ ] Activate/deactivate admin
- [ ] Update permissions
- [ ] Get admin activity log
- [ ] Get system statistics
- [ ] Admin dashboard data

### ✅ Phase 12: Reports & Analytics (Report Controller - 12 endpoints)
- [ ] Generate sales report
- [ ] Generate inventory report
- [ ] Generate user report
- [ ] Generate order report
- [ ] Generate revenue report
- [ ] Get report by ID
- [ ] List all reports
- [ ] Delete report
- [ ] Export report (CSV)
- [ ] Export report (PDF)
- [ ] Schedule report
- [ ] Analytics dashboard

### ✅ Phase 13: AI Recommendations (IA Controller - 9 endpoints)
- [ ] Upload image for diagnosis
- [ ] Get AI recommendation
- [ ] List user recommendations
- [ ] Get recommendation by ID
- [ ] Rate recommendation
- [ ] Get vehicle-based recommendations
- [ ] Get purchase history recommendations
- [ ] Chat with AI assistant
- [ ] Get trending products

---

## 🔧 Test Setup

### Prerequisites
```bash
# Ensure database is running
psql -U lasmer -d ecommercespareparts

# Ensure application is running
./mvnw.cmd spring-boot:run

# Application should be accessible at:
# http://localhost:8080
```

### Required Tools
- ✅ cURL (command line)
- ✅ Postman (optional, GUI)
- ✅ Web Browser (for Swagger UI)

---

## 📝 Test Scripts

### Test 1: Health Check
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Expected Response:
# {"status":"UP"}
```

### Test 2: Swagger UI
```bash
# Open in browser:
http://localhost:8080/swagger-ui.html

# Verify:
# - All controllers visible
# - All endpoints documented
# - Try it out functionality works
```

### Test 3: Database Connectivity
```sql
-- Verify roles exist
SELECT * FROM roles;

-- Expected: CLIENT, SUPPORT, ADMIN, SUPER_ADMIN
```

---

## 🧪 Authentication Tests

### Test 4: User Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "fullName": "Test User",
    "phoneNumber": "+216 20 123 456",
    "address": "123 Test Street, Tunis"
  }'

# Expected: 201 CREATED
# Response should include user details (without password)
```

### Test 5: User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Expected: 200 OK
# Response should include JWT token
# Save token for subsequent tests
```

### Test 6: Invalid Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "wrongpassword"
  }'

# Expected: 401 UNAUTHORIZED
```

---

## 🧪 User Management Tests

### Test 7: Get User Profile (Requires Auth)
```bash
curl -X GET http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
# Response should include user profile data
```

### Test 8: Update User Profile
```bash
curl -X PUT http://localhost:8080/api/users/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Updated Test User",
    "phoneNumber": "+216 20 999 888",
    "address": "456 New Address, Tunis"
  }'

# Expected: 200 OK
```

### Test 9: Unauthorized Access
```bash
curl -X GET http://localhost:8080/api/users/profile

# Expected: 401 UNAUTHORIZED
# (No token provided)
```

---

## 🧪 Product Management Tests

### Test 10: Get All Products (Public)
```bash
curl -X GET "http://localhost:8080/api/products?page=0&size=10"

# Expected: 200 OK
# Response should include paginated product list
```

### Test 11: Search Products
```bash
curl -X GET "http://localhost:8080/api/products/search?keyword=brake&page=0&size=10"

# Expected: 200 OK
# Response should include matching products
```

### Test 12: Filter by Category
```bash
curl -X GET "http://localhost:8080/api/products/category/BRAKE_SYSTEM?page=0&size=10"

# Expected: 200 OK
```

### Test 13: Filter by Brand
```bash
curl -X GET "http://localhost:8080/api/products/brand/Bosch?page=0&size=10"

# Expected: 200 OK
```

### Test 14: Create Product (Admin Only)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Premium Brake Pads",
    "description": "High-performance ceramic brake pads",
    "price": 150.00,
    "category": "BRAKE_SYSTEM",
    "brand": "Bosch",
    "stockQuantity": 50,
    "imageUrl": "https://example.com/brake-pads.jpg",
    "vehicleCompatibility": {
      "brands": ["Toyota", "Honda"],
      "models": ["Corolla", "Civic"],
      "years": [2018, 2019, 2020, 2021, 2022]
    }
  }'

# Expected: 201 CREATED
```

---

## 🧪 Shopping Cart Tests

### Test 15: Add to Cart
```bash
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "PRODUCT_UUID_HERE",
    "quantity": 2
  }'

# Expected: 200 OK
```

### Test 16: Get Cart
```bash
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
# Response should include cart items and total
```

### Test 17: Update Quantity
```bash
curl -X PUT http://localhost:8080/api/cart/items/CART_ITEM_UUID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5
  }'

# Expected: 200 OK
```

### Test 18: Remove from Cart
```bash
curl -X DELETE http://localhost:8080/api/cart/items/CART_ITEM_UUID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 204 NO CONTENT
```

---

## 🧪 Order Management Tests

### Test 19: Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "shippingAddress": "123 Test Street, Tunis",
    "billingAddress": "123 Test Street, Tunis",
    "phoneNumber": "+216 20 123 456",
    "paymentMethod": "CASH_ON_DELIVERY",
    "notes": "Please call before delivery"
  }'

# Expected: 201 CREATED
# Response should include order details and order number
```

### Test 20: Get Order by ID
```bash
curl -X GET http://localhost:8080/api/orders/ORDER_UUID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

### Test 21: List User Orders
```bash
curl -X GET "http://localhost:8080/api/orders/user?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

### Test 22: Cancel Order
```bash
curl -X PATCH http://localhost:8080/api/orders/ORDER_UUID/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

---

## 🧪 Vehicle Management Tests

### Test 23: Add Vehicle
```bash
curl -X POST http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2020
  }'

# Expected: 201 CREATED
```

### Test 24: Get User Vehicles
```bash
curl -X GET http://localhost:8080/api/vehicles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

### Test 25: Get Compatible Products
```bash
curl -X GET http://localhost:8080/api/vehicles/VEHICLE_UUID/compatible-products \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

---

## 🧪 Chat System Tests

### Test 26: Start Conversation
```bash
curl -X POST http://localhost:8080/api/chat/conversations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Product Inquiry"
  }'

# Expected: 201 CREATED
```

### Test 27: Send Message
```bash
curl -X POST http://localhost:8080/api/chat/conversations/CONVERSATION_UUID/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "I need help selecting brake pads for my Toyota Corolla 2020",
    "messageType": "TEXT"
  }'

# Expected: 201 CREATED
```

### Test 28: Get Conversation Messages
```bash
curl -X GET http://localhost:8080/api/chat/conversations/CONVERSATION_UUID/messages \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

---

## 🧪 Reclamation Tests

### Test 29: Create Reclamation
```bash
curl -X POST http://localhost:8080/api/reclamations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "subject": "Defective Product",
    "description": "The brake pads I received are damaged",
    "category": "PRODUCT"
  }'

# Expected: 201 CREATED
```

### Test 30: Get User Reclamations
```bash
curl -X GET http://localhost:8080/api/reclamations/user \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

---

## 🧪 Delivery Tests

### Test 31: Track Delivery
```bash
curl -X GET http://localhost:8080/api/deliveries/track/ORDER_UUID \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

### Test 32: Track by Tracking Number
```bash
curl -X GET http://localhost:8080/api/deliveries/track-number/TRK123456 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 200 OK
```

---

## 📊 Test Results Template

### Test Execution Log

| # | Test Name | Endpoint | Method | Expected | Actual | Status | Notes |
|---|-----------|----------|--------|----------|--------|--------|-------|
| 1 | Health Check | /actuator/health | GET | 200 | - | ⏳ | |
| 2 | Swagger UI | /swagger-ui.html | GET | 200 | - | ⏳ | |
| 3 | Register User | /api/auth/register | POST | 201 | - | ⏳ | |
| 4 | Login User | /api/auth/login | POST | 200 | - | ⏳ | |
| 5 | Get Profile | /api/users/profile | GET | 200 | - | ⏳ | |
| ... | ... | ... | ... | ... | ... | ... | |

### Summary Statistics
- **Total Tests:** 152+
- **Passed:** 0
- **Failed:** 0
- **Pending:** 152+
- **Success Rate:** 0%

---

## 🐛 Common Issues & Solutions

### Issue: 401 Unauthorized
**Solution:** Ensure JWT token is included in Authorization header

### Issue: 403 Forbidden
**Solution:** User doesn't have required role (CLIENT/ADMIN/SUPPORT)

### Issue: 404 Not Found
**Solution:** Verify resource UUID exists in database

### Issue: 400 Bad Request
**Solution:** Check request body validation, ensure all required fields present

### Issue: 500 Internal Server Error
**Solution:** Check application logs, verify database connection

---

## 📝 Notes

- Replace `YOUR_JWT_TOKEN` with actual token from login response
- Replace `PRODUCT_UUID`, `ORDER_UUID`, etc. with actual UUIDs from responses
- For admin endpoints, use admin credentials
- Some endpoints may require database to have initial data

---

**Next Steps:**
1. Execute Phase 1 tests (Health & Documentation)
2. Execute Phase 2 tests (Authentication)
3. Progressively test all phases
4. Document results
5. Fix any issues found
6. Re-test failed scenarios
