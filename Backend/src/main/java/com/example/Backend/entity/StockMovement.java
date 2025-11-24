package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock Movement Entity - Tracks all inventory movements
 */
@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_stock_movements_product", columnList = "product_id"),
        @Index(name = "idx_stock_movements_date", columnList = "movement_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotNull(message = "Product is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "suppliers" })
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Positive for IN, negative for OUT

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Column(name = "reference_id", columnDefinition = "UUID")
    private UUID referenceId; // OrderID, PurchaseOrderID, etc.

    @Column(name = "reference_type")
    private String referenceType; // ORDER, PURCHASE_ORDER, ADJUSTMENT, RETURN

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "performed_by")
    private String performedBy; // Username of admin/user who made the change

    @CreationTimestamp
    @Column(name = "movement_date", nullable = false, updatable = false)
    private LocalDateTime movementDate;

    // Movement Type Enum
    public enum MovementType {
        PURCHASE, // Stock received from supplier
        SALE, // Stock sold to customer
        RETURN_FROM_CUSTOMER, // Customer returned product
        RETURN_TO_SUPPLIER, // Returned to supplier
        ADJUSTMENT, // Manual adjustment
        DAMAGED, // Damaged/Expired stock removal
        TRANSFER, // Transfer between warehouses
        INITIAL // Initial stock entry
    }
}
