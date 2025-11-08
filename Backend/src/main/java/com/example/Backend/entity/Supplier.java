package com.example.Backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Supplier Entity - Manages product suppliers
 */
@Entity
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @NotBlank(message = "Supplier name is required")
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "company_name")
    private String companyName;

    @Email(message = "Valid email is required")
    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country = "Maroc";

    @Column(name = "website")
    private String website;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "tax_id")
    private String taxId; // ICE in Morocco

    @Column(name = "payment_terms")
    private String paymentTerms; // e.g., "Net 30", "COD"

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "rating")
    private Double rating; // 0-5 stars

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL)
    private List<PurchaseOrder> purchaseOrders = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "supplier_products", joinColumns = @JoinColumn(name = "supplier_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<Product> products = new ArrayList<>();
}
