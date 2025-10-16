# Entity Relationship Diagram

## Core Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                      AUTHENTICATION & AUTHORIZATION                  │
└──────────────────────────────────────────────────────────────────────┘

    ┌─────────┐
    │  Role   │──────┐
    └─────────┘      │
         │           │ ManyToOne
         │           │
         ▼           │
    ┌─────────┐◄────┘         ┌──────────────┐        ┌──────────────┐
    │  User   │               │    Admin     │        │  SuperAdmin  │
    └─────────┘               └──────────────┘        └──────────────┘
         │                     (Separate auth)         (Separate auth)
         │
         └──────┬──────┬──────┬──────┬──────┬──────┐
                │      │      │      │      │      │
                │      │      │      │      │      │
                ▼      ▼      ▼      ▼      ▼      ▼

┌──────────────────────────────────────────────────────────────────────┐
│                      E-COMMERCE FUNCTIONALITY                        │
└──────────────────────────────────────────────────────────────────────┘

    ┌──────────┐
    │ Vehicle  │ (User's cars)
    └──────────┘
    
    ┌──────────┐      ┌──────────┐
    │   Cart   │◄────►│ CartItem │◄───┐
    └──────────┘      └──────────┘    │
    (OneToOne)        (ManyToOne)     │
                                      │
                      ┌───────────┐   │
                      │  Product  │◄──┤
                      └───────────┘   │
                           │          │
                           │          │
                           ▼          │
    ┌──────────┐      ┌───────────┐  │
    │  Order   │◄────►│ OrderItem │◄─┘
    └──────────┘      └───────────┘
         │            (ManyToOne)
         │
         ▼
    ┌──────────┐
    │ Delivery │ (ONdelivery integration)
    └──────────┘
    (OneToOne)

┌──────────────────────────────────────────────────────────────────────┐
│                      CUSTOMER SUPPORT SYSTEM                         │
└──────────────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │ Reclamation  │ (Support tickets)
    └──────────────┘
    
    ┌──────────────┐      ┌──────────┐
    │ Conversation │◄────►│ Message  │ (WebSocket chat)
    └──────────────┘      └──────────┘
    (OneToMany)           (ManyToOne)

┌──────────────────────────────────────────────────────────────────────┐
│                      AI INTEGRATION & ANALYTICS                      │
└──────────────────────────────────────────────────────────────────────┘

    ┌────────────────┐
    │ Recommendation │ (AI-powered product suggestions)
    └────────────────┘
    
    ┌──────────┐
    │  Report  │ (Admin analytics, CSV/PDF export)
    └──────────┘
```

## Detailed Relationship Mapping

### User Entity (Central Hub)
```
User (UUID)
├── Role (ManyToOne) ────────────► Role (Integer)
├── Cart (OneToOne) ─────────────► Cart (UUID)
├── Vehicles (OneToMany) ────────► Vehicle (UUID)
├── Orders (OneToMany) ──────────► Order (UUID)
├── Reclamations (OneToMany) ───► Reclamation (UUID)
├── Conversations (OneToMany) ──► Conversation (UUID)
└── Recommendations (OneToMany) ► Recommendation (UUID)
```

### Product Entity (Inventory Core)
```
Product (UUID)
├── CartItems (OneToMany) ───────► CartItem (UUID)
│                                   └── Cart (ManyToOne)
│
└── OrderItems (OneToMany) ──────► OrderItem (UUID)
                                    └── Order (ManyToOne)
```

### Order Processing Flow
```
Cart + CartItems
       │
       ▼ (Checkout)
Order + OrderItems
       │
       ▼ (Shipping)
Delivery (ONdelivery API)
```

### Support System Flow
```
User Issue
   │
   ├──► Reclamation (Ticket system)
   │       └── Status: OPEN → IN_PROGRESS → RESOLVED → CLOSED
   │
   └──► Conversation (Real-time chat)
           └── Messages (WebSocket)
                ├── Attachments (photos/videos)
                └── Read status tracking
```

### AI Integration Points
```
User uploads image/describes symptom
              │
              ▼
       Recommendation
              │
              ├── AI Analysis (CNN/MobileNet)
              ├── Confidence Score
              └── Suggested Products (JSON array)
```

## Key Relationships Summary

| Parent Entity | Relationship | Child Entity | Type | Notes |
|---------------|-------------|--------------|------|-------|
| Role | OneToMany | User | ManyToOne | User roles (CLIENT, SUPPORT, ADMIN, SUPER_ADMIN) |
| User | OneToOne | Cart | OneToOne | Each user has one cart |
| User | OneToMany | Vehicle | ManyToOne | User's registered vehicles |
| User | OneToMany | Order | ManyToOne | User's purchase history |
| User | OneToMany | Reclamation | ManyToOne | User's support tickets |
| User | OneToMany | Conversation | ManyToOne | User's chat conversations |
| User | OneToMany | Recommendation | ManyToOne | User's AI recommendations |
| Cart | OneToMany | CartItem | ManyToOne | Items in cart |
| Product | OneToMany | CartItem | ManyToOne | Product in multiple carts |
| Product | OneToMany | OrderItem | ManyToOne | Product in multiple orders |
| Order | OneToMany | OrderItem | ManyToOne | Products in order |
| Order | OneToOne | Delivery | OneToOne | Order delivery tracking |
| Conversation | OneToMany | Message | ManyToOne | Messages in conversation |

## Cascade Operations

### Complete Cascade (ALL + orphanRemoval)
- **Cart** → CartItems: Deleting cart removes all items
- **Order** → OrderItems: Deleting order removes all items
- **User** → Cart: Deleting user removes cart
- **Conversation** → Messages: Deleting conversation removes all messages

### Partial Cascade (ALL without orphanRemoval)
- **User** → Orders: History preserved even if user deleted
- **User** → Reclamations: Tickets preserved for audit
- **Product** → CartItems/OrderItems: Product history maintained

## Index Strategy

### Primary Indexes (Foreign Keys)
- All foreign key columns are indexed automatically
- Examples: `user_id`, `product_id`, `order_id`, `cart_id`, `conversation_id`

### Composite Indexes
- **Product**: `(brand, model, year)` - for vehicle compatibility searches
- **Order**: `(user_id, status, created_at)` - for order history filtering

### Status Indexes
- **Order**: `status` - for admin dashboard filtering
- **Delivery**: `status` - for delivery status tracking
- **Reclamation**: `status` - for support ticket management

### Timestamp Indexes
- **Order**: `created_at` - for chronological ordering
- **Message**: `created_at` - for chat message ordering
- **Reclamation**: `created_at` - for ticket queue ordering
- **Conversation**: `updated_at` - for recent conversations

## Data Flow Examples

### 1. User Registration & Shopping
```
1. User registers → Creates User + Role
2. System auto-creates → Cart (OneToOne)
3. User adds vehicle → Vehicle (ManyToOne to User)
4. User browses products → Product query
5. User adds to cart → CartItem (links Cart + Product)
6. User checkout → Order + OrderItems (cart snapshot)
7. Payment processed → Order.paymentStatus = COMPLETED
8. Delivery initiated → Delivery (ONdelivery API)
```

### 2. Support Ticket Flow
```
1. User has issue → Creates Reclamation
2. User uploads photo → Reclamation.attachmentUrl
3. Support views ticket → Status: OPEN → IN_PROGRESS
4. Support responds → Reclamation.response
5. Issue resolved → Status: RESOLVED, resolvedAt timestamp
```

### 3. Real-time Chat Flow
```
1. User opens support chat → Create Conversation
2. User sends message → Create Message (senderType: USER)
3. WebSocket broadcasts → Real-time delivery
4. Support replies → Create Message (senderType: SUPPORT)
5. Messages marked as read → Message.isRead = true
```

### 4. AI Recommendation Flow
```
1. User uploads part image → Recommendation.imageUrl
2. User describes symptom → Recommendation.symptoms
3. AI analyzes (CNN) → Recommendation.aiResponse
4. System suggests products → Recommendation.suggestedProducts (JSON)
5. Confidence score stored → Recommendation.confidenceScore
```

### 5. Admin Report Generation
```
1. Admin requests report → Report.reportType = "SALES"
2. System aggregates data → Report.data (JSON)
3. Export CSV/PDF → Report.fileUrl
4. Audit trail → Report.generatedBy (admin UUID)
```

## JSON Field Usage

### Product.compatibility
```json
["Toyota Camry 2015-2020", "Honda Accord 2016-2021", "Nissan Altima 2014-2019"]
```

### Admin.permissions
```json
["MANAGE_PRODUCTS", "VIEW_REPORTS", "MANAGE_ORDERS", "VIEW_USERS"]
```

### Recommendation.suggestedProducts
```json
[
  "550e8400-e29b-41d4-a716-446655440001",
  "550e8400-e29b-41d4-a716-446655440002",
  "550e8400-e29b-41d4-a716-446655440003"
]
```

### Report.data
```json
{
  "totalSales": 125000.50,
  "orderCount": 342,
  "topProducts": [
    {"id": "...", "name": "Brake Pads", "sales": 15000},
    {"id": "...", "name": "Oil Filter", "sales": 12500}
  ],
  "period": "2025-01-01 to 2025-03-31"
}
```

## Security Considerations

### Password Hashing
- All password fields (User, Admin, SuperAdmin) store **hashed** passwords
- Implementation in `SecurityConfig` using BCrypt

### Role-Based Access
```
SUPER_ADMIN → Manage admins (CRUD)
ADMIN → Manage products, orders, view reports
SUPPORT → Handle reclamations, chat conversations
CLIENT → Shop, place orders, chat with support
```

### UUID Benefits
- **Security**: Non-sequential IDs prevent enumeration attacks
- **Distribution**: Works in distributed/microservice architectures
- **Privacy**: Harder to guess than auto-increment IDs

---

**Status**: ✅ All 16 entities fully implemented and compiled successfully
**Next Step**: Implement Repository layer with custom queries
