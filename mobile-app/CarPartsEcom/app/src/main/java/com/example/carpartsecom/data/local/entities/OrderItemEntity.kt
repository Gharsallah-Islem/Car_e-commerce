package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: Long,
    val orderId: Long?,
    val productId: Long?,
    val productName: String?,
    val quantity: Int?,
    val priceAtPurchase: Double?
)
