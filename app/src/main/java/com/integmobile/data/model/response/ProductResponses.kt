package com.integmobile.data.model.response

import com.integmobile.data.db.entity.Product

/**
 * Response models for product endpoints
 */

data class ProductListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Product>
)

data class ProductDetailResponse(
    val success: Boolean,
    val message: String,
    val data: Product?
)
