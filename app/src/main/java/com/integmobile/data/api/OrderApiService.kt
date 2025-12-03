package com.integmobile.data.api

import com.integmobile.data.model.request.CancelOrderRequest
import com.integmobile.data.model.request.CreateOrderRequest
import com.integmobile.data.model.request.SubmitClaimRequest
import com.integmobile.data.model.response.CancelOrderResponse
import com.integmobile.data.model.response.ClaimResponse
import com.integmobile.data.model.response.OrderDetailResponse
import com.integmobile.data.model.response.OrderListResponse
import com.integmobile.utils.Constants
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service for order endpoints
 */
interface OrderApiService {
    
    @GET(Constants.Endpoints.ORDERS)
    suspend fun getOrders(): Response<OrderListResponse>
    
    @GET(Constants.Endpoints.ORDER_DETAIL)
    suspend fun getOrderById(@Path("id") orderId: String): Response<OrderDetailResponse>
    
    @POST(Constants.Endpoints.ORDERS)
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderDetailResponse>
    
    @PUT(Constants.Endpoints.CANCEL_ORDER)
    suspend fun cancelOrder(
        @Path("id") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<CancelOrderResponse>
    
    @POST(Constants.Endpoints.SUBMIT_CLAIM)
    suspend fun submitClaim(@Body request: SubmitClaimRequest): Response<ClaimResponse>
}
