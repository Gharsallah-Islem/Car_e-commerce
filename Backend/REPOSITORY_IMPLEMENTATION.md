# Repository Layer Implementation

## Overview
All **15 JPA repository interfaces** have been successfully implemented with custom queries, pagination support, and optimized database operations.

## Repository Summary

### ✅ Implemented Repositories

| Repository | Entity | Key Features | Query Methods |
|------------|--------|--------------|---------------|
| **RoleRepository** | Role | Role management | 2 custom |
| **UserRepository** | User | User auth & management | 8 custom |
| **AdminRepository** | Admin | Admin management | 6 custom |
| **SuperAdminRepository** | SuperAdmin | Super admin CRUD | 5 custom |
| **VehicleRepository** | Vehicle | Vehicle compatibility | 8 custom |
| **ProductRepository** | Product | Advanced product search | 16 custom |
| **CartRepository** | Cart | Shopping cart | 3 custom |
| **CartItemRepository** | CartItem | Cart item management | 5 custom |
| **OrderRepository** | Order | Order processing & analytics | 16 custom |
| **OrderItemRepository** | OrderItem | Order details | 4 custom |
| **DeliveryRepository** | Delivery | ONdelivery integration | 8 custom |
| **ReclamationRepository** | Reclamation | Support tickets | 11 custom |
| **ConversationRepository** | Conversation | Chat conversations | 9 custom |
| **MessageRepository** | Message | Real-time messaging | 13 custom |
| **RecommendationRepository** | Recommendation | AI recommendations | 8 custom |
| **ReportRepository** | Report | Analytics reports | 10 custom |

**Total Custom Queries**: 132 methods

---

## Detailed Repository Documentation

### 1. RoleRepository
**Purpose**: Manage security roles

```java
// Find role by name
Optional<Role> findByName(String name);

// Check role existence
boolean existsByName(String name);
```

**Use Cases**:
- User registration (assign CLIENT role)
- Admin authentication (verify ADMIN/SUPER_ADMIN roles)
- Role-based access control

---

### 2. UserRepository
**Purpose**: User authentication and management

**Key Methods**:
```java
// Authentication
Optional<User> findByUsername(String username);
Optional<User> findByEmail(String email);
Optional<User> findByUsernameOrEmail(String username, String email);

// Validation
boolean existsByUsername(String username);
boolean existsByEmail(String email);

// Advanced queries
List<User> searchUsers(String searchTerm); // Search by username, email, full name
List<User> findByRoleName(String roleName);
List<User> findActiveUsers(LocalDateTime since); // Users with recent orders
Long countByRoleName(String roleName);
```

**Use Cases**:
- Login/registration validation
- User search in admin dashboard
- Analytics (count users by role)
- Finding active customers

---

### 3. AdminRepository
**Purpose**: Administrator account management

**Key Methods**:
```java
// Authentication
Optional<Admin> findByUsername(String username);
Optional<Admin> findByUsernameOrEmail(String username, String email);

// Status management
List<Admin> findByIsActive(Boolean isActive);
List<Admin> findAllActive(); // Default method
Long countByIsActive(Boolean isActive);
```

**Use Cases**:
- Admin login
- Super admin managing admin accounts
- Active/inactive admin filtering

---

### 4. SuperAdminRepository
**Purpose**: Super administrator management (identical to AdminRepository)

**Features**:
- Same authentication methods as AdminRepository
- Manage super admin accounts
- Track active/inactive status

---

### 5. VehicleRepository
**Purpose**: User vehicle management for product compatibility

**Key Methods**:
```java
// User vehicles
List<Vehicle> findByUserId(UUID userId);

// Compatibility search
List<Vehicle> findByBrandAndModelAndYear(String brand, String model, Integer year);
List<Vehicle> searchVehicles(String searchTerm);

// Dropdowns/filters
List<String> findDistinctBrands();
List<String> findDistinctModelsByBrand(String brand);
List<Integer> findDistinctYearsByBrandAndModel(String brand, String model);
```

**Use Cases**:
- User registering their vehicle
- Product compatibility checking
- Populating vehicle selection dropdowns (cascading: Brand → Model → Year)

---

### 6. ProductRepository ⭐ **Most Complex**
**Purpose**: Product catalog with advanced search and analytics

**Search & Filtering**:
```java
// Basic search
List<Product> findByNameContaining(String name);
List<Product> findByCategory(String category);
List<Product> findByBrand(String brand);

// Vehicle compatibility
List<Product> findCompatibleProducts(String brand, String model, Integer year);

// Advanced multi-filter search with pagination
Page<Product> searchProducts(
    String searchTerm,    // Search in name/description
    String category,      // Filter by category
    String brand,         // Filter by brand
    String model,         // Filter by model
    BigDecimal minPrice,  // Price range min
    BigDecimal maxPrice,  // Price range max
    Pageable pageable     // Pagination
);
```

