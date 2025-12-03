package com.integmobile.data.api

import com.integmobile.data.model.request.FilterProductsRequest
import com.integmobile.data.model.response.ProductDetailResponse
import com.integmobile.data.model.response.ProductListResponse
import com.integmobile.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit API service for product endpoints
 */
interface ProductApiService {
    
    @GET(Constants.Endpoints.PRODUCTS)
    suspend fun getAllProducts(): Response<ProductListResponse>
    
    @GET(Constants.Endpoints.PRODUCT_DETAIL)
    suspend fun getProductById(@Path("id") productId: String): Response<ProductDetailResponse>
    
    @GET(Constants.Endpoints.SEARCH_PRODUCTS)
    suspend fun searchProducts(@Query("query") query: String): Response<ProductListResponse>
    
    @POST(Constants.Endpoints.FILTER_PRODUCTS)
    suspend fun filterProducts(@Body request: FilterProductsRequest): Response<ProductListResponse>
}
