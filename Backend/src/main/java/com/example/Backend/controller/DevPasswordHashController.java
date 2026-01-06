package com.example.Backend.controller;

import com.example.Backend.entity.Order;
import com.example.Backend.repository.OrderRepository;
import com.example.Backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * TEMPORARY CONTROLLER - DELETE AFTER USE
 * This controller is for debugging mail and password issues
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Slf4j
public class DevPasswordHashController {

    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OrderRepository orderRepository;

    @GetMapping("/hash/{password}")
    public ResponseEntity<Map<String, String>> generateHash(@PathVariable String password) {
        String hash = passwordEncoder.encode(password);

        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("sql", "UPDATE users SET password = '" + hash + "' WHERE username = 'admin';");

        return ResponseEntity.ok(response);
    }

    /**
     * Test email sending
     * Usage: GET /api/dev/test-email?to=your@email.com
     */
    @GetMapping("/test-email")
    public ResponseEntity<Map<String, String>> testEmail(@RequestParam String to) {
        Map<String, String> response = new HashMap<>();
        try {
            log.info("Testing email send to: {}", to);
            emailService.sendPasswordResetEmail(to, "TestUser", "123456");
            response.put("status", "SUCCESS");
            response.put("message", "Test email sent to: " + to);
            log.info("Test email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Test email failed: {}", e.getMessage(), e);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
            response.put("cause", e.getCause() != null ? e.getCause().getMessage() : "N/A");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Resend order confirmation email - for debugging
     * Usage: GET /api/dev/resend-order-email/{orderId}
     */
    @GetMapping("/resend-order-email/{orderId}")
    public ResponseEntity<Map<String, String>> resendOrderEmail(@PathVariable String orderId) {
        Map<String, String> response = new HashMap<>();
        try {
            log.info("Attempting to resend order confirmation for order: {}", orderId);

            // Get the order from repository
            Optional<Order> orderOpt = orderRepository.findById(UUID.fromString(orderId));
            if (orderOpt.isEmpty()) {
                response.put("status", "FAILED");
                response.put("error", "Order not found: " + orderId);
                return ResponseEntity.ok(response);
            }

            Order order = orderOpt.get();
            log.info("Order found: {}", order.getId());
            log.info("Order user: {}", order.getUser() != null ? order.getUser().getUsername() : "NULL");
            log.info("Order user email: {}", order.getUser() != null ? order.getUser().getEmail() : "NULL");
            log.info("Order items count: {}", order.getOrderItems() != null ? order.getOrderItems().size() : "NULL");

            response.put("orderId", order.getId().toString());
            response.put("userName", order.getUser() != null ? order.getUser().getUsername() : "NULL");
            response.put("userEmail", order.getUser() != null ? order.getUser().getEmail() : "NULL");
            response.put("itemsCount",
                    order.getOrderItems() != null ? String.valueOf(order.getOrderItems().size()) : "NULL");

            // Try to send email
            emailService.sendOrderConfirmationEmail(order);
            response.put("status", "SUCCESS");
            response.put("message", "Order confirmation email resent for order: " + orderId);

        } catch (Exception e) {
            log.error("Resend order email failed: {}", e.getMessage(), e);
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Find orders by partial ID - for debugging
     * Usage: GET /api/dev/find-orders?partialId=38ef9dc3
     */
    @GetMapping("/find-orders")
    public ResponseEntity<Map<String, Object>> findOrders(@RequestParam String partialId) {
        Map<String, Object> response = new HashMap<>();
        try {
            var allOrders = orderRepository.findAll();
            var matchingOrders = allOrders.stream()
                    .filter(o -> o.getId().toString().startsWith(partialId) || o.getId().toString().contains(partialId))
                    .limit(5)
                    .toList();

            response.put("count", matchingOrders.size());
            response.put("orders", matchingOrders.stream().map(o -> {
                Map<String, String> orderInfo = new HashMap<>();
                orderInfo.put("fullId", o.getId().toString());
                orderInfo.put("status", o.getStatus());
                orderInfo.put("total", o.getTotalPrice() != null ? o.getTotalPrice().toString() : "N/A");
                orderInfo.put("userEmail", o.getUser() != null ? o.getUser().getEmail() : "NULL");
                return orderInfo;
            }).toList());

        } catch (Exception e) {
            response.put("status", "FAILED");
            response.put("error", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
