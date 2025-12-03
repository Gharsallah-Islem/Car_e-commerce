package com.integmobile.data.model.response

import com.integmobile.data.db.entity.Order

/**
 * Response models for order endpoints
 */

data class OrderListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Order>?
)

data class OrderDetailResponse(
    val success: Boolean,
    val message: String,
    val data: Order?
)

data class CancelOrderResponse(
    val success: Boolean,
    val message: String
)

data class ClaimResponse(
    val success: Boolean,
    val message: String,
    val claimId: String?
)
