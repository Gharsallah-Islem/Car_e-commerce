package com.example.carpartsecom.data.remote.api

import com.example.carpartsecom.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ClaimService {
    @POST("api/claims")
    suspend fun createClaim(
        @Header("Authorization") token: String,
        @Body request: CreateClaimRequest
    ): Response<ClaimResponse>
    
    @GET("api/claims")
    suspend fun getClaims(@Header("Authorization") token: String): Response<List<ClaimResponse>>
    
    @GET("api/claims/order/{orderId}")
    suspend fun getClaimsByOrder(
        @Path("orderId") orderId: Long,
        @Header("Authorization") token: String
    ): Response<List<ClaimResponse>>
}
