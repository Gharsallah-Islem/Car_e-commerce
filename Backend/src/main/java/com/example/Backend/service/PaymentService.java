package com.example.Backend.service;

import com.example.Backend.dto.PaymentDTO;
import com.example.Backend.dto.PaymentIntentRequest;
import com.example.Backend.dto.PaymentIntentResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    /**
     * Create a payment intent for an order
     */
    PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request, UUID userId);

    /**
     * Confirm a payment after customer completes payment
     */
    PaymentDTO confirmPayment(String paymentIntentId);

    /**
     * Cancel a payment intent
     */
    PaymentDTO cancelPayment(String paymentIntentId);

    /**
     * Refund a payment (full or partial)
     */
    PaymentDTO refundPayment(UUID paymentId, java.math.BigDecimal amount, String reason);

    /**
     * Get payment by ID
     */
    PaymentDTO getPaymentById(UUID paymentId);

    /**
     * Get payment by Stripe payment intent ID
     */
    PaymentDTO getPaymentByStripePaymentIntentId(String stripePaymentIntentId);

    /**
     * Get all payments for a user
     */
    List<PaymentDTO> getPaymentsByUserId(UUID userId);

    /**
     * Get all payments for an order
     */
    List<PaymentDTO> getPaymentsByOrderId(UUID orderId);

    /**
     * Handle Stripe webhook events
     */
    void handleWebhookEvent(String payload, String sigHeader);
}
