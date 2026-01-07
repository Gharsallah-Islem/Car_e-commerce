package com.example.carpartsecom.data.remote.dto

data class AddToCartRequest(
    val productId: Long,
    val quantity: Int
)

data class UpdateCartRequest(
    val productId: Long,
    val quantity: Int
)

data class CartItemResponse(
    val id: Long,
    val userId: Long,
    val productId: Long,
    val quantity: Int,
    val addedAt: String
)
