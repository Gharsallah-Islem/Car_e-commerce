package com.integmobile.backend.model.request

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String?
)

data class VerifyEmailRequest(
    val email: String,
    val otp: String
)

data class GoogleSignInRequest(
    val idToken: String
)

data class RequestPasswordResetRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)
