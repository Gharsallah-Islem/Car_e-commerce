# 🗄️ Database Schema Documentation

## Overview

The Car E-Commerce platform uses **PostgreSQL 15** as the primary database with UUID-based primary keys for all tables.

## Database Information

- **Database Name**: `ecommercespareparts`
- **DBMS**: PostgreSQL 15+
- **Character Set**: UTF-8
- **Timezone**: UTC

## Entity Relationship Diagram

```
┌─────────────┐         ┌──────────────┐
│    users    │────┬───▶│   vehicles   │
└──────┬──────┘    │    └──────────────┘
       │           │
       │           ├───▶│    carts     │
       │           │    └──────┬───────┘
       │           │           │
       │           │           ▼
       │           │    ┌──────────────┐
       │           │    │  cart_items  │
       │           │    └──────────────┘
       │           │
       │           ├───▶│    orders    │
       │           │    └──────┬───────┘
       │           │           │
       │           │           ▼
       │           │    ┌──────────────┐
       │           │    │ order_items  │
       │           │    └──────────────┘
       │           │
       │           └───▶│conversations │
       │                └──────┬───────┘
       │                       │
       │                       ▼
       │                ┌──────────────┐
       │                │   messages   │
       │                └──────────────┘
       │
       ├───────────────▶│ reclamations │
       │                └──────────────┘
       │
       └───────────────▶│recommendations│
                        └──────────────┘
```

## Tables

### 1. roles
Stores user role definitions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | SERIAL | PRIMARY KEY | Role ID |
| name | VARCHAR(50) | NOT NULL, UNIQUE | Role name (CLIENT, SUPPORT, ADMIN, SUPER_ADMIN) |

**Initial Data**:
- CLIENT
- SUPPORT
- ADMIN
- SUPER_ADMIN

---

### 2. users
Base table for all user types.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | User unique identifier |
| username | VARCHAR(100) | NOT NULL, UNIQUE | Username for login |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User email address |
| password | VARCHAR(255) | NOT NULL | Hashed password (BCrypt) |
| full_name | VARCHAR(255) | | User's full name |
| address | TEXT | | User's address |
| phone | VARCHAR(20) | | Phone number |
| role_id | INTEGER | FK → roles(id) | User role reference |
| created_at | TIMESTAMP | DEFAULT NOW() | Account creation timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_users_email` on `email`
- `idx_users_username` on `username`

---

### 3. vehicles
User vehicles for personalization.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Vehicle unique identifier |
| user_id | UUID | FK → users(id) | Vehicle owner |
| brand | VARCHAR(100) | NOT NULL | Vehicle brand |
| model | VARCHAR(100) | NOT NULL | Vehicle model |
| year | INTEGER | NOT NULL | Manufacturing year |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |

**Indexes**:
- `idx_vehicles_user_id` on `user_id`

---

### 4. products
Spare parts catalog.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Product unique identifier |
| name | VARCHAR(255) | NOT NULL | Product name |
| description | TEXT | | Product description |
| price | DECIMAL(10,2) | NOT NULL | Product price |
| stock | INTEGER | DEFAULT 0 | Available quantity |
| brand | VARCHAR(100) | | Compatible brand |
| model | VARCHAR(100) | | Compatible model |
| year | INTEGER | | Compatible year |
| compatibility | TEXT | | JSON array of compatible models |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_products_brand_model_year` on `(brand, model, year)`

---

### 5. carts
User shopping carts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Cart unique identifier |
| user_id | UUID | FK → users(id) | Cart owner |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

---

### 6. cart_items
Items in shopping carts.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Cart item unique identifier |
| cart_id | UUID | FK → carts(id) | Parent cart |
| product_id | UUID | FK → products(id) | Product reference |
| quantity | INTEGER | DEFAULT 1 | Quantity |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |

**Indexes**:
- `idx_cart_items_cart_id` on `cart_id`

---

### 7. orders
Customer orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Order unique identifier |
| user_id | UUID | FK → users(id) | Customer |
| order_date | TIMESTAMP | DEFAULT NOW() | Order creation date |
| status | VARCHAR(50) | DEFAULT 'PENDING' | Order status |
| total | DECIMAL(10,2) | NOT NULL | Order total amount |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

