# Backend Entities Implementation

## Overview
All 15 JPA entities have been successfully implemented with complete mappings, relationships, validation, and helper methods.

## Implemented Entities

### 1. **Role** (`roles` table)
- **Purpose**: Security roles for access control
- **Primary Key**: Integer (auto-increment)
- **Roles**: CLIENT, SUPPORT, ADMIN, SUPER_ADMIN
- **Features**: Role constants for easy reference

### 2. **User** (`users` table)
- **Purpose**: Client/customer accounts
- **Primary Key**: UUID
- **Key Fields**: username, email, password (hashed), fullName, address, phone
- **Relationships**:
  - ManyToOne → Role
  - OneToMany → Vehicle, Order, Reclamation, Conversation, Recommendation
  - OneToOne → Cart
- **Validation**: Email format, username/password length
- **Helper Methods**: isClient(), isAdmin(), isSuperAdmin(), isSupport()

### 3. **Admin** (`admins` table)
- **Purpose**: Administrator accounts with permissions
- **Primary Key**: UUID
- **Key Fields**: username, email, password, fullName, permissions (JSON), isActive
- **Features**: Activate/deactivate methods
- **Timestamps**: createdAt, updatedAt

### 4. **SuperAdmin** (`super_admins` table)
- **Purpose**: Super administrator accounts (manage admins)
- **Primary Key**: UUID
- **Key Fields**: username, email, password, fullName, isActive
- **Features**: Same as Admin with highest privileges
- **Timestamps**: createdAt, updatedAt

### 5. **Vehicle** (`vehicles` table)
- **Purpose**: User's registered vehicles for compatibility checking
- **Primary Key**: UUID
- **Key Fields**: brand, model, year
- **Relationships**: ManyToOne → User
- **Validation**: Year >= 1900
- **Indexes**: user_id for fast queries

### 6. **Product** (`products` table)
- **Purpose**: Car spare parts inventory
- **Primary Key**: UUID
- **Key Fields**: 
  - name, description, price, stock
  - brand, model, year (for compatibility)
  - compatibility (JSON array), category, imageUrl
- **Relationships**: 
  - OneToMany → CartItem, OrderItem
- **Validation**: Price > 0, Stock >= 0
- **Helper Methods**: 
  - isInStock(), isLowStock()
  - decreaseStock(), increaseStock()
- **Indexes**: (brand, model, year) composite index
- **Timestamps**: createdAt, updatedAt

### 7. **Cart** (`carts` table)
- **Purpose**: Shopping cart per user
- **Primary Key**: UUID
- **Relationships**: 
  - OneToOne → User
  - OneToMany → CartItem
- **Helper Methods**: 
  - getTotalPrice() - calculates total from all items
  - getTotalItems() - counts total quantity
  - clearCart() - removes all items
- **Indexes**: user_id (unique)
- **Timestamps**: createdAt, updatedAt

### 8. **CartItem** (`cart_items` table)
- **Purpose**: Individual items in shopping cart
- **Primary Key**: UUID
- **Key Fields**: quantity
- **Relationships**: 
  - ManyToOne → Cart
  - ManyToOne → Product (EAGER fetch)
- **Validation**: Quantity >= 1
- **Helper Methods**: 
  - getSubtotal() - price × quantity
  - increaseQuantity(), decreaseQuantity()
- **Indexes**: cart_id, product_id

### 9. **Order** (`orders` table)
- **Purpose**: Customer orders with ONdelivery integration
- **Primary Key**: UUID
- **Key Fields**: 
  - totalPrice, status, deliveryAddress
  - paymentMethod, paymentStatus
  - trackingNumber (ONdelivery), notes
- **Relationships**: 
  - ManyToOne → User
  - OneToMany → OrderItem
  - OneToOne → Delivery
- **Status Values**: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
- **Payment Status**: PENDING, COMPLETED, FAILED
- **Helper Methods**: isPending(), isDelivered(), markAsDelivered()
- **Indexes**: user_id, status, created_at
- **Timestamps**: createdAt, updatedAt, deliveredAt

### 10. **OrderItem** (`order_items` table)
- **Purpose**: Products in an order (snapshot at purchase time)
- **Primary Key**: UUID
- **Key Fields**: quantity, price (at time of order)
- **Relationships**: 
  - ManyToOne → Order
  - ManyToOne → Product
- **Helper Methods**: getSubtotal()
- **Indexes**: order_id, product_id

### 11. **Delivery** (`deliveries` table)
- **Purpose**: ONdelivery integration for order tracking
- **Primary Key**: UUID
- **Key Fields**: 
  - trackingNumber (unique), status
  - deliveryAddress, deliveryNotes
  - estimatedDelivery, actualDelivery
  - driverName, driverPhone
- **Relationships**: OneToOne → Order
- **Status Values**: PROCESSING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED
- **Helper Methods**: isDelivered(), markAsDelivered()
- **Indexes**: order_id, status
- **Timestamps**: createdAt, updatedAt

