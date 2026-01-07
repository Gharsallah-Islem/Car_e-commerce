package com.example.carpartsecom.data.remote.dto

import com.example.carpartsecom.data.local.entities.UserEntity

// Request DTOs
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleSignInRequest(
    val idToken: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)

// Response DTOs
data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Long,
    val email: String,
    val verified: Boolean,
    val createdAt: Any?,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val googleId: String? = null
)

// User Profile DTOs
data class UserProfileResponse(
    val id: Long,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val verified: Boolean,
    val createdAt: String
)

data class UpdateProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// Mapper
fun User.toEntity(token: String): UserEntity {
    return UserEntity(
        id = this.id,
        email = this.email,
        token = token,
        isVerified = this.verified,
        createdAt = this.createdAt?.toString(),
        firstName = this.firstName,
        lastName = this.lastName,
        phoneNumber = this.phoneNumber,
        googleId = this.googleId
    )
}
