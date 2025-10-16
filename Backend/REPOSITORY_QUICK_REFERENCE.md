# Repository Layer - Quick Reference Guide

## 🎯 Repository Selection Guide

### When to use which repository?

#### User Management
- **Authentication**: `UserRepository`, `AdminRepository`, `SuperAdminRepository`
- **User Search**: `UserRepository.searchUsers()`
- **Role Management**: `RoleRepository`

#### E-Commerce Operations
- **Product Catalog**: `ProductRepository.searchProducts()` 
- **Cart Operations**: `CartRepository`, `CartItemRepository`
- **Order Processing**: `OrderRepository`, `OrderItemRepository`
- **Delivery Tracking**: `DeliveryRepository`

#### Customer Support
- **Tickets**: `ReclamationRepository`
- **Chat**: `ConversationRepository`, `MessageRepository`

#### Analytics & Reporting
- **Sales Analytics**: `OrderRepository.calculateRevenue*()`
- **Inventory**: `ProductRepository.findLowStock()`, `countInStock()`
- **Reports**: `ReportRepository`
- **AI Insights**: `RecommendationRepository`

---

## 🔍 Common Query Patterns

### Pattern 1: Find by User
```java
userRepository.findByUserId(userId)
orderRepository.findByUserId(userId)
cartRepository.findByUserId(userId)
vehicleRepository.findByUserId(userId)
reclamationRepository.findByUserId(userId)
conversationRepository.findByUserId(userId)
recommendationRepository.findByUserId(userId)
```

### Pattern 2: Find by Status
```java
orderRepository.findByStatus("PENDING")
reclamationRepository.findByStatus("OPEN")
deliveryRepository.findByStatus("IN_TRANSIT")
```

### Pattern 3: Search with Pagination
```java
productRepository.searchProducts(..., pageable)
orderRepository.findByUserId(userId, pageable)
messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
```

### Pattern 4: Count/Aggregate
```java
userRepository.countByRoleName("CLIENT")
orderRepository.countByStatus("DELIVERED")
orderRepository.calculateTotalRevenue()
productRepository.countInStock()
```

### Pattern 5: Distinct Values (Dropdowns)
```java
productRepository.findDistinctCategories()
productRepository.findDistinctBrands()
vehicleRepository.findDistinctBrands()
```

---

## 📊 Repository Method Count

| Repository | Basic Methods | Custom Queries | Total |
|------------|---------------|----------------|-------|
| RoleRepository | 7 (JPA) | 2 | 9 |
| UserRepository | 7 | 8 | 15 |
| AdminRepository | 7 | 6 | 13 |
| SuperAdminRepository | 7 | 5 | 12 |
| VehicleRepository | 7 | 8 | 15 |
| ProductRepository | 7 | 16 | 23 |
| CartRepository | 7 | 3 | 10 |
| CartItemRepository | 7 | 5 | 12 |
| OrderRepository | 7 | 16 | 23 |
| OrderItemRepository | 7 | 4 | 11 |
| DeliveryRepository | 7 | 8 | 15 |
| ReclamationRepository | 7 | 11 | 18 |
| ConversationRepository | 7 | 9 | 16 |
| MessageRepository | 7 | 13 | 20 |
| RecommendationRepository | 7 | 8 | 15 |
| ReportRepository | 7 | 10 | 17 |
| **TOTAL** | **112** | **132** | **244** |

---

## 🚀 Performance Tips

### 1. Use Fetch Joins for Relationships
```java
// ❌ Bad (N+1 query problem)
Cart cart = cartRepository.findByUserId(userId).get();
List<CartItem> items = cart.getCartItems(); // Triggers N queries

// ✅ Good (Single query)
Cart cart = cartRepository.findByUserIdWithItems(userId).get();
List<CartItem> items = cart.getCartItems(); // Already loaded
```

### 2. Paginate Large Results
```java
// ❌ Bad (loads all data)
List<Order> orders = orderRepository.findByUserId(userId);

// ✅ Good (loads only requested page)
Page<Order> orders = orderRepository.findByUserId(userId, 
    PageRequest.of(0, 20, Sort.by("createdAt").descending()));
```

