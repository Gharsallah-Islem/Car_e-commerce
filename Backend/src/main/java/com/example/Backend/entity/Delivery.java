package com.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "deliveries", indexes = {
        @Index(name = "idx_deliveries_order_id", columnList = "order_id"),
        @Index(name = "idx_deliveries_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Delivery implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @NotBlank(message = "Tracking number is required")
    @Column(name = "tracking_number", nullable = false, unique = true, length = 255)
    private String trackingNumber; // ONdelivery tracking number

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 50)
    private String status; // PROCESSING, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    @Column(name = "driver_name", length = 255)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Delivery status constants
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_IN_TRANSIT = "IN_TRANSIT";
    public static final String STATUS_OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
    public static final String STATUS_DELIVERED = "DELIVERED";
    public static final String STATUS_FAILED = "FAILED";

    // Helper methods
    public boolean isDelivered() {
        return STATUS_DELIVERED.equals(status);
    }

    public void markAsDelivered() {
        this.status = STATUS_DELIVERED;
        this.actualDelivery = LocalDateTime.now();
    }
}
