package com.integmobile.data.repository

import com.integmobile.data.api.PaymentApiService
import com.integmobile.data.model.request.ConfirmPaymentRequest
import com.integmobile.data.model.request.CreatePaymentIntentRequest
import com.integmobile.utils.Result

/**
 * Repository for payment operations
 * Handles Stripe payment intent creation and confirmation
 */
class PaymentRepository(
    private val apiService: PaymentApiService
) {
    
    /**
     * Create Stripe payment intent
     * Returns client secret for Stripe SDK
     */
    suspend fun createPaymentIntent(amount: Double, currency: String = "dzd"): Result<String> {
        return try {
            val request = CreatePaymentIntentRequest(amount, currency)
            val response = apiService.createPaymentIntent(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val clientSecret = response.body()?.clientSecret
                if (!clientSecret.isNullOrEmpty()) {
                    Result.Success(clientSecret)
                } else {
                    Result.Error(Exception("Invalid client secret"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Payment intent creation failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Confirm payment with Stripe
     */
    suspend fun confirmPayment(paymentIntentId: String, paymentMethodId: String): Result<String> {
        return try {
            val request = ConfirmPaymentRequest(paymentIntentId, paymentMethodId)
            val response = apiService.confirmPayment(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val paymentStatus = response.body()?.paymentStatus ?: "unknown"
                Result.Success(paymentStatus)
            } else {
                val errorMessage = response.body()?.message ?: "Payment confirmation failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
}
