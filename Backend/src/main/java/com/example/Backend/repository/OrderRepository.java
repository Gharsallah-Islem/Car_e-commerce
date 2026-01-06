package com.example.Backend.repository;

import com.example.Backend.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

        /**
         * Find all orders for a user
         */
        List<Order> findByUserId(UUID userId);

        /**
         * Find orders by user with pagination
         */
        Page<Order> findByUserId(UUID userId, Pageable pageable);

        /**
         * Find orders by status
         */
        List<Order> findByStatus(String status);

        /**
         * Find orders by user and status
         */
        List<Order> findByUserIdAndStatus(UUID userId, String status);

        /**
         * Find orders by payment status
         */
        List<Order> findByPaymentStatus(String paymentStatus);

        /**
         * Find orders by tracking number
         */
        @Query("SELECT o FROM Order o WHERE o.trackingNumber = :trackingNumber")
        List<Order> findByTrackingNumber(@Param("trackingNumber") String trackingNumber);

        /**
         * Find orders created between dates
         */
        List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Find orders by status with pagination
         */
        Page<Order> findByStatus(String status, Pageable pageable);

        /**
         * Get order with items, products, and user (fetch join for email)
         */
        @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.user LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.id = :orderId")
        Order findByIdWithItems(@Param("orderId") UUID orderId);

        /**
         * Find pending orders
         */
        @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' ORDER BY o.createdAt DESC")
        List<Order> findPendingOrders();

        /**
         * Find recent orders (last N days)
         */
        @Query("SELECT o FROM Order o WHERE o.createdAt >= :since ORDER BY o.createdAt DESC")
        List<Order> findRecentOrders(@Param("since") LocalDateTime since);

        /**
         * Count orders by status
         */
        Long countByStatus(String status);

        /**
         * Count orders by user
         */
        Long countByUserId(UUID userId);

        /**
         * Calculate total revenue
         */
        @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.paymentStatus = 'COMPLETED'")
        BigDecimal calculateTotalRevenue();

        /**
         * Calculate revenue between dates
         */
        @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.paymentStatus = 'COMPLETED' " +
                        "AND o.createdAt BETWEEN :startDate AND :endDate")
        BigDecimal calculateRevenueBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get orders for admin dashboard (with filters)
         */
        @Query("SELECT o FROM Order o WHERE " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(:startDate IS NULL OR o.createdAt >= :startDate) AND " +
                        "(:endDate IS NULL OR o.createdAt <= :endDate) " +
                        "ORDER BY o.createdAt DESC")
        Page<Order> findOrdersForDashboard(@Param("status") String status,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Find orders created after a specific date
         */
        List<Order> findByCreatedAtAfter(LocalDateTime date);

        /**
         * Find orders requiring attention (pending payment or confirmation)
         */
        @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED') " +
                        "AND o.paymentStatus = 'PENDING' ORDER BY o.createdAt ASC")
        List<Order> findOrdersRequiringAttention();

        /**
         * Find orders with items (fetch join) for analytics
         */
        @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.createdAt BETWEEN :startDate AND :endDate")
        List<Order> findByCreatedAtBetweenWithItems(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
