package com.example.Backend.service.impl;

import com.example.Backend.dto.OrderDTO;
import com.example.Backend.entity.*;
import com.example.Backend.repository.*;
import com.example.Backend.service.DeliveryService;
import com.example.Backend.service.OrderService;
import com.example.Backend.service.EmailService;
import com.example.Backend.entity.StockMovement;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;
    private final StockMovementRepository stockMovementRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            EmailService emailService,
            DeliveryRepository deliveryRepository,
            @Lazy DeliveryService deliveryService,
            StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
        this.deliveryRepository = deliveryRepository;
        this.deliveryService = deliveryService;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    public Order createOrderFromCart(UUID userId, OrderDTO orderDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Cart cart = user.getCart();
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate cart items stock
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();
            if (!product.isInStock()) {
                throw new IllegalStateException("Product " + product.getName() + " is out of stock");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
        }

        // Create order
        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setDeliveryAddress(orderDTO.getShippingAddress());
        order.setTotalPrice(cart.getTotalPrice());
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setNotes(orderDTO.getNotes());

        Order savedOrder = orderRepository.save(order);

        // Create order items and update product stock
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);

            // Decrease product stock and log movement
            Product product = cartItem.getProduct();
            int previousStock = product.getStock();
            product.decreaseStock(cartItem.getQuantity());
            productRepository.save(product);

            // Log stock movement for audit trail
            logStockMovement(product, StockMovement.MovementType.SALE,
                    -cartItem.getQuantity(), previousStock, product.getStock(),
                    savedOrder.getId(), "ORDER", "Vente - Commande créée");
        }

        // Clear cart
        cart.clearCart();
        cartRepository.save(cart);

        // Refresh order with order items and products loaded
        UUID orderId = savedOrder.getId();
        Order refreshedOrder = orderRepository.findByIdWithItems(orderId);
        if (refreshedOrder == null) {
            // Fallback: use findById and manually load items
            refreshedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Order not found after creation"));
            // Force load order items by accessing them
            if (refreshedOrder.getOrderItems() != null) {
                refreshedOrder.getOrderItems().size(); // This will trigger lazy loading
                // Also force load products for each item
                refreshedOrder.getOrderItems().forEach(item -> {
                    if (item.getProduct() != null) {
                        item.getProduct().getName(); // Trigger product loading
                    }
                });
            }
        }
        savedOrder = refreshedOrder;

        // Send order confirmation email
        try {
            log.info("Attempting to send order confirmation email for order: {}", savedOrder.getId());
            if (savedOrder.getOrderItems() == null || savedOrder.getOrderItems().isEmpty()) {
                log.warn("Order {} has no order items, skipping email", savedOrder.getId());
            } else {
                log.info("Order {} has {} items, sending email", savedOrder.getId(), savedOrder.getOrderItems().size());
                emailService.sendOrderConfirmationEmail(savedOrder);
                log.info("Order confirmation email sent successfully for order: {}", savedOrder.getId());
            }
        } catch (Exception e) {
            // Log but don't fail the order
            log.error("Failed to send order confirmation email for order {}: {}", savedOrder.getId(), e.getMessage(),
                    e);
        }

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByUser(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getPendingOrders(Pageable pageable) {
        return orderRepository.findByStatus("PENDING", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrdersForDashboard(String status, LocalDateTime startDate,
            LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findOrdersForDashboard(status, startDate, endDate, pageable);
    }

    @Override
    public Order updateOrderStatus(UUID orderId, String status) {
        Order order = getOrderById(orderId);

        // If changing to SHIPPED, use shipOrder to auto-create delivery
        if ("SHIPPED".equals(status) && !"SHIPPED".equals(order.getStatus())) {
            return shipOrder(orderId, null); // Auto-generate tracking number
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    public Order confirmOrder(UUID orderId) {
        Order order = getOrderById(orderId);
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        order.setStatus("CONFIRMED");
        Order savedOrder = orderRepository.save(order);

        // AUTO-CREATE DELIVERY when order is confirmed
        try {
            deliveryService.createDeliveryFromOrder(savedOrder);
            log.info("Auto-created delivery for confirmed order {}", orderId);
        } catch (Exception e) {
            log.error("Failed to auto-create delivery for order {}: {}", orderId, e.getMessage());
        }

        return savedOrder;
    }

    @Override
    public Order shipOrder(UUID orderId, String trackingNumber) {
        Order order = getOrderById(orderId);

        // Validation: Order must be CONFIRMED or PROCESSING before shipping
        // PROCESSING is accepted for backward compatibility with existing orders
        String currentStatus = order.getStatus();
        boolean canShip = Order.STATUS_CONFIRMED.equals(currentStatus)
                || "PROCESSING".equals(currentStatus)
                || "PAID".equals(currentStatus);

        if (!canShip) {
            throw new IllegalStateException(
                    "Order must be confirmed or processing before shipping. Current status: " + currentStatus);
        }

        // For Stripe orders: if payment is still PENDING but order was CONFIRMED by
        // admin,
        // we trust that admin verified the payment. Auto-mark as completed.
        if (order.isStripePayment() && !order.isPaymentCompleted()) {
            // Auto-update payment status since admin confirmed the order
            order.setPaymentStatus(Order.PAYMENT_COMPLETED);
            log.info("Auto-marked payment as COMPLETED for Stripe order {} (admin confirmed)", orderId);
        }

        // Generate tracking number if not provided
        String finalTrackingNumber = trackingNumber;
        if (finalTrackingNumber == null || finalTrackingNumber.isBlank()) {
            finalTrackingNumber = "TRK-" + System.currentTimeMillis() + "-"
                    + orderId.toString().substring(0, 8).toUpperCase();
        }

        order.setStatus(Order.STATUS_SHIPPED);
        order.setTrackingNumber(finalTrackingNumber);
        Order savedOrder = orderRepository.save(order);

        // Auto-create Delivery record if it doesn't exist
        if (deliveryRepository.findByOrderId(orderId).isEmpty()) {
            Delivery delivery = new Delivery();
            delivery.setOrder(savedOrder);
            delivery.setTrackingNumber(finalTrackingNumber);
            delivery.setStatus(Delivery.STATUS_PROCESSING);
            delivery.setAddress(savedOrder.getDeliveryAddress());
            delivery.setEstimatedDelivery(LocalDateTime.now().plusDays(3)); // Default 3 days
            deliveryRepository.save(delivery);
            log.info("Auto-created delivery for order {} with tracking number {}", orderId, finalTrackingNumber);
        }

        return savedOrder;
    }

    @Override
    public Order markAsDelivered(UUID orderId) {
        Order order = getOrderById(orderId);
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new IllegalStateException("Order must be shipped before marking as delivered");
        }
        order.markAsDelivered();
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(UUID orderId, String reason) {
        Order order = getOrderById(orderId);
        if ("DELIVERED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }

        // Restore product stock and log movement
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int previousStock = product.getStock();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);

            // Log stock movement for audit trail
            logStockMovement(product, StockMovement.MovementType.RETURN_FROM_CUSTOMER,
                    item.getQuantity(), previousStock, product.getStock(),
                    orderId, "ORDER_CANCEL", "Retour stock - Commande annulée: " + reason);
        }

        order.setStatus("CANCELLED");
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Cancelled: " + reason);
        return orderRepository.save(order);
    }

    @Override
    public Order processPayment(UUID orderId, String paymentIntentId) {
        Order order = getOrderById(orderId);
        order.setPaymentStatus("COMPLETED");
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Payment ID: " + paymentIntentId);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenueBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.calculateRevenueBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getOrderStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", orderRepository.count());
        stats.put("pending", orderRepository.countByStatus("PENDING"));
        stats.put("confirmed", orderRepository.countByStatus("CONFIRMED"));
        stats.put("shipped", orderRepository.countByStatus("SHIPPED"));
        stats.put("delivered", orderRepository.countByStatus("DELIVERED"));
        stats.put("cancelled", orderRepository.countByStatus("CANCELLED"));
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOrdersByUser(UUID userId) {
        return orderRepository.countByUserId(userId);
    }

    /**
     * Helper method to log stock movements for audit trail
     */
    private void logStockMovement(Product product, StockMovement.MovementType movementType,
            int quantity, int previousStock, int newStock, UUID referenceId,
            String referenceType, String notes) {
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setPreviousStock(previousStock);
        movement.setNewStock(newStock);
        movement.setReferenceId(referenceId);
        movement.setReferenceType(referenceType);
        movement.setNotes(notes);
        movement.setPerformedBy("SYSTEM");
        stockMovementRepository.save(movement);
        log.info("Stock movement logged: {} {} units for product {}",
                movementType, quantity, product.getName());
    }
}
