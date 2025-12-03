package com.integmobile.backend.model.response

data class CartItemResponse(
    val id: String,
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

data class CartSummaryResponse(
    val items: List<CartItemResponse>,
    val subtotal: Double,
    val tax: Double,
    val shipping: Double,
    val total: Double
)
