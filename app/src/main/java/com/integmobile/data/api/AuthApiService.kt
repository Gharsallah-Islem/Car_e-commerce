package com.integmobile.data.api

import com.integmobile.data.model.request.*
import com.integmobile.data.model.response.*
import com.integmobile.utils.Constants
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API service for authentication endpoints
 */
interface AuthApiService {
    
    @POST(Constants.Endpoints.LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST(Constants.Endpoints.REGISTER)
    suspend fun register(@Body request: SignUpRequest): Response<AuthResponse>
    
    @POST(Constants.Endpoints.VERIFY_EMAIL)
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<VerifyEmailResponse>
    
    @POST(Constants.Endpoints.REQUEST_PASSWORD_RESET)
    suspend fun requestPasswordReset(@Body request: RequestPasswordResetRequest): Response<PasswordResetResponse>
    
    @POST(Constants.Endpoints.VERIFY_OTP)
    suspend fun verifyOTP(@Body request: VerifyEmailRequest): Response<VerifyEmailResponse>
    
    @POST(Constants.Endpoints.RESET_PASSWORD)
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<PasswordResetResponse>
    
    @POST(Constants.Endpoints.GOOGLE_SIGNIN)
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<AuthResponse>
}
