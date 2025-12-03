package com.integmobile.data.model.response

import com.integmobile.data.db.entity.CartItem

/**
 * Response models for cart endpoints
 */

data class CartResponse(
    val success: Boolean,
    val message: String,
    val data: List<CartItem>?
)

data class CartItemResponse(
    val success: Boolean,
    val message: String,
    val data: CartItem?
)

data class CartSummary(
    val subtotal: Double,
    val tax: Double,
    val shipping: Double,
    val total: Double,
    val itemCount: Int
)
