package com.integmobile.data.model.response

/**
 * Response models for payment endpoints
 */

data class PaymentIntentResponse(
    val success: Boolean,
    val message: String,
    val clientSecret: String?,
    val paymentIntentId: String?
)

data class PaymentConfirmationResponse(
    val success: Boolean,
    val message: String,
    val paymentStatus: String?
)
