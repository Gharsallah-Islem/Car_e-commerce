package com.example.Backend.service;

import com.example.Backend.dto.OrderDTO;
import com.example.Backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface OrderService {

    /**
     * Create order from cart
     * 
     * @param userId   User ID
     * @param orderDTO Order details (shipping address, etc.)
     * @return Created order
     */
    Order createOrderFromCart(UUID userId, OrderDTO orderDTO);

    /**
     * Get order by ID
     * 
     * @param orderId Order ID
     * @return Order entity
     */
    Order getOrderById(UUID orderId);

    /**
     * Get all orders
     * 
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<Order> getAllOrders(Pageable pageable);

    /**
     * Get orders by user
     * 
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of user's orders
     */
    Page<Order> getOrdersByUser(UUID userId, Pageable pageable);

    /**
     * Get orders by status
     * 
     * @param status   Order status
     * @param pageable Pagination parameters
     * @return Page of orders
     */
    Page<Order> getOrdersByStatus(String status, Pageable pageable);

    /**
     * Get pending orders
     * 
     * @param pageable Pagination parameters
     * @return Page of pending orders
     */
    Page<Order> getPendingOrders(Pageable pageable);

    /**
     * Get orders for dashboard with filters
     * 
     * @param status    Status filter
     * @param startDate Start date filter
     * @param endDate   End date filter
     * @param pageable  Pagination parameters
     * @return Page of filtered orders
     */
    Page<Order> getOrdersForDashboard(String status, LocalDateTime startDate,
            LocalDateTime endDate, Pageable pageable);

    /**
     * Update order status
     * 
     * @param orderId Order ID
     * @param status  New status
     * @return Updated order
     */
    Order updateOrderStatus(UUID orderId, String status);

    /**
     * Confirm order
     * 
     * @param orderId Order ID
     * @return Updated order
     */
    Order confirmOrder(UUID orderId);

    /**
     * Ship order
     * 
     * @param orderId        Order ID
     * @param trackingNumber ONdelivery tracking number
     * @return Updated order
     */
    Order shipOrder(UUID orderId, String trackingNumber);

    /**
     * Mark order as delivered
     * 
     * @param orderId Order ID
     * @return Updated order
     */
    Order markAsDelivered(UUID orderId);

    /**
     * Cancel order
     * 
     * @param orderId Order ID
     * @param reason  Cancellation reason
     * @return Updated order
     */
    Order cancelOrder(UUID orderId, String reason);

    /**
     * Process payment for order
     * 
     * @param orderId         Order ID
     * @param paymentIntentId Stripe payment intent ID
     * @return Updated order
     */
    Order processPayment(UUID orderId, String paymentIntentId);

    /**
     * Calculate total revenue
     * 
     * @return Total revenue from all paid orders
     */
    BigDecimal calculateTotalRevenue();

    /**
     * Calculate revenue between dates
     * 
     * @param startDate Start date
     * @param endDate   End date
     * @return Revenue in date range
     */
    BigDecimal calculateRevenueBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get order statistics
     * 
     * @return Map of order counts by status
     */
    Map<String, Long> getOrderStatistics();

    /**
     * Count orders by user
     * 
     * @param userId User ID
     * @return Number of orders
     */
    Long countOrdersByUser(UUID userId);
}
