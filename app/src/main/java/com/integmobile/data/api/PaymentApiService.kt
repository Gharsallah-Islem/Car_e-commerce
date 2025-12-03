package com.integmobile.data.api

import com.integmobile.data.model.request.ConfirmPaymentRequest
import com.integmobile.data.model.request.CreatePaymentIntentRequest
import com.integmobile.data.model.response.PaymentConfirmationResponse
import com.integmobile.data.model.response.PaymentIntentResponse
import com.integmobile.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service for payment endpoints
 */
interface PaymentApiService {
    
    @POST(Constants.Endpoints.CREATE_PAYMENT_INTENT)
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): Response<PaymentIntentResponse>
    
    @POST(Constants.Endpoints.CONFIRM_PAYMENT)
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequest): Response<PaymentConfirmationResponse>
}
