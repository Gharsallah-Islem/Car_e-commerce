package com.integmobile.backend.model.response

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

data class AuthResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val isVerified: Boolean,
    val profileImage: String?
)

data class OtpResponse(
    val message: String,
    val email: String
)
