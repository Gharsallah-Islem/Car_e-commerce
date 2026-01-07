package com.example.carpartsecom.data.remote.dto

import com.example.carpartsecom.data.local.entities.OrderEntity

data class CreateOrderRequest(
    val paymentMethod: String,
    val deliveryLatitude: Double,
    val deliveryLongitude: Double,
    val deliveryAddress: String? = null,
    val contactPhone: String? = null,
    val deliveryNotes: String? = null,
    val paymentIntentId: String? = null  // Required for card payments
)

data class OrderResponse(
    val id: Long,
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

// Mapper
fun OrderResponse.toEntity(): OrderEntity {
    return OrderEntity(
        id = this.id,
        userId = this.userId,
        totalAmount = this.totalAmount,
        status = this.status,
        paymentMethod = this.paymentMethod,
        paymentIntentId = this.paymentIntentId,
        deliveryLatitude = this.deliveryLatitude,
        deliveryLongitude = this.deliveryLongitude,
        deliveryAddress = this.deliveryAddress,
        contactPhone = this.contactPhone,
        deliveryNotes = this.deliveryNotes,
        createdAt = this.createdAt
    )
}
