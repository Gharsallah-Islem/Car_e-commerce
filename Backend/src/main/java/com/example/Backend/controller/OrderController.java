package com.example.Backend.controller;

import com.example.Backend.dto.OrderDTO;
import com.example.Backend.entity.Order;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Order management
 * Handles order creation, tracking, and management
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ========== CUSTOMER ENDPOINTS ==========

    /**
     * Create order from cart (Checkout)
     * POST /api/orders
     * Security: CLIENT role required
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody OrderDTO orderDTO) {
        Order order = orderService.createOrderFromCart(currentUser.getId(), orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Get current user's orders
     * GET /api/orders/my-orders?page=0&size=10
     * GET /api/orders/user/me?page=0&size=10 (alias)
     * Security: CLIENT role required
     */
    @GetMapping({ "/my-orders", "/user/me" })
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<Order>> getMyOrders(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.getOrdersByUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get order by ID
     * GET /api/orders/{id}
     * Security: CLIENT role (own orders) or ADMIN/SUPER_ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Order> getOrderById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id) {
        Order order = orderService.getOrderById(id);

        // Clients can only view their own orders
        if (currentUser.getRoleName().equals("CLIENT") &&
                !order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(order);
    }

    /**
     * Cancel order
     * POST /api/orders/{id}/cancel
     * Security: CLIENT role (own orders only)
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Order> cancelOrder(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID id,
            @RequestBody(required = false) CancelOrderRequest request) {

        // Verify order belongs to user
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String reason = request != null && request.getReason() != null
                ? request.getReason()
                : "Customer requested cancellation";

        Order cancelledOrder = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(cancelledOrder);
    }

    /**
     * Get order count for current user
     * GET /api/orders/my-orders/count
     * Security: CLIENT role required
     */
    @GetMapping("/my-orders/count")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Map<String, Long>> getMyOrdersCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        Long count = orderService.countOrdersByUser(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ========== ADMIN ENDPOINTS ==========

    /**
     * Get all orders (Admin dashboard)
     * GET /api/orders?page=0&size=20
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders with filters (Admin dashboard)
     * GET /api/orders/dashboard?status=PENDING&startDate=...&endDate=...
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Order>> getOrdersForDashboard(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.getOrdersForDashboard(status, startDate, endDate, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get orders by status
     * GET /api/orders/status/{status}
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Order>> getOrdersByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get pending orders
     * GET /api/orders/pending
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<Order>> getPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Order> orders = orderService.getPendingOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Update order status
     * PATCH /api/orders/{id}/status
     * Body: { "status": "CONFIRMED" }
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody UpdateStatusRequest request) {
        Order order = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(order);
    }

    /**
     * Confirm order
     * POST /api/orders/{id}/confirm
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Order> confirmOrder(@PathVariable UUID id) {
        Order order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Ship order
     * POST /api/orders/{id}/ship
     * Body: { "trackingNumber": "TRK123456789" }
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Order> shipOrder(
            @PathVariable UUID id,
            @RequestBody ShipOrderRequest request) {
        Order order = orderService.shipOrder(id, request.getTrackingNumber());
        return ResponseEntity.ok(order);
    }

    /**
     * Mark order as delivered
     * POST /api/orders/{id}/deliver
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Order> markAsDelivered(@PathVariable UUID id) {
        Order order = orderService.markAsDelivered(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Process payment
     * POST /api/orders/{id}/payment
     * Body: { "paymentIntentId": "pi_xxxx" }
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @PostMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Order> processPayment(
            @PathVariable UUID id,
            @RequestBody PaymentRequest request) {
        Order order = orderService.processPayment(id, request.getPaymentIntentId());
        return ResponseEntity.ok(order);
    }

    /**
     * Get order statistics
     * GET /api/orders/statistics
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Order counts by status
        Map<String, Long> orderStats = orderService.getOrderStatistics();
        stats.put("ordersByStatus", orderStats);

        // Revenue
        BigDecimal totalRevenue = orderService.calculateTotalRevenue();
        stats.put("totalRevenue", totalRevenue);

        // Revenue last 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        BigDecimal revenueLastMonth = orderService.calculateRevenueBetween(
                thirtyDaysAgo, LocalDateTime.now());
        stats.put("revenueLastMonth", revenueLastMonth);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get revenue report
     * GET /api/orders/revenue?startDate=...&endDate=...
     * Security: ADMIN or SUPER_ADMIN role required
     */
    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        BigDecimal revenue;
        if (startDate != null && endDate != null) {
            revenue = orderService.calculateRevenueBetween(startDate, endDate);
        } else {
            revenue = orderService.calculateTotalRevenue();
        }

        return ResponseEntity.ok(Map.of("revenue", revenue));
    }

    // ========== Request DTOs ==========

    /**
     * Request body for cancelling order
     */
    @lombok.Data
    public static class CancelOrderRequest {
        private String reason;
    }

    /**
     * Request body for updating order status
     */
    @lombok.Data
    public static class UpdateStatusRequest {
        private String status;
    }

    /**
     * Request body for shipping order
     */
    @lombok.Data
    public static class ShipOrderRequest {
        private String trackingNumber;
    }

    /**
     * Request body for payment processing
     */
    @lombok.Data
    public static class PaymentRequest {
        private String paymentIntentId;
    }
}
