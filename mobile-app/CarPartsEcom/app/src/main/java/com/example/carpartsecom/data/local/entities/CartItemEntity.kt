package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: Long,
    val userId: Long?,
    val productId: Long,
    val quantity: Int,
    val addedAt: String?
)
