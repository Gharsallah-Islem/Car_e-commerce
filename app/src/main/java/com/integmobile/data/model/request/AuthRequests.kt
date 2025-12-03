package com.integmobile.data.model.request

/**
 * Request models for authentication endpoints
 */

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String? = null
)

data class VerifyEmailRequest(
    val email: String,
    val otp: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class RequestPasswordResetRequest(
    val email: String
)

data class GoogleSignInRequest(
    val idToken: String
)
