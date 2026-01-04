package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "password", "vehicles", "cart", "orders", "reclamations", "emailVerificationToken",
            "passwordResetToken" })
    private User user;

    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 50)
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // STRIPE, CASH_ON_DELIVERY

    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // PENDING, COMPLETED, FAILED

    @Column(name = "tracking_number", length = 255)
    private String trackingNumber; // ONdelivery tracking number

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore
    private Delivery delivery;

    // Order status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_DELIVERY_FAILED = "DELIVERY_FAILED";

    // Payment method constants
    public static final String PAYMENT_METHOD_STRIPE = "STRIPE";
    public static final String PAYMENT_METHOD_COD = "CASH_ON_DELIVERY";

    // Payment status constants
    public static final String PAYMENT_PENDING = "PENDING";
    public static final String PAYMENT_COMPLETED = "COMPLETED";
    public static final String PAYMENT_FAILED = "FAILED";

    // Helper methods
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }

    public boolean isShipped() {
        return STATUS_SHIPPED.equals(status);
    }

    public boolean isDelivered() {
        return STATUS_DELIVERED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public boolean isDeliveryFailed() {
        return STATUS_DELIVERY_FAILED.equals(status);
    }

    public boolean isCashOnDelivery() {
        return PAYMENT_METHOD_COD.equals(paymentMethod);
    }

    public boolean isStripePayment() {
        return PAYMENT_METHOD_STRIPE.equals(paymentMethod);
    }

    public boolean isPaymentCompleted() {
        return PAYMENT_COMPLETED.equals(paymentStatus);
    }

    public void markAsDelivered() {
        this.status = STATUS_DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    public void markAsDeliveryFailed() {
        this.status = STATUS_DELIVERY_FAILED;
    }
}