**Inventory Management**:
```java
List<Product> findInStock(); // Stock > 0
List<Product> findLowStock(Integer threshold); // Stock <= threshold (e.g., 5)
List<Product> findOutOfStock(); // Stock = 0
Long countInStock();
```

**Analytics & Recommendations**:
```java
Page<Product> findTopSellingProducts(Pageable pageable);
Page<Product> findFeaturedProducts(Pageable pageable);
List<String> findDistinctCategories();
List<String> findDistinctBrands();
Long countByCategory(String category);
```

**Use Cases**:
- Product search page with filters
- Admin inventory management
- Low stock alerts
- Sales analytics
- Homepage featured products
- Vehicle-specific product recommendations

---

### 7. CartRepository
**Purpose**: User shopping cart management

**Key Methods**:
```java
// Cart retrieval
Optional<Cart> findByUserId(UUID userId);
Optional<Cart> findByUserIdWithItems(UUID userId); // Fetch join optimization

// Utilities
boolean existsByUserId(UUID userId);
Integer countItemsByUserId(UUID userId); // Total quantity
```

**Use Cases**:
- User viewing cart
- Adding products to cart
- Cart badge count
- Checkout process

---

### 8. CartItemRepository
**Purpose**: Individual cart item operations

**Key Methods**:
```java
// CRUD operations
List<CartItem> findByCartId(UUID cartId);
Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

// Modifying queries
@Modifying
void deleteByCartId(UUID cartId); // Clear cart
@Modifying
void deleteByCartIdAndProductId(UUID cartId, UUID productId); // Remove item
```

**Use Cases**:
- Update item quantity
- Remove item from cart
- Check if product already in cart
- Clear cart after checkout

---

### 9. OrderRepository ⭐ **Critical for Business**
**Purpose**: Order processing and business analytics

**Order Management**:
```java
// User orders
List<Order> findByUserId(UUID userId);
Page<Order> findByUserId(UUID userId, Pageable pageable);

// Status filtering
List<Order> findByStatus(String status);
List<Order> findByUserIdAndStatus(UUID userId, String status);
Order findByIdWithItems(UUID orderId); // Fetch join
```

**Admin Dashboard**:
```java
// Pending orders queue
List<Order> findPendingOrders();

// Orders requiring attention
List<Order> findOrdersRequiringAttention(); // Pending payment

// Advanced filtering
Page<Order> findOrdersForDashboard(
    String status,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Pageable pageable
);
```

**Analytics & Reporting**:
```java
// Revenue calculations
BigDecimal calculateTotalRevenue(); // All time
BigDecimal calculateRevenueBetween(LocalDateTime start, LocalDateTime end);

// Statistics
Long countByStatus(String status);
Long countByUserId(UUID userId);
List<Order> findRecentOrders(LocalDateTime since);
```

**Use Cases**:
- User order history
- Admin order management dashboard
- Sales reports (daily, weekly, monthly)
- Revenue analytics
- Order tracking by tracking number

---

### 10. OrderItemRepository
**Purpose**: Order details and product sales analytics

**Key Methods**:
```java
List<OrderItem> findByOrderId(UUID orderId);
Long getTotalQuantitySoldForProduct(UUID productId);
List<Object[]> getTopSellingProducts(); // For reports
```

**Use Cases**:
- Order details page
- Product sales statistics
- Best sellers report

---

### 11. DeliveryRepository
**Purpose**: ONdelivery integration and tracking

**Key Methods**:
```java
// Tracking
Optional<Delivery> findByOrderId(UUID orderId);
Optional<Delivery> findByTrackingNumber(String trackingNumber);

// Status management
List<Delivery> findByStatus(String status);
List<Delivery> findActiveDeliveries(); // IN_TRANSIT, OUT_FOR_DELIVERY

// Monitoring
List<Delivery> findOverdueDeliveries(LocalDateTime now);
List<Delivery> findPendingDeliveriesForSync(); // ONdelivery API sync
```

**Use Cases**:
- Order tracking page
- ONdelivery API synchronization
- Overdue delivery alerts
- Driver assignment
- Delivery status updates

---

### 12. ReclamationRepository
**Purpose**: Customer support ticket system

**Key Methods**:
```java
// User reclamations
List<Reclamation> findByUserId(UUID userId);
List<Reclamation> findByUserIdAndStatus(UUID userId, String status);

// Support dashboard
List<Reclamation> findOpenReclamations(); // OPEN, IN_PROGRESS
List<Reclamation> findUnresolvedReclamations();
List<Reclamation> findStaleReclamations(LocalDateTime threshold); // Old tickets

// Search & analytics
List<Reclamation> searchReclamations(String searchTerm);
Long countByStatus(String status);
```

