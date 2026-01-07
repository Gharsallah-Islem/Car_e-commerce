package com.example.carpartsecom.data.remote.api

import com.example.carpartsecom.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface CartService {
    @POST("api/cart/add")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body request: AddToCartRequest
    ): Response<ResponseBody>
    
    @PUT("api/cart/update")
    suspend fun updateCart(
        @Header("Authorization") token: String,
        @Body request: UpdateCartRequest
    ): Response<ResponseBody>
    
    @GET("api/cart")
    suspend fun getCart(@Header("Authorization") token: String): Response<List<CartItemResponse>>
    
    @DELETE("api/cart/remove/{productId}")
    suspend fun removeFromCart(
        @Path("productId") productId: Long,
        @Header("Authorization") token: String
    ): Response<ResponseBody>
    
    @DELETE("api/cart/clear")
    suspend fun clearCart(@Header("Authorization") token: String): Response<ResponseBody>
}