### 3. Use Database Aggregations
```java
// ❌ Bad (load all + calculate in Java)
List<Order> orders = orderRepository.findAll();
BigDecimal total = orders.stream()
    .map(Order::getTotalPrice)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// ✅ Good (calculate in database)
BigDecimal total = orderRepository.calculateTotalRevenue();
```

### 4. Batch Operations
```java
// ❌ Bad (multiple queries)
for (UUID id : messageIds) {
    Message msg = messageRepository.findById(id).get();
    msg.setIsRead(true);
    messageRepository.save(msg);
}

// ✅ Good (single query)
messageRepository.markAsRead(messageIds);
```

---

## 🎨 Implementation Examples

### Example 1: User Registration
```java
// 1. Check if username/email exists
if (userRepository.existsByUsername(username)) {
    throw new UserAlreadyExistsException("Username taken");
}

// 2. Get CLIENT role
Role role = roleRepository.findByName("CLIENT")
    .orElseThrow(() -> new RoleNotFoundException("CLIENT"));

// 3. Create user
User user = new User();
user.setUsername(username);
user.setEmail(email);
user.setRole(role);
userRepository.save(user);

// 4. Create cart for user
Cart cart = new Cart();
cart.setUser(user);
cartRepository.save(cart);
```

### Example 2: Add to Cart
```java
// 1. Get user's cart
Cart cart = cartRepository.findByUserId(userId)
    .orElseThrow(() -> new CartNotFoundException());

// 2. Check if product already in cart
Optional<CartItem> existing = cartItemRepository
    .findByCartIdAndProductId(cart.getId(), productId);

if (existing.isPresent()) {
    // Update quantity
    existing.get().increaseQuantity(quantity);
    cartItemRepository.save(existing.get());
} else {
    // Add new item
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException());
    
    CartItem item = new CartItem();
    item.setCart(cart);
    item.setProduct(product);
    item.setQuantity(quantity);
    cartItemRepository.save(item);
}
```

### Example 3: Checkout Process
```java
// 1. Get cart with items
Cart cart = cartRepository.findByUserIdWithItems(userId)
    .orElseThrow(() -> new CartNotFoundException());

// 2. Create order
Order order = new Order();
order.setUser(cart.getUser());
order.setTotalPrice(cart.getTotalPrice());
order.setStatus("PENDING");
order.setPaymentStatus("PENDING");
orderRepository.save(order);

// 3. Create order items from cart items
for (CartItem cartItem : cart.getCartItems()) {
    OrderItem orderItem = new OrderItem();
    orderItem.setOrder(order);
    orderItem.setProduct(cartItem.getProduct());
    orderItem.setQuantity(cartItem.getQuantity());
    orderItem.setPrice(cartItem.getProduct().getPrice());
    orderItemRepository.save(orderItem);
    
    // Update product stock
    cartItem.getProduct().decreaseStock(cartItem.getQuantity());
}

// 4. Clear cart
cartItemRepository.deleteByCartId(cart.getId());

// 5. Create delivery
Delivery delivery = new Delivery();
delivery.setOrder(order);
delivery.setStatus("PROCESSING");
delivery.setTrackingNumber(generateTrackingNumber());
deliveryRepository.save(delivery);
```

### Example 4: Admin Dashboard
```java
public DashboardDTO getAdminDashboard() {
    LocalDateTime lastMonth = LocalDateTime.now().minusDays(30);
    
    return DashboardDTO.builder()
        // User stats
        .totalUsers(userRepository.count())
        .activeUsers(userRepository.findActiveUsers(lastMonth).size())
        .clientCount(userRepository.countByRoleName("CLIENT"))
        
        // Product stats
        .totalProducts(productRepository.count())
        .inStockProducts(productRepository.countInStock())
        .lowStockProducts(productRepository.findLowStock(5).size())
        
        // Order stats
        .totalOrders(orderRepository.count())
        .pendingOrders(orderRepository.countByStatus("PENDING"))
        .recentOrders(orderRepository.findRecentOrders(lastMonth))
        
        // Revenue
        .totalRevenue(orderRepository.calculateTotalRevenue())
        .monthlyRevenue(orderRepository.calculateRevenueBetween(lastMonth, LocalDateTime.now()))
        
        // Support stats
        .openReclamations(reclamationRepository.countByStatus("OPEN"))
        .activeConversations(conversationRepository.countByIsActive(true))
        
        .build();
}
```