**Status Values**:
- PENDING
- CONFIRMED
- SHIPPED
- DELIVERED
- CANCELLED

**Indexes**:
- `idx_orders_user_id` on `user_id`

---

### 8. order_items
Items in orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Order item unique identifier |
| order_id | UUID | FK → orders(id) | Parent order |
| product_id | UUID | FK → products(id) | Product reference |
| quantity | INTEGER | NOT NULL | Quantity ordered |
| price | DECIMAL(10,2) | NOT NULL | Price at time of order |

**Indexes**:
- `idx_order_items_order_id` on `order_id`

---

### 9. deliveries
Delivery tracking with ONdelivery integration.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Delivery unique identifier |
| order_id | UUID | FK → orders(id) | Related order |
| status | VARCHAR(50) | DEFAULT 'PENDING' | Delivery status |
| address | TEXT | NOT NULL | Delivery address |
| ondelivery_tracking_id | VARCHAR(100) | | External tracking ID |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_deliveries_order_id` on `order_id`

---

### 10. reclamations
Customer complaints.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Reclamation unique identifier |
| user_id | UUID | FK → users(id) | Customer |
| description | TEXT | NOT NULL | Complaint description |
| status | VARCHAR(50) | DEFAULT 'PENDING' | Status |
| photos | JSONB | | Photo URLs array |
| videos | JSONB | | Video URLs array |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_reclamations_user_id` on `user_id`

---

### 11. conversations
Chat conversations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Conversation unique identifier |
| user_id | UUID | FK → users(id) | Customer |
| support_id | UUID | FK → users(id), NULL | Support agent |
| started_at | TIMESTAMP | DEFAULT NOW() | Start timestamp |
| updated_at | TIMESTAMP | DEFAULT NOW() | Last update timestamp |

**Indexes**:
- `idx_conversations_user_id` on `user_id`
- `idx_conversations_support_id` on `support_id`

---

### 12. messages
Chat messages.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Message unique identifier |
| conversation_id | UUID | FK → conversations(id) | Parent conversation |
| sender_id | UUID | FK → users(id) | Message sender |
| content | TEXT | NOT NULL | Message content |
| type | VARCHAR(50) | DEFAULT 'TEXT' | Message type |
| attachments | JSONB | | Attachment URLs |
| timestamp | TIMESTAMP | DEFAULT NOW() | Send timestamp |

**Indexes**:
- `idx_messages_conversation_id` on `conversation_id`

---

### 13. recommendations
AI-generated recommendations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Recommendation unique identifier |
| user_id | UUID | FK → users(id) | Target user |
| product_id | UUID | FK → products(id), NULL | Recommended product |
| message | TEXT | NOT NULL | Recommendation message |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |

**Indexes**:
- `idx_recommendations_user_id` on `user_id`

---

### 14. reports
Admin reports.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Report unique identifier |
| admin_id | UUID | FK → users(id) | Report creator |
| type | VARCHAR(50) | NOT NULL | Report type |
| data | JSONB | NOT NULL | Report data (JSON) |
| created_at | TIMESTAMP | DEFAULT NOW() | Creation timestamp |

**Report Types**:
- SALES
- STOCK
- USERS
- REVENUE

**Indexes**:
- `idx_reports_admin_id` on `admin_id`

---

### 15. stock_alerts
Low stock notifications.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | UUID | PRIMARY KEY | Alert unique identifier |
| product_id | UUID | FK → products(id) | Product reference |
| threshold | INTEGER | DEFAULT 5 | Stock threshold |
| alert_date | TIMESTAMP | DEFAULT NOW() | Alert timestamp |

**Indexes**:
- `idx_stock_alerts_product_id` on `product_id`

---

## Backup & Maintenance

### Backup Strategy
```bash
# Full backup
pg_dump -U lasmer -d ecommercespareparts > backup.sql

# Restore
psql -U lasmer -d ecommercespareparts < backup.sql
```

### Maintenance Tasks
- Weekly VACUUM ANALYZE
- Monthly index rebuild
- Daily automated backups

---

**Last Updated**: October 15, 2025
