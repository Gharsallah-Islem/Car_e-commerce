package com.example.carpartsecom.data.remote.api

import com.example.carpartsecom.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface OrderService {
    @POST("api/orders")
    suspend fun createOrder(
        @Header("Authorization") token: String,
        @Body request: CreateOrderRequest
    ): Response<OrderResponse>
    
    @GET("api/orders")
    suspend fun getOrders(@Header("Authorization") token: String): Response<List<OrderResponse>>
    
    @POST("api/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") orderId: Long,
        @Header("Authorization") token: String
    ): Response<ResponseBody>
}