### Example 5: Real-time Chat
```java
// WebSocket: Send new message
public Message sendMessage(UUID conversationId, MessageDTO dto) {
    Conversation conv = conversationRepository.findById(conversationId)
        .orElseThrow(() -> new ConversationNotFoundException());
    
    Message message = new Message();
    message.setConversation(conv);
    message.setSenderId(dto.getSenderId());
    message.setSenderType(dto.getSenderType());
    message.setContent(dto.getContent());
    messageRepository.save(message);
    
    // Broadcast via WebSocket
    webSocketService.broadcast(conversationId, message);
    
    return message;
}

// WebSocket: Poll for new messages (fallback)
public List<Message> getNewMessages(UUID conversationId, LocalDateTime since) {
    return messageRepository.findByConversationIdAndCreatedAtAfter(conversationId, since);
}

// Mark conversation as read
@Transactional
public void markAsRead(UUID conversationId) {
    messageRepository.markAllAsReadInConversation(conversationId);
}
```

### Example 6: Product Search
```java
public Page<Product> searchProducts(ProductSearchDTO dto) {
    Pageable pageable = PageRequest.of(
        dto.getPage(), 
        dto.getSize(),
        Sort.by(dto.getSortBy()).descending()
    );
    
    return productRepository.searchProducts(
        dto.getSearchTerm(),
        dto.getCategory(),
        dto.getBrand(),
        dto.getModel(),
        dto.getMinPrice(),
        dto.getMaxPrice(),
        pageable
    );
}

// Get filter options
public ProductFiltersDTO getFilterOptions() {
    return ProductFiltersDTO.builder()
        .categories(productRepository.findDistinctCategories())
        .brands(productRepository.findDistinctBrands())
        .build();
}

// Get models for selected brand (AJAX)
public List<String> getModelsForBrand(String brand) {
    return productRepository.findDistinctModelsByBrand(brand);
}
```

---

## 🔐 Security Considerations

### Always validate user access
```java
// ❌ Bad: Direct access without validation
Order order = orderRepository.findById(orderId).get();

// ✅ Good: Verify ownership
Order order = orderRepository.findById(orderId)
    .orElseThrow(() -> new OrderNotFoundException());
if (!order.getUser().getId().equals(currentUserId)) {
    throw new UnauthorizedException("Not your order");
}
```

### Use role-based queries
```java
// For regular users: only their data
List<Order> orders = orderRepository.findByUserId(currentUserId);

// For admins: all data with filters
Page<Order> orders = orderRepository.findOrdersForDashboard(...);
```

---

## ⚠️ Common Pitfalls

### 1. N+1 Query Problem
```java
// ❌ This triggers N+1 queries
List<Order> orders = orderRepository.findAll();
for (Order order : orders) {
    System.out.println(order.getOrderItems().size()); // N queries
}

// ✅ Use fetch join
@Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems")
List<Order> findAllWithItems();
```

### 2. Missing @Transactional for @Modifying
```java
// ❌ Will throw exception
public void clearCart(UUID cartId) {
    cartItemRepository.deleteByCartId(cartId); // Needs transaction
}

// ✅ Add @Transactional
@Transactional
public void clearCart(UUID cartId) {
    cartItemRepository.deleteByCartId(cartId);
}
```

### 3. Loading Too Much Data
```java
// ❌ Loading all messages (could be thousands)
List<Message> messages = messageRepository.findByConversationId(conversationId);

// ✅ Use pagination
Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(
    conversationId, 
    PageRequest.of(0, 50)
);
```

---

## 📝 Testing Checklist

### Repository Tests
- [ ] Test custom query methods
- [ ] Test pagination
- [ ] Test sorting
- [ ] Test null parameter handling
- [ ] Test edge cases (empty results, etc.)

### Integration Tests
- [ ] Test cascade operations
- [ ] Test transactional behavior
- [ ] Test fetch joins
- [ ] Test concurrent updates
- [ ] Test constraint violations

---

**Quick Start**: Start with `UserRepository`, `ProductRepository`, `CartRepository`, and `OrderRepository` - these are the core of your e-commerce platform!
