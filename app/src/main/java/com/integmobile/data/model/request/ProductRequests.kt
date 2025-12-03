package com.integmobile.data.model.request

/**
 * Request models for product endpoints
 */

data class SearchProductsRequest(
    val query: String
)

data class FilterProductsRequest(
    val brands: List<String>? = null,
    val categories: List<String>? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val inStockOnly: Boolean = false,
    val sortBy: String? = null
)
