package com.example.carpartsecom.data.remote.api

import com.example.carpartsecom.data.remote.dto.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<ProductResponse>>
    
    @GET("api/products/{id}")
    suspend fun getProductById(@Path("id") id: Long): Response<ProductResponse>
    
    @GET("api/products/search")
    suspend fun searchProducts(@Query("query") query: String): Response<List<ProductResponse>>
    
    @GET("api/products/sort")
    suspend fun sortProducts(@Query("by") sortBy: String): Response<List<ProductResponse>>
    
    @GET("api/products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): Response<List<ProductResponse>>
}
