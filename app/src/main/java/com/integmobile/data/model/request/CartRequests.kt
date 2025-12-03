package com.integmobile.data.model.request

/**
 * Request models for cart endpoints
 */

data class AddToCartRequest(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

data class UpdateCartItemRequest(
    val quantity: Int
)
