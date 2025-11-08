package com.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Purchase Order Entity - Manages orders from suppliers
 */
@Entity
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "po_number", nullable = false, unique = true)
    private String poNumber; // PO-2024-001

    @NotNull(message = "Supplier is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private POStatus status = POStatus.DRAFT;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_cost", precision = 12, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by")
    private String createdBy; // Admin username

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "received_by")
    private String receivedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    // Purchase Order Status Enum
    public enum POStatus {
        DRAFT, // Being created
        PENDING, // Waiting for approval
        APPROVED, // Approved and sent to supplier
        ORDERED, // Order placed with supplier
        SHIPPED, // Supplier shipped the order
        RECEIVED, // Goods received
        CANCELLED, // Order cancelled
        COMPLETED // Order completed and stock updated
    }

    // Helper method to calculate totals
    public void calculateTotals() {
        this.totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.grandTotal = this.totalAmount
                .add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO)
                .add(this.shippingCost != null ? this.shippingCost : BigDecimal.ZERO)
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO);
    }
}
