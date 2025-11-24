package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reorder Setting Entity - Automated reorder point configuration per product
 */
@Entity
@Table(name = "reorder_settings", uniqueConstraints = {
        @UniqueConstraint(columnNames = "product_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderSetting implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull(message = "Product is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "suppliers" })
    private Product product;

    @NotNull(message = "Reorder point is required")
    @Min(value = 0, message = "Reorder point cannot be negative")
    @Column(name = "reorder_point", nullable = false)
    private Integer reorderPoint; // When stock reaches this level, trigger reorder

    @NotNull(message = "Reorder quantity is required")
    @Min(value = 1, message = "Reorder quantity must be at least 1")
    @Column(name = "reorder_quantity", nullable = false)
    private Integer reorderQuantity; // How much to reorder

    @Min(value = 0, message = "Minimum stock cannot be negative")
    @Column(name = "minimum_stock")
    private Integer minimumStock; // Safety stock level

    @Min(value = 1, message = "Maximum stock must be at least 1")
    @Column(name = "maximum_stock")
    private Integer maximumStock; // Maximum stock to maintain

    @Min(value = 1, message = "Lead time days must be at least 1")
    @Column(name = "lead_time_days")
    private Integer leadTimeDays; // Days from order to delivery

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    @Column(name = "auto_reorder", nullable = false)
    private Boolean autoReorder = false; // Automatically create PO when reorder point reached

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_supplier_id")
    @JsonIgnoreProperties({ "purchaseOrders", "products", "hibernateLazyInitializer", "handler" })
    private Supplier preferredSupplier; // Default supplier for auto-reorder

    @Column(name = "last_reorder_date")
    private LocalDateTime lastReorderDate;

    @Column(name = "last_alert_sent")
    private LocalDateTime lastAlertSent;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper method to check if reorder is needed
    public boolean needsReorder(Integer currentStock) {
        return isEnabled && currentStock != null && currentStock <= reorderPoint;
    }

    // Helper method to check if stock is below minimum
    public boolean isBelowMinimum(Integer currentStock) {
        return minimumStock != null && currentStock != null && currentStock < minimumStock;
    }

    // Helper method to check if stock exceeds maximum
    public boolean exceedsMaximum(Integer currentStock) {
        return maximumStock != null && currentStock != null && currentStock > maximumStock;
    }
}
