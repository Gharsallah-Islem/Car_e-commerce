package com.integmobile.backend.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

data class OrderItem(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

@Entity
@Table(name = "orders")
data class Order(
    @Id
    val id: String = UUID.randomUUID().toString(),
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @Column(columnDefinition = "jsonb", nullable = false)
    val items: List<OrderItem> = emptyList(),
    
    @Column(name = "total_amount", nullable = false)
    val totalAmount: Double,
    
    @Column(nullable = false)
    val subtotal: Double,
    
    @Column(nullable = false)
    val tax: Double,
    
    @Column(nullable = false)
    val shipping: Double,
    
    @Column(nullable = false)
    var status: String, // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    
    @Column(name = "delivery_address", nullable = false, columnDefinition = "TEXT")
    val deliveryAddress: String,
    
    val latitude: Double,
    
    val longitude: Double,
    
    @Column(name = "phone_number", nullable = false)
    val phoneNumber: String,
    
    @Column(name = "payment_method", nullable = false)
    val paymentMethod: String,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
