package com.example.Backend.service.impl;

import com.example.Backend.dto.PaymentDTO;
import com.example.Backend.dto.PaymentIntentRequest;
import com.example.Backend.dto.PaymentIntentResponse;
import com.example.Backend.entity.Order;
import com.example.Backend.entity.Payment;
import com.example.Backend.entity.User;
import com.example.Backend.repository.OrderRepository;
import com.example.Backend.repository.PaymentRepository;
import com.example.Backend.repository.UserRepository;
import com.example.Backend.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request, UUID userId) {
        try {
            // Validate order exists and belongs to user
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getUser().getId().equals(userId)) {
                throw new RuntimeException("Unauthorized access to order");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if payment already exists for this order
            if (paymentRepository.existsByStripePaymentIntentId(order.getId().toString())) {
                throw new RuntimeException("Payment already exists for this order");
            }

            // Convert amount to cents (Stripe requires amount in smallest currency unit)
            Long amountInCents = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            // Create payment intent with Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setDescription(request.getDescription() != null ? request.getDescription()
                            : "Payment for Order #" + order.getId())
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("userId", userId.toString())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment record in database
            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setUser(user);
            payment.setStripePaymentIntentId(paymentIntent.getId());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(Payment.PaymentStatus.PENDING);

            paymentRepository.save(payment);

            log.info("Created payment intent: {} for order: {}", paymentIntent.getId(), order.getId());

            return PaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(amountInCents)
                    .currency(request.getCurrency())
                    .status(paymentIntent.getStatus())
                    .message("Payment intent created successfully")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error creating payment intent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentDTO confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Update payment status based on Stripe payment intent status
            switch (paymentIntent.getStatus()) {
                case "succeeded":
                    payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
                    // Update order status to CONFIRMED
                    Order order = payment.getOrder();
                    order.setStatus("CONFIRMED");
                    order.setPaymentStatus("COMPLETED");
                    orderRepository.save(order);
                    break;
                case "processing":
                    payment.setStatus(Payment.PaymentStatus.PROCESSING);
                    break;
                case "requires_action":
                    payment.setStatus(Payment.PaymentStatus.REQUIRES_ACTION);
                    break;
                case "canceled":
                    payment.setStatus(Payment.PaymentStatus.CANCELED);
                    break;
                default:
                    payment.setStatus(Payment.PaymentStatus.FAILED);
            }

            // Extract payment method details if available
            if (paymentIntent.getLatestCharge() != null) {
                payment.setReceiptUrl(paymentIntent.getLatestCharge());
            }

            paymentRepository.save(payment);

            log.info("Confirmed payment: {} with status: {}", paymentIntentId, payment.getStatus());

            return convertToDTO(payment);

        } catch (StripeException e) {
            log.error("Stripe error confirming payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to confirm payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentDTO cancelPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            paymentIntent = paymentIntent.cancel();

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setStatus(Payment.PaymentStatus.CANCELED);
            paymentRepository.save(payment);

            log.info("Canceled payment: {}", paymentIntentId);

            return convertToDTO(payment);

        } catch (StripeException e) {
            log.error("Stripe error canceling payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to cancel payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentDTO refundPayment(UUID paymentId, BigDecimal amount, String reason) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
                throw new RuntimeException("Can only refund succeeded payments");
            }

            // Convert amount to cents
            Long amountInCents = amount != null ? amount.multiply(BigDecimal.valueOf(100)).longValue() : null;

            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(payment.getStripePaymentIntentId());

            if (amountInCents != null) {
                paramsBuilder.setAmount(amountInCents);
            }

            if (reason != null) {
                paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
            }

            Refund.create(paramsBuilder.build());

            // Update payment record
            if (amount != null && amount.compareTo(payment.getAmount()) < 0) {
                payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
                payment.setRefundAmount(amount);
            } else {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
                payment.setRefundAmount(payment.getAmount());
            }

            payment.setRefundReason(reason);
            payment.setRefundedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Refunded payment: {} with amount: {}", paymentId, amount);

            return convertToDTO(payment);

        } catch (StripeException e) {
            log.error("Stripe error refunding payment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to refund payment: " + e.getMessage());
        }
    }

    @Override
    public PaymentDTO getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    public PaymentDTO getPaymentByStripePaymentIntentId(String stripePaymentIntentId) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    public List<PaymentDTO> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            log.info("Received Stripe webhook event: {}", event.getType());

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", event.getType());
            }

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Webhook processing failed: " + e.getMessage());
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .orElse(null);

        if (payment != null) {
            payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
            paymentRepository.save(payment);

            // Update order status
            Order order = payment.getOrder();
            order.setStatus("CONFIRMED");
            order.setPaymentStatus("COMPLETED");
            orderRepository.save(order);

            log.info("Payment succeeded via webhook: {}", paymentIntent.getId());
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new RuntimeException("Failed to deserialize payment intent"));

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                .orElse(null);

        if (payment != null) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureMessage(
                    paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage()
                            : "Payment failed");
            paymentRepository.save(payment);

            log.info("Payment failed via webhook: {}", paymentIntent.getId());
        }
    }

    private void handleChargeRefunded(Event event) {
        log.info("Charge refunded event received");
        // Additional refund handling if needed
    }

    private PaymentDTO convertToDTO(Payment payment) {
        return PaymentDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .userId(payment.getUser().getId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .paymentMethod(payment.getPaymentMethod())
                .cardLast4(payment.getCardLast4())
                .cardBrand(payment.getCardBrand())
                .failureMessage(payment.getFailureMessage())
                .receiptUrl(payment.getReceiptUrl())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .refundedAt(payment.getRefundedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