### 12. **Reclamation** (`reclamations` table)
- **Purpose**: Customer complaints and support tickets
- **Primary Key**: UUID
- **Key Fields**: 
  - subject, description, status
  - response, attachmentUrl (photo/video evidence)
- **Relationships**: ManyToOne → User
- **Status Values**: OPEN, IN_PROGRESS, RESOLVED, CLOSED
- **Helper Methods**: isOpen(), isResolved(), markAsResolved()
- **Indexes**: user_id, status, created_at
- **Timestamps**: createdAt, updatedAt, resolvedAt

### 13. **Conversation** (`conversations` table)
- **Purpose**: Chat conversations (Messenger-style support)
- **Primary Key**: UUID
- **Key Fields**: title, isActive
- **Relationships**: 
  - ManyToOne → User
  - OneToMany → Message
- **Helper Methods**: 
  - closeConversation(), reopenConversation()
  - getLastMessage()
- **Indexes**: user_id, updated_at
- **Timestamps**: createdAt, updatedAt

### 14. **Message** (`messages` table)
- **Purpose**: Individual chat messages with WebSocket support
- **Primary Key**: UUID
- **Key Fields**: 
  - senderId, senderType (USER/SUPPORT/ADMIN)
  - content, attachmentUrl, isRead
- **Relationships**: ManyToOne → Conversation
- **Helper Methods**: markAsRead(), isFromUser(), isFromSupport()
- **Indexes**: conversation_id, created_at
- **Timestamp**: createdAt

### 15. **Recommendation** (`recommendations` table)
- **Purpose**: AI-generated product recommendations
- **Primary Key**: UUID
- **Key Fields**: 
  - imageUrl (uploaded by user)
  - symptoms (user description)
  - aiResponse, suggestedProducts (JSON)
  - confidenceScore (0.0 to 1.0)
- **Relationships**: ManyToOne → User
- **Helper Methods**: isHighConfidence() (>= 0.8)
- **Indexes**: user_id, created_at
- **Timestamp**: createdAt

### 16. **Report** (`reports` table)
- **Purpose**: Analytics and admin reports (CSV/PDF export)
- **Primary Key**: UUID
- **Key Fields**: 
  - reportType, title, description
  - data (JSON), fileUrl
  - generatedBy (admin UUID)
- **Report Types**: SALES, INVENTORY, USERS, ORDERS, ANALYTICS
- **Indexes**: report_type, created_at
- **Timestamp**: createdAt

## Key Design Patterns

### 1. **UUID Primary Keys**
All entities (except Role) use UUID for distributed system compatibility and security.

### 2. **Soft Deletes**
Admin and SuperAdmin entities have `isActive` flag for soft deletion.

### 3. **Audit Fields**
Most entities include:
- `@CreationTimestamp` - automatic creation time
- `@UpdateTimestamp` - automatic update time

### 4. **Cascade Operations**
- `CascadeType.ALL` with `orphanRemoval=true` for dependent entities
- Ensures data integrity when parent entities are deleted

### 5. **Fetch Strategies**
- **LAZY**: Default for most relationships to avoid N+1 queries
- **EAGER**: Used for Role and Product in specific cases

### 6. **Validation**
- `@NotBlank`, `@NotNull` for required fields
- `@Email` for email validation
- `@Size`, `@Min`, `@DecimalMin` for constraints
- Custom validation in helper methods

### 7. **Indexes**
Strategic indexes on:
- Foreign keys (user_id, product_id, etc.)
- Frequently queried fields (status, created_at)
- Composite indexes for complex queries

### 8. **JSON Fields**
Using TEXT columns for flexible JSON storage:
- `compatibility` in Product
- `permissions` in Admin
- `suggestedProducts` in Recommendation
- `data` in Report

## Business Logic Helper Methods

### Cart Management
- `Cart.getTotalPrice()` - aggregate calculation
- `CartItem.getSubtotal()` - line item total

### Order Processing
- `Order.markAsDelivered()` - status update with timestamp
- `Product.decreaseStock()` - inventory management

### Status Checks
- `User.isAdmin()` - role verification
- `Order.isPending()` - order state
- `Reclamation.isResolved()` - ticket state

## Database Compatibility

### PostgreSQL Specific
- UUID data type: `@Column(columnDefinition = "UUID")`
- TEXT for long strings: `columnDefinition = "TEXT"`
- Proper index definitions with `@Index`

### JPA/Hibernate Configuration
- Strategy: `GenerationType.AUTO` for UUID generation
- Dialect: PostgreSQLDialect (configured in application.yml)
- DDL: `hibernate.ddl-auto=validate` (production-safe)

## Next Steps

1. ✅ **Entities Implementation** - COMPLETED
2. ⏳ **Repository Layer** - Add custom queries
3. ⏳ **Service Layer** - Implement business logic
4. ⏳ **Controller Layer** - Create REST APIs
5. ⏳ **Security Configuration** - JWT implementation
6. ⏳ **Testing** - Unit and integration tests

## Build & Test

To verify the entity implementation:

```powershell
cd Backend
mvn clean compile
```

All entities should compile without errors and be ready for repository implementation.
