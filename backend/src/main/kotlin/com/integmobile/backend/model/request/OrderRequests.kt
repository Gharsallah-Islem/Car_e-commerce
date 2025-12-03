package com.integmobile.backend.model.request

import com.integmobile.backend.model.entity.OrderItem

data class CreateOrderRequest(
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val latitude: Double,
    val longitude: Double,
    val phoneNumber: String,
    val paymentMethod: String
)

data class CancelOrderRequest(
    val reason: String
)

data class SubmitClaimRequest(
    val orderId: String,
    val reason: String,
    val description: String,
    val photos: List<String> = emptyList()
)
