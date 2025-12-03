package com.integmobile.data.api

import com.integmobile.data.model.request.AddToCartRequest
import com.integmobile.data.model.request.UpdateCartItemRequest
import com.integmobile.data.model.response.CartItemResponse
import com.integmobile.data.model.response.CartResponse
import com.integmobile.utils.Constants
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service for cart endpoints
 */
interface CartApiService {
    
    @GET(Constants.Endpoints.CART_ITEMS)
    suspend fun getCartItems(): Response<CartResponse>
    
    @POST(Constants.Endpoints.CART_ITEMS)
    suspend fun addToCart(@Body request: AddToCartRequest): Response<CartItemResponse>
    
    @PUT(Constants.Endpoints.CART_UPDATE)
    suspend fun updateCartItem(
        @Path("id") cartItemId: String,
        @Body request: UpdateCartItemRequest
    ): Response<CartItemResponse>
    
    @DELETE(Constants.Endpoints.CART_DELETE)
    suspend fun removeFromCart(@Path("id") cartItemId: String): Response<CartItemResponse>
}
