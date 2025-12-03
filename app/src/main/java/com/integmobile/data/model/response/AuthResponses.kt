package com.integmobile.data.model.response

/**
 * Response models for authentication endpoints
 */

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData?
)

data class UserData(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val token: String,
    val refreshToken: String?,
    val profileImage: String?,
    val isVerified: Boolean
)

data class VerifyEmailResponse(
    val success: Boolean,
    val message: String,
    val verified: Boolean
)

data class PasswordResetResponse(
    val success: Boolean,
    val message: String
)
