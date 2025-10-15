# Database Configuration Guide

## PostgreSQL Database Setup

### Database Details
- **Database Name**: `ecommercespareparts`
- **Username**: `lasmer`
- **Password**: `lasmer`
- **Host**: `localhost`
- **Port**: `5432`

### Connection Configuration

The application has been configured to connect to your PostgreSQL database with the following settings:

#### Main Configuration (`application.yml`)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommercespareparts
    username: lasmer
    password: lasmer
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Set to 'validate' since you already created the schema
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

#### Development Profile (`application-dev.yml`)
Same credentials for development environment.

#### Production Profile (`application-prod.yml`)
Uses environment variables with fallback defaults.

### Database Schema Overview

Based on your SQL script, the following tables have been created:

1. **roles** - User roles (CLIENT, SUPPORT, ADMIN, SUPER_ADMIN)
2. **users** - Base table for all users (UUID primary key)
3. **vehicles** - User vehicles for personalization
4. **products** - Spare parts catalog
5. **carts** - Shopping carts
6. **cart_items** - Cart line items
7. **orders** - Customer orders
8. **order_items** - Order line items
9. **deliveries** - ONdelivery integration
10. **reclamations** - Customer complaints
11. **conversations** - Chat conversations
12. **messages** - Chat messages
13. **recommendations** - AI-generated suggestions
14. **reports** - Admin reports
15. **stock_alerts** - Low stock notifications

### Important Notes

1. **UUID Primary Keys**: All tables use UUID as primary keys. Make sure to use `UUID` type in your JPA entities.

2. **Hibernate DDL Mode**: Set to `validate` to ensure Hibernate validates the schema against your existing database without modifying it.

3. **Initial Data**: The SQL script already inserted:
   - 4 roles (CLIENT, SUPPORT, ADMIN, SUPER_ADMIN)
   - 1 super admin user

### Dependencies Added

The following dependencies have been added to `pom.xml`:

1. **PostgreSQL Driver** - Database connectivity
2. **JWT (jjwt)** - JSON Web Token for authentication
3. **WebSocket** - For real-time chat functionality
4. **Validation** - Bean validation
5. **Mail** - Email functionality
6. **Lombok** - Reduce boilerplate code
7. **SpringDoc OpenAPI** - API documentation (Swagger UI)

### Next Steps

1. **Run Maven Clean Install**:
   ```bash
   mvn clean install
   ```

2. **Verify Database Connection**:
   - Make sure PostgreSQL is running
   - Ensure the database `ecommercespareparts` exists
   - Test connection with credentials: lasmer/lasmer

3. **Start Implementing Entities**:
   - Update entity classes to match the database schema
   - Use UUID for primary keys
   - Add proper JPA annotations and relationships

### Swagger UI Access

Once the application is running, access API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Database Connection Test

You can test the connection by running:
```bash
psql -h localhost -U lasmer -d ecommercespareparts
```

---
**Created**: October 15, 2025  
**Database**: PostgreSQL  
**Schema**: Already created via SQL script
the code:
-- Full PostgreSQL Database Creation Script for the Application
-- Database: ecommercespareparts
-- Created on: October 15, 2025
-- Note: This script creates all tables, columns, relations, and indexes needed based on the project requirements.
-- Eliminated audio-related features, focusing on image, chatbot, recommendations, AR (optional), delivery (ONdelivery), and chat.

-- Drop database if exists (for development)
DROP DATABASE IF EXISTS ecommercespareparts;

-- Create database
CREATE DATABASE ecommercespareparts;

-- Connect to the database
\c ecommercespareparts;

-- Enable extensions if needed (e.g., for UUID)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: roles (for user roles: CLIENT, SUPPORT, ADMIN, SUPER_ADMIN)
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE  -- e.g., 'CLIENT', 'SUPPORT', 'ADMIN', 'SUPER_ADMIN'
);

-- Table: users (base for clients, support, admins, super admins)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hashed password
    full_name VARCHAR(255),
    address TEXT,
    phone VARCHAR(20),
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: vehicles (linked to users for personalization)
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for vehicles
CREATE INDEX idx_vehicles_user_id ON vehicles(user_id);

-- Table: products (spare parts)
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    brand VARCHAR(100),
    model VARCHAR(100),
    year INTEGER,
    compatibility TEXT,  -- e.g., JSON array of compatible models
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for products
CREATE INDEX idx_products_brand_model_year ON products(brand, model, year);

-- Table: carts (user shopping carts)
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table: cart_items (products in carts)
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for cart_items
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);

-- Table: orders (user orders)
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- e.g., PENDING, SHIPPED, DELIVERED, CANCELLED
    total DECIMAL(10, 2) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for orders
CREATE INDEX idx_orders_user_id ON orders(user_id);

-- Table: order_items (products in orders)
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL,
    price DECIMAL(10, 2) NOT NULL  -- Price at time of order
);

-- Index for order_items
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Table: deliveries (integrated with ONdelivery)
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- e.g., IN_TRANSIT, DELIVERED
    address TEXT NOT NULL,
    ondelivery_tracking_id VARCHAR(100),  -- External ID from ONdelivery API
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for deliveries
CREATE INDEX idx_deliveries_order_id ON deliveries(order_id);

-- Table: reclamations (user complaints)
CREATE TABLE reclamations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- e.g., PENDING, RESOLVED
    photos JSONB,  -- Array of photo URLs
    videos JSONB,  -- Array of video URLs
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for reclamations
CREATE INDEX idx_reclamations_user_id ON reclamations(user_id);

-- Table: conversations (for chat system)
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    support_id UUID REFERENCES users(id) ON DELETE SET NULL,  -- Support user (can be null if not assigned)
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for conversations
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_support_id ON conversations(support_id);

-- Table: messages (chat messages)
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'TEXT',  -- e.g., TEXT, IMAGE, VIDEO
    attachments JSONB,  -- Array of attachment URLs
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for messages
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);

-- Table: recommendations (IA-generated suggestions)
CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    message TEXT NOT NULL,  -- e.g., "Replace timing belt in 5000 km"
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for recommendations
CREATE INDEX idx_recommendations_user_id ON recommendations(user_id);

-- Table: reports (admin reports)
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    admin_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    type VARCHAR(50) NOT NULL,  -- e.g., SALES, STOCK
    data JSONB NOT NULL,  -- Report data as JSON
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for reports
CREATE INDEX idx_reports_admin_id ON reports(admin_id);

-- Table: stock_alerts (low stock notifications)
CREATE TABLE stock_alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    threshold INTEGER NOT NULL DEFAULT 5,
    alert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for stock_alerts
CREATE INDEX idx_stock_alerts_product_id ON stock_alerts(product_id);

-- Initial Data Insertion (Optional, for development)
-- Insert roles
INSERT INTO roles (name) VALUES ('CLIENT'), ('SUPPORT'), ('ADMIN'), ('SUPER_ADMIN');

-- Example super admin user
INSERT INTO users (id, username, email, password, full_name, role_id) 
VALUES (uuid_generate_v4(), 'superadmin', 'superadmin@example.com', 'hashed_password', 'Super Admin', (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'));

COMMIT;

-- End of Script