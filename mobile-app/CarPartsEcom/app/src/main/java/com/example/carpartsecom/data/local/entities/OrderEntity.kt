package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Long,
    val userId: Long?,
    val totalAmount: Double?,
    val status: String?,
    val paymentMethod: String?,
    val paymentIntentId: String?,
    val deliveryLatitude: Double?,
    val deliveryLongitude: Double?,
    val deliveryAddress: String?,
    val contactPhone: String?,
    val deliveryNotes: String?,
    val createdAt: String?
)
