package com.example.Backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
@Table(name = "products", indexes = {
        @Index(name = "idx_products_brand_model_year", columnList = "brand, model, year")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Product name is required")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "compatibility", columnDefinition = "TEXT")
    private String compatibility; // Comma-separated string of compatible models

    /**
     * Get compatibility as a simple string for frontend display
     * The frontend expects this as a string to display in the UI
     */
    @com.fasterxml.jackson.annotation.JsonProperty("compatibilityString")
    public String getCompatibilityString() {
        return compatibility;
    }

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "category", length = 100)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Helper methods
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

    public boolean isLowStock() {
        return stock != null && stock > 0 && stock <= 5;
    }

    public void decreaseStock(int quantity) {
        if (stock >= quantity) {
            this.stock -= quantity;
        } else {
            throw new IllegalArgumentException("Insufficient stock");
        }
    }

    public void increaseStock(int quantity) {
        this.stock += quantity;
    }
}
