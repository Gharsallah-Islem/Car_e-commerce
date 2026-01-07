package com.example.carpartsecom.data.remote.api

import com.example.carpartsecom.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentService {
    @POST("api/payment/create-intent")
    suspend fun createPaymentIntent(
        @Header("Authorization") token: String,
        @Body request: CreatePaymentIntentRequest
    ): Response<PaymentIntentResponse>

    @GET("api/payment/verify/{paymentIntentId}")
    suspend fun verifyPaymentIntent(
        @Header("Authorization") token: String,
        @Path("paymentIntentId") paymentIntentId: String
    ): Response<PaymentVerifyResponse>
}
