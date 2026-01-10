# Database Documentation

> PostgreSQL database schema for the AutoParts Store e-commerce platform

## Table of Contents
- [Overview](#overview)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Tables Reference](#tables-reference)
- [Indexes](#indexes)
- [Data Lifecycle](#data-lifecycle)

---

## Overview

| Attribute | Value |
|-----------|-------|
| **Database** | PostgreSQL 15+ |
| **ORM** | Spring Data JPA / Hibernate |
| **Strategy** | `hibernate.ddl-auto=update` |
| **Naming** | Snake case (e.g., `order_items`) |
| **IDs** | UUID (auto-generated) |
| **Timestamps** | LocalDateTime with auto-generation |

---

## Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              USER MANAGEMENT                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────┐     1:N     ┌─────────────┐                                        │
│  │  Role   │◄────────────│    User     │                                        │
│  └─────────┘             └──────┬──────┘                                        │
│                                 │                                                │
│         ┌───────────────────────┼───────────────────────┐                       │
│         │                       │                       │                       │
│         ▼ 1:N                   ▼ 1:1                   ▼ 1:N                   │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐               │
│  │   Vehicle   │         │    Cart     │         │   Address   │               │
│  └─────────────┘         └──────┬──────┘         └─────────────┘               │
│                                 │                                                │
│                                 ▼ 1:N                                            │
│                          ┌─────────────┐                                        │
│                          │  CartItem   │                                        │
│                          └──────┬──────┘                                        │
│                                 │                                                │
│                                 ▼ N:1                                            │
│                          ┌─────────────┐                                        │
│                          │   Product   │                                        │
│                          └─────────────┘                                        │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRODUCT CATALOG                                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────┐    N:1     ┌─────────────┐    N:1     ┌──────────┐               │
│  │ Category │◄───────────│   Product   │───────────►│  Brand   │               │
│  └──────────┘            └──────┬──────┘            └──────────┘               │
│                                 │                                                │
│                                 │ 1:N                                            │
│                                 ▼                                                │
│                          ┌─────────────┐                                        │
│                          │ProductImage │                                        │
│                          └─────────────┘                                        │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ORDER MANAGEMENT                                    │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────┐     N:1     ┌─────────────┐     1:N     ┌───────────┐             │
│  │  User   │◄────────────│    Order    │────────────►│ OrderItem │             │
│  └─────────┘             └──────┬──────┘             └─────┬─────┘             │
│                                 │                          │                    │
│                                 │ 1:1                      ▼ N:1                │
│                                 ▼                    ┌───────────┐             │
│                          ┌─────────────┐             │  Product  │             │
│                          │  Delivery   │             └───────────┘             │
│                          └──────┬──────┘                                        │
│                                 │                                                │
│                                 ▼ N:1                                            │
│                          ┌─────────────┐                                        │
│                          │   Driver    │                                        │
│                          └─────────────┘                                        │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                           INVENTORY MANAGEMENT                                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────┐    1:N     ┌───────────────┐    N:1     ┌──────────┐             │
│  │ Supplier │◄───────────│ PurchaseOrder │────────────│ Product  │             │
│  └──────────┘            └───────┬───────┘            └──────────┘             │
│                                  │                                              │
│                                  │ 1:N                                          │
│                                  ▼                                              │
│                          ┌─────────────────┐                                    │
│                          │PurchaseOrderItem│                                    │
│                          └─────────────────┘                                    │
│                                                                                  │
│  ┌──────────┐    N:1     ┌───────────────┐                                     │
│  │ Product  │◄───────────│ StockMovement │                                     │
│  └──────────┘            └───────────────┘                                     │
│                                                                                  │
│  ┌──────────┐    1:1     ┌───────────────┐    N:1     ┌──────────┐             │
│  │ Product  │◄───────────│ReorderSetting │───────────►│ Supplier │             │
│  └──────────┘            └───────────────┘            └──────────┘             │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SUPPORT SYSTEM                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────┐     N:1     ┌─────────────┐                                        │
│  │  User   │◄────────────│ Reclamation │                                        │
│  └─────────┘             └─────────────┘                                        │
│                                                                                  │
│  ┌─────────┐     N:1     ┌─────────────┐     1:N     ┌─────────────┐           │
│  │  User   │◄────────────│Conversation │────────────►│   Message   │           │
│  └─────────┘             └─────────────┘             └─────────────┘           │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## Tables Reference

### users
Primary user table for all account types.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `username` | VARCHAR(100) | UNIQUE, NOT NULL | Login username |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Email address |
| `password` | VARCHAR(255) | NOT NULL | BCrypt hash |
| `full_name` | VARCHAR(255) | - | Display name |
| `phone` | VARCHAR(20) | - | Contact number |
| `address` | TEXT | - | Default address |
| `profile_picture` | TEXT | - | Image URL/Base64 |
| `role_id` | UUID | FK → roles | User role |
| `is_email_verified` | BOOLEAN | DEFAULT false | Verification status |
| `is_active` | BOOLEAN | DEFAULT true | Account status |
| `email_verification_token` | VARCHAR(255) | - | Verification token |
| `email_verification_token_expiry` | TIMESTAMP | - | Token expiry |
| `password_reset_token` | VARCHAR(255) | - | Reset token |
| `password_reset_token_expiry` | TIMESTAMP | - | Token expiry |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |

---

### roles
User role definitions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `name` | VARCHAR(50) | UNIQUE, NOT NULL | Role name |

**Default Roles:**
- `CLIENT` - Regular customers
- `ADMIN` - Platform administrators  
- `SUPER_ADMIN` - Full access including role management
- `SUPPORT` - Customer support team
- `DRIVER` - Delivery drivers

---

### products
Product catalog.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `name` | VARCHAR(255) | NOT NULL | Product name |
| `description` | TEXT | - | Full description |
| `price` | DECIMAL(10,2) | NOT NULL | Unit price |
| `stock` | INTEGER | NOT NULL, DEFAULT 0 | Available quantity |
| `sku` | VARCHAR(100) | UNIQUE | Stock keeping unit |
| `category_id` | UUID | FK → categories | Product category |
| `brand_id` | UUID | FK → brands | Product brand |
| `is_active` | BOOLEAN | DEFAULT true | Listing status |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |

**Indexes:**
- `idx_products_category_id`
- `idx_products_brand_id`
- `idx_products_sku`

---

### categories
Product categories.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `name` | VARCHAR(100) | UNIQUE, NOT NULL | Category name |
| `description` | TEXT | - | Category description |

---

### brands
Product brands/manufacturers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `name` | VARCHAR(100) | UNIQUE, NOT NULL | Brand name |
| `logo` | TEXT | - | Brand logo URL |

---

### orders
Customer orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `user_id` | UUID | FK → users, NOT NULL | Customer |
| `total_price` | DECIMAL(10,2) | NOT NULL | Order total |
| `status` | VARCHAR(50) | NOT NULL | Order status |
| `delivery_address` | TEXT | - | Shipping address |
| `payment_method` | VARCHAR(50) | - | STRIPE / CASH_ON_DELIVERY |
| `payment_status` | VARCHAR(50) | - | Payment state |
| `tracking_number` | VARCHAR(255) | - | Delivery tracking |
| `notes` | TEXT | - | Customer notes |
| `created_at` | TIMESTAMP | AUTO | Order timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |
| `delivered_at` | TIMESTAMP | - | Completion time |

**Indexes:**
- `idx_orders_user_id`
- `idx_orders_status`
- `idx_orders_created_at`

---

### order_items
Order line items.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `order_id` | UUID | FK → orders, NOT NULL | Parent order |
| `product_id` | UUID | FK → products, NOT NULL | Product |
| `quantity` | INTEGER | NOT NULL | Item quantity |
| `price` | DECIMAL(10,2) | NOT NULL | Unit price at time of order |

---

### deliveries
Delivery tracking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `order_id` | UUID | FK → orders, UNIQUE, NOT NULL | Associated order |
| `tracking_number` | VARCHAR(255) | UNIQUE, NOT NULL | Tracking ID |
| `status` | VARCHAR(50) | NOT NULL | Delivery status |
| `address` | TEXT | - | Delivery address |
| `delivery_notes` | TEXT | - | Special instructions |
| `driver_id` | UUID | FK → drivers | Assigned driver |
| `driver_name` | VARCHAR(255) | - | Driver name (snapshot) |
| `driver_phone` | VARCHAR(20) | - | Driver phone (snapshot) |
| `current_latitude` | DOUBLE | - | GPS latitude |
| `current_longitude` | DOUBLE | - | GPS longitude |
| `current_location` | TEXT | - | Address description |
| `estimated_delivery` | TIMESTAMP | - | ETA |
| `actual_delivery` | TIMESTAMP | - | Completion time |
| `pickup_time` | TIMESTAMP | - | Driver pickup time |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |

**Indexes:**
- `idx_deliveries_order_id`
- `idx_deliveries_status`

---

### drivers
Delivery personnel.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `user_id` | UUID | FK → users | Linked user account |
| `name` | VARCHAR(255) | NOT NULL | Driver name |
| `email` | VARCHAR(255) | UNIQUE | Email address |
| `phone` | VARCHAR(20) | NOT NULL | Contact number |
| `license_number` | VARCHAR(100) | UNIQUE | Driving license |
| `vehicle_type` | VARCHAR(100) | - | Vehicle category |
| `vehicle_plate` | VARCHAR(50) | - | License plate |
| `status` | VARCHAR(50) | DEFAULT 'OFFLINE' | Availability |
| `current_latitude` | DOUBLE | - | GPS latitude |
| `current_longitude` | DOUBLE | - | GPS longitude |
| `is_active` | BOOLEAN | DEFAULT true | Account status |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |

---

### carts
Shopping carts (one per user).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `user_id` | UUID | FK → users, UNIQUE, NOT NULL | Cart owner |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |

---

### cart_items
Cart line items.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `cart_id` | UUID | FK → carts, NOT NULL | Parent cart |
| `product_id` | UUID | FK → products, NOT NULL | Product |
| `quantity` | INTEGER | NOT NULL | Item quantity |

---

### suppliers
Inventory suppliers/distributors.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `name` | VARCHAR(255) | NOT NULL | Company name |
| `contact_person` | VARCHAR(255) | - | Contact name |
| `email` | VARCHAR(255) | - | Email |
| `phone` | VARCHAR(20) | - | Phone |
| `address` | TEXT | - | Business address |
| `is_active` | BOOLEAN | DEFAULT true | Status |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |

---

### purchase_orders
Orders to suppliers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `supplier_id` | UUID | FK → suppliers, NOT NULL | Vendor |
| `order_number` | VARCHAR(100) | UNIQUE | PO number |
| `status` | VARCHAR(50) | NOT NULL | Order status |
| `total_amount` | DECIMAL(12,2) | - | Order total |
| `order_date` | TIMESTAMP | - | Order timestamp |
| `expected_delivery` | TIMESTAMP | - | Expected arrival |
| `notes` | TEXT | - | Order notes |
| `created_at` | TIMESTAMP | AUTO | Creation timestamp |
| `updated_at` | TIMESTAMP | AUTO | Last update |

---

### stock_movements
Inventory transaction log.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `product_id` | UUID | FK → products, NOT NULL | Affected product |
| `type` | VARCHAR(50) | NOT NULL | IN / OUT / ADJUSTMENT |
| `quantity` | INTEGER | NOT NULL | Change amount |
| `reason` | TEXT | - | Movement reason |
| `reference_id` | UUID | - | Related order/PO |
| `created_by` | UUID | FK → users | Who made change |
| `created_at` | TIMESTAMP | AUTO | Timestamp |

---

### reorder_settings
Automatic reorder rules.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `product_id` | UUID | FK → products, UNIQUE, NOT NULL | Target product |
| `supplier_id` | UUID | FK → suppliers | Preferred supplier |
| `minimum_stock` | INTEGER | NOT NULL | Low stock threshold |
| `reorder_quantity` | INTEGER | NOT NULL | Auto-order quantity |
| `is_auto_reorder` | BOOLEAN | DEFAULT false | Automation enabled |
| `last_reorder_date` | TIMESTAMP | - | Last auto-order |

---

### reclamations
Support tickets.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `user_id` | UUID | FK → users, NOT NULL | Customer |
| `order_id` | UUID | FK → orders | Related order |
| `subject` | VARCHAR(255) | NOT NULL | Ticket title |
| `description` | TEXT | NOT NULL | Issue details |
| `category` | VARCHAR(100) | - | Ticket category |
| `status` | VARCHAR(50) | DEFAULT 'OPEN' | Ticket status |
| `priority` | VARCHAR(50) | DEFAULT 'MEDIUM' | Priority level |
| `assigned_to` | UUID | FK → users | Support agent |
| `created_at` | TIMESTAMP | AUTO | Submission time |
| `updated_at` | TIMESTAMP | AUTO | Last update |
| `resolved_at` | TIMESTAMP | - | Resolution time |

---

### conversations
Chat conversations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `user_id` | UUID | FK → users, NOT NULL | Customer |
| `status` | VARCHAR(50) | DEFAULT 'ACTIVE' | Conversation status |
| `started_at` | TIMESTAMP | AUTO | Start time |
| `ended_at` | TIMESTAMP | - | End time |

---

### messages
Chat messages.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | PK | Primary key |
| `conversation_id` | UUID | FK → conversations, NOT NULL | Parent conversation |
| `content` | TEXT | NOT NULL | Message text |
| `sender` | VARCHAR(50) | NOT NULL | USER / AI / SUPPORT |
| `created_at` | TIMESTAMP | AUTO | Timestamp |

---

## Indexes

### Primary Indexes (Automatic)
All tables have primary key indexes on their `id` columns.

### Foreign Key Indexes
```sql
-- Users
CREATE INDEX idx_users_role_id ON users(role_id);

-- Products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_brand_id ON products(brand_id);
CREATE INDEX idx_products_sku ON products(sku);

-- Orders
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- Deliveries
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);
CREATE INDEX idx_deliveries_status ON deliveries(status);
CREATE INDEX idx_deliveries_driver_id ON deliveries(driver_id);

-- Cart Items
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- Stock Movements
CREATE INDEX idx_stock_movements_product_id ON stock_movements(product_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);
```

---

## Data Lifecycle

### Order Lifecycle

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                                                                               │
│    ┌─────────┐   Payment   ┌───────────┐   Shipped   ┌─────────┐   Done     │
│    │ PENDING │────────────►│ CONFIRMED │────────────►│ SHIPPED │──────────┐ │
│    └────┬────┘             └───────────┘             └────┬────┘          │ │
│         │                                                 │               │ │
│         │ Cancel                                          │ Fail          ▼ │
│         ▼                                                 ▼         ┌─────────┐
│    ┌───────────┐                                    ┌───────────────────┐│ │
│    │ CANCELLED │                                    │ DELIVERY_FAILED   ││DELIVERED│
│    └───────────┘                                    └───────────────────┘└─────────┘
│                                                                               │
└──────────────────────────────────────────────────────────────────────────────┘
```

**State Transitions:**
1. `PENDING` → `CONFIRMED`: Payment completed
2. `CONFIRMED` → `SHIPPED`: Driver assigned, delivery created
3. `SHIPPED` → `DELIVERED`: Delivery completed
4. `PENDING` → `CANCELLED`: Customer cancellation
5. `SHIPPED` → `DELIVERY_FAILED`: Delivery attempt failed

### Delivery Lifecycle

```
PROCESSING → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                    ↓
                                  FAILED
```

### Stock Movement Types

| Type | Description | Stock Effect |
|------|-------------|--------------|
| `IN` | Stock received (purchase orders, returns) | +quantity |
| `OUT` | Stock sold (orders fulfilled) | -quantity |
| `ADJUSTMENT` | Manual correction (inventory count, damage) | ±quantity |

### Auto-Reorder Process

```
1. Stock falls below minimum_stock threshold
2. System checks if is_auto_reorder = true
3. Creates PurchaseOrder with reorder_quantity
4. Sends notification to admin
5. Updates last_reorder_date
```

---

## Database Maintenance

### Backup Strategy
```bash
# Full backup
pg_dump -U postgres -h localhost ecommercespareparts > backup.sql

# Restore
psql -U postgres -h localhost ecommercespareparts < backup.sql
```

### Common Queries

**Low Stock Products:**
```sql
SELECT p.name, p.stock, rs.minimum_stock
FROM products p
JOIN reorder_settings rs ON p.id = rs.product_id
WHERE p.stock < rs.minimum_stock;
```

**Daily Order Summary:**
```sql
SELECT DATE(created_at) as date,
       COUNT(*) as orders,
       SUM(total_price) as revenue
FROM orders
WHERE status != 'CANCELLED'
GROUP BY DATE(created_at)
ORDER BY date DESC;
```

**Top Selling Products:**
```sql
SELECT p.name, SUM(oi.quantity) as sold
FROM products p
JOIN order_items oi ON p.id = oi.product_id
JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'DELIVERED'
GROUP BY p.id
ORDER BY sold DESC
LIMIT 10;
```
