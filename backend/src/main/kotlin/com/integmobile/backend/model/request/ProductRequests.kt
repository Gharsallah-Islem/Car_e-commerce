package com.integmobile.backend.model.request

data class ProductFilterRequest(
    val brands: List<String>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val inStockOnly: Boolean = false
)
