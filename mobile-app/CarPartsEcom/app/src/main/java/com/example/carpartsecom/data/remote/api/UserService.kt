package com.example.carpartsecom.data.remote.api

import com.example.carpartsecom.data.remote.dto.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("api/user/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserProfileResponse>
    
    @PUT("api/user/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserProfileResponse>
    
    @PUT("api/user/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ResponseBody>
}
