package com.integmobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for shopping cart persistence
 * Stores cart items locally for offline access
 */
@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey
    val id: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)