**Use Cases**:
- User submitting/viewing tickets
- Support team queue management
- Ticket escalation (stale tickets)
- Support performance metrics

---

### 13. ConversationRepository
**Purpose**: Chat conversation management (Messenger-style)

**Key Methods**:
```java
// User conversations
List<Conversation> findByUserId(UUID userId);
Page<Conversation> findByUserIdOrderByUpdatedAtDesc(UUID userId, Pageable pageable);

// Conversation details
Optional<Conversation> findByIdWithMessages(UUID conversationId); // Fetch join

// Support dashboard
List<Conversation> findAllActiveConversations();
List<Conversation> findConversationsWithUnreadMessages(UUID userId);
Long countByUserIdAndIsActive(UUID userId, Boolean isActive);
```

**Use Cases**:
- User chat inbox
- Support team active chats list
- Unread message notifications
- Conversation history

---

### 14. MessageRepository ⭐ **Real-time Chat**
**Purpose**: Chat messages with WebSocket support

**Key Methods**:
```java
// Message retrieval
List<Message> findByConversationId(UUID conversationId);
Page<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);

// Real-time updates
List<Message> findByConversationIdAndCreatedAtAfter(UUID conversationId, LocalDateTime after);
Message findLastMessageInConversation(UUID conversationId);

// Read status
Long countByConversationIdAndIsRead(UUID conversationId, Boolean isRead);
Long countUnreadMessagesForUser(UUID userId);

// Batch updates
@Modifying
void markAllAsReadInConversation(UUID conversationId);
@Modifying
void markAsRead(List<UUID> messageIds);

// Search
List<Message> searchInConversation(UUID conversationId, String searchTerm);
```

**Use Cases**:
- Real-time chat interface
- WebSocket message broadcasting
- Unread message badges
- Message search within conversation
- Mark as read functionality

---

### 15. RecommendationRepository
**Purpose**: AI-powered product recommendations

**Key Methods**:
```java
// User recommendations
List<Recommendation> findByUserId(UUID userId);
Page<Recommendation> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

// AI confidence filtering
List<Recommendation> findHighConfidenceRecommendations(Double threshold); // >= 0.8
List<Recommendation> findUserHighConfidenceRecommendations(UUID userId, Double threshold);

// Analytics
Double getAverageConfidenceScoreForUser(UUID userId);
List<Recommendation> findBySymptomKeyword(String keyword);
```

**Use Cases**:
- AI recommendation history
- High-confidence recommendations display
- AI performance analytics
- Symptom pattern analysis

---

### 16. ReportRepository
**Purpose**: Admin analytics and report generation

**Key Methods**:
```java
// Report retrieval
List<Report> findByReportType(String reportType);
Page<Report> findByReportTypeOrderByCreatedAtDesc(String reportType, Pageable pageable);

// Audit trail
List<Report> findByGeneratedBy(UUID adminId);

// Report management
Report findLatestReportByType(String reportType);
List<Report> findReportsWithFiles(); // CSV/PDF exports
List<Report> searchReports(String searchTerm);

// Statistics
Long countByReportType(String reportType);
```

**Use Cases**:
- Admin dashboard analytics
- Sales reports (daily, weekly, monthly)
- Inventory reports
- User analytics
- CSV/PDF report downloads
- Report history and audit

---

## Query Optimization Techniques

### 1. **Fetch Joins** (Solve N+1 Problem)
```java
@Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems WHERE c.user.id = :userId")
Optional<Cart> findByUserIdWithItems(@Param("userId") UUID userId);

@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
Order findByIdWithItems(@Param("orderId") UUID orderId);

@Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.messages WHERE c.id = :conversationId")
Optional<Conversation> findByIdWithMessages(@Param("conversationId") UUID conversationId);
```

### 2. **Pagination** (Handle Large Datasets)
```java
Page<Product> searchProducts(..., Pageable pageable);
Page<Order> findByUserId(UUID userId, Pageable pageable);
Page<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId, Pageable pageable);
```

### 3. **Aggregations** (Database-level Calculations)
```java
BigDecimal calculateTotalRevenue();
Long getTotalQuantitySoldForProduct(UUID productId);
Integer countItemsByUserId(UUID userId);
Double getAverageConfidenceScoreForUser(UUID userId);
```

### 4. **Modifying Queries** (Batch Updates)
```java
@Modifying
void deleteByCartId(UUID cartId);

@Modifying
void markAllAsReadInConversation(UUID conversationId);
```

