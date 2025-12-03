package com.integmobile.data.model.request

import com.integmobile.data.db.entity.OrderItem

/**
 * Request models for order endpoints
 */

data class CreateOrderRequest(
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val latitude: Double,
    val longitude: Double,
    val paymentMethod: String,
    val phoneNumber: String
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
