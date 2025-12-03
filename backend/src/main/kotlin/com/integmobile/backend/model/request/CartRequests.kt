package com.integmobile.backend.model.request

data class AddToCartRequest(
    val productId: String,
    val quantity: Int
)

data class UpdateCartItemRequest(
    val quantity: Int
)