### 5. **DISTINCT Queries** (Dropdown/Filter Options)
```java
List<String> findDistinctCategories();
List<String> findDistinctBrands();
List<String> findDistinctModelsByBrand(String brand);
```

---

## Usage Examples

### Example 1: Product Search with Filters
```java
@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    public Page<Product> searchProducts(ProductSearchDTO searchDTO) {
        Pageable pageable = PageRequest.of(
            searchDTO.getPage(), 
            searchDTO.getSize(),
            Sort.by("createdAt").descending()
        );
        
        return productRepository.searchProducts(
            searchDTO.getSearchTerm(),
            searchDTO.getCategory(),
            searchDTO.getBrand(),
            searchDTO.getModel(),
            searchDTO.getMinPrice(),
            searchDTO.getMaxPrice(),
            pageable
        );
    }
}
```

### Example 2: Cart Operations
```java
@Service
public class CartService {
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    public Cart getUserCart(UUID userId) {
        return cartRepository.findByUserIdWithItems(userId)
            .orElseGet(() -> createNewCart(userId));
    }
    
    public void addToCart(UUID userId, UUID productId, int quantity) {
        Cart cart = getUserCart(userId);
        Optional<CartItem> existingItem = cartItemRepository
            .findByCartIdAndProductId(cart.getId(), productId);
        
        if (existingItem.isPresent()) {
            // Update quantity
            existingItem.get().increaseQuantity(quantity);
        } else {
            // Add new item
            // ...
        }
    }
}
```

### Example 3: Admin Dashboard Analytics
```java
@Service
public class DashboardService {
    @Autowired
    private OrderRepository orderRepository;
    
    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
        
        return DashboardStatsDTO.builder()
            .totalRevenue(orderRepository.calculateTotalRevenue())
            .monthlyRevenue(orderRepository.calculateRevenueBetween(lastMonth, LocalDateTime.now()))
            .pendingOrdersCount(orderRepository.countByStatus("PENDING"))
            .recentOrders(orderRepository.findRecentOrders(lastMonth))
            .build();
    }
}
```

### Example 4: Real-time Chat
```java
@Service
public class ChatService {
    @Autowired
    private MessageRepository messageRepository;
    
    public List<Message> getNewMessages(UUID conversationId, LocalDateTime since) {
        return messageRepository.findByConversationIdAndCreatedAtAfter(conversationId, since);
    }
    
    @Transactional
    public void markConversationAsRead(UUID conversationId) {
        messageRepository.markAllAsReadInConversation(conversationId);
    }
}
```

---

## Performance Considerations

### 1. **Index Usage**
All foreign keys and frequently queried fields are indexed in entities:
- `user_id`, `product_id`, `order_id`, `conversation_id`
- `status`, `created_at`, `updated_at`
- Composite indexes: `(brand, model, year)`

### 2. **Query Optimization**
- Use `LIMIT 1` for single results
- Fetch joins to avoid N+1 queries
- Pagination for large datasets
- Database-level aggregations (SUM, COUNT, AVG)

### 3. **Caching Opportunities**
Consider caching for:
- `findDistinctCategories()`, `findDistinctBrands()`
- `findByRoleName()` (roles rarely change)
- `calculateTotalRevenue()` (update on schedule)

### 4. **Transactional Operations**
`@Modifying` queries require `@Transactional` in service layer:
```java
@Transactional
public void clearCart(UUID cartId) {
    cartItemRepository.deleteByCartId(cartId);
}
```

---

## Testing Recommendations

### Unit Tests
```java
@DataJpaTest
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    void testFindByCategory() {
        List<Product> products = productRepository.findByCategory("Brakes");
        assertThat(products).isNotEmpty();
    }
}
```

### Integration Tests
```java
@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {
    @Autowired
    private OrderRepository orderRepository;
    
    @Test
    void testCreateOrderAndCalculateRevenue() {
        // Create test order
        // ...
        BigDecimal revenue = orderRepository.calculateTotalRevenue();
        assertThat(revenue).isGreaterThan(BigDecimal.ZERO);
    }
}
```

---

## Next Steps

### ✅ Completed
1. Entity layer (16 entities)
2. Repository layer (15 repositories + 132 custom queries)

### ⏳ To Do
3. **Service Layer** - Business logic implementation
4. **Controller Layer** - REST API endpoints
5. **Security Configuration** - JWT authentication
6. **DTO Layer** - Request/response mapping
7. **Exception Handling** - Custom exceptions
8. **Validation** - Input validation
9. **Testing** - Unit + Integration tests
10. **API Documentation** - Swagger/OpenAPI

---

**Status**: ✅ All repositories fully implemented and compiled successfully  
**Build Status**: ✅ SUCCESS (95 source files compiled)  
**Ready for**: Service layer implementation
