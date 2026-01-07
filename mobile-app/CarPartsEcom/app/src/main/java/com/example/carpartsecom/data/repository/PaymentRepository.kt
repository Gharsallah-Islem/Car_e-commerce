package com.example.carpartsecom.data.repository

import com.example.carpartsecom.data.remote.api.PaymentService
import com.example.carpartsecom.data.remote.dto.CreatePaymentIntentRequest
import com.example.carpartsecom.data.remote.dto.PaymentIntentResponse
import com.example.carpartsecom.data.remote.dto.PaymentVerifyResponse
import com.example.carpartsecom.util.NetworkErrorHandler
import com.example.carpartsecom.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PaymentRepository(
    private val paymentService: PaymentService,
    private val tokenManager: TokenManager
) {
    // Backend calculates amount from cart - no need to pass amount
    suspend fun createPaymentIntent(): Result<PaymentIntentResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext Result.failure(Exception("Not authenticated"))
            val request = CreatePaymentIntentRequest()
            val response = paymentService.createPaymentIntent("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            NetworkErrorHandler.handleException(e)
        }
    }

    // Legacy method for backward compatibility
    suspend fun createPaymentIntent(amount: Long): Result<PaymentIntentResponse> = createPaymentIntent()

    suspend fun verifyPaymentIntent(paymentIntentId: String): Result<PaymentVerifyResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() ?: return@withContext Result.failure(Exception("Not authenticated"))
            val response = paymentService.verifyPaymentIntent("Bearer $token", paymentIntentId)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            NetworkErrorHandler.handleException(e)
        }
    }
}
