package com.example.Backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private UUID id;
    private UUID orderId;
    private UUID userId;
    private String stripePaymentIntentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String cardLast4;
    private String cardBrand;
    private String failureMessage;
    private String receiptUrl;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
