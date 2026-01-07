package com.example.carpartsecom.data.remote.dto

// Note: Backend creates payment intent from cart contents automatically
// The amount field is no longer needed - backend calculates from cart
data class CreatePaymentIntentRequest(
    val amount: Long? = null  // Optional - backend calculates from cart
)

data class PaymentIntentResponse(
    val clientSecret: String,
    val paymentIntentId: String,
    val amount: Double
)

data class PaymentVerifyResponse(
    val amount: Long,
    val currency: String,
    val status: String
)
