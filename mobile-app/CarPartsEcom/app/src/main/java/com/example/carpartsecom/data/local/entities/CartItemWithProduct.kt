package com.example.carpartsecom.data.local.entities

data class CartItemWithProduct(
    val cartItemId: Long,
    val productId: Long,
    val quantity: Int,
    val addedAt: String?,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String,
    val productStockQuantity: Int,
    val productCategory: String
) {
    val totalPrice: Double
        get() = productPrice * quantity
}
