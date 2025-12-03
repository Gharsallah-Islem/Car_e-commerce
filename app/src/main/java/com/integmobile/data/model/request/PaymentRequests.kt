package com.integmobile.data.model.request

/**
 * Request models for payment endpoints
 */

data class CreatePaymentIntentRequest(
    val amount: Double,
    val currency: String = "dzd"
)

data class ConfirmPaymentRequest(
    val paymentIntentId: String,
    val paymentMethodId: String
)
