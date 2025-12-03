package com.integmobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.integmobile.data.db.converters.OrderItemListConverter

/**
 * Room entity for order history
 * Stores order data with status tracking
 */
@Entity(tableName = "orders")
@TypeConverters(OrderItemListConverter::class)
data class Order(
    @PrimaryKey
    val id: String,
    val userId: String,
    val orderDate: Long,
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val latitude: Double,
    val longitude: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val status: String, // PENDING, CONFIRMED, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    val trackingNumber: String? = null,
    val estimatedDeliveryDate: Long? = null,
    val actualDeliveryDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Data class for order items (nested in Order)
 */
data class OrderItem(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)
