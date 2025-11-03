package com.example.Backend.controller;

import com.example.Backend.dto.PaymentDTO;
import com.example.Backend.dto.PaymentIntentRequest;
import com.example.Backend.dto.PaymentIntentResponse;
import com.example.Backend.security.UserPrincipal;
import com.example.Backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Create a payment intent for an order
     * POST /api/payments/create-intent
     */
    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Creating payment intent for order: {} by user: {}", request.getOrderId(), userPrincipal.getId());

        PaymentIntentResponse response = paymentService.createPaymentIntent(request, userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm a payment (called after Stripe confirms payment)
     * POST /api/payments/confirm/{paymentIntentId}
     */
    @PostMapping("/confirm/{paymentIntentId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentDTO> confirmPayment(@PathVariable String paymentIntentId) {
        log.info("Confirming payment: {}", paymentIntentId);

        PaymentDTO payment = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Cancel a payment intent
     * POST /api/payments/cancel/{paymentIntentId}
     */
    @PostMapping("/cancel/{paymentIntentId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaymentDTO> cancelPayment(@PathVariable String paymentIntentId) {
        log.info("Canceling payment: {}", paymentIntentId);

        PaymentDTO payment = paymentService.cancelPayment(paymentIntentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Refund a payment (full or partial)
     * POST /api/payments/refund/{paymentId}
     */
    @PostMapping("/refund/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentDTO> refundPayment(
            @PathVariable UUID paymentId,
            @RequestParam(required = false) BigDecimal amount,
            @RequestParam(required = false) String reason) {

        log.info("Refunding payment: {} with amount: {}", paymentId, amount);

        PaymentDTO payment = paymentService.refundPayment(paymentId, amount, reason);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get payment by ID
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PaymentDTO> getPayment(@PathVariable UUID paymentId) {
        log.info("Getting payment: {}", paymentId);

        PaymentDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get all payments for current user
     * GET /api/payments/user/me
     */
    @GetMapping("/user/me")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<PaymentDTO>> getMyPayments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Getting payments for user: {}", userPrincipal.getId());

        List<PaymentDTO> payments = paymentService.getPaymentsByUserId(userPrincipal.getId());
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all payments for an order
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByOrder(@PathVariable UUID orderId) {
        log.info("Getting payments for order: {}", orderId);

        List<PaymentDTO> payments = paymentService.getPaymentsByOrderId(orderId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Stripe webhook endpoint
     * POST /api/payments/webhook
     * This endpoint receives events from Stripe (payment succeeded, failed, etc.)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        log.info("Received Stripe webhook");

        try {
            paymentService.handleWebhookEvent(payload, sigHeader);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing failed");
        }
    }
}
