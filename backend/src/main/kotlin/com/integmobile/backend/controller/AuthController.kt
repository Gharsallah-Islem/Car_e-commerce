package com.integmobile.backend.controller

import com.integmobile.backend.model.request.*
import com.integmobile.backend.model.response.ApiResponse
import com.integmobile.backend.model.response.AuthResponse
import com.integmobile.backend.model.response.OtpResponse
import com.integmobile.backend.service.AuthService
import com.integmobile.backend.util.ResponseUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val response = authService.login(request)
            ResponseEntity.ok(ResponseUtil.success(response, "Login successful"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Login failed"))
        }
    }
    
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<OtpResponse>> {
        return try {
            val response = authService.register(request)
            ResponseEntity.ok(ResponseUtil.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Registration failed"))
        }
    }
    
    @PostMapping("/verify-email")
    fun verifyEmail(@RequestBody request: VerifyEmailRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val response = authService.verifyEmail(request)
            ResponseEntity.ok(ResponseUtil.success(response, "Email verified successfully"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Verification failed"))
        }
    }
    
    @PostMapping("/google-signin")
    fun googleSignIn(@RequestBody request: GoogleSignInRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        return try {
            val response = authService.googleSignIn(request)
            ResponseEntity.ok(ResponseUtil.success(response, "Google sign-in successful"))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Google sign-in failed"))
        }
    }
    
    @PostMapping("/request-password-reset")
    fun requestPasswordReset(@RequestBody request: RequestPasswordResetRequest): ResponseEntity<ApiResponse<OtpResponse>> {
        return try {
            val response = authService.requestPasswordReset(request)
            ResponseEntity.ok(ResponseUtil.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Request failed"))
        }
    }
    
    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody request: VerifyOtpRequest): ResponseEntity<ApiResponse<OtpResponse>> {
        return try {
            val response = authService.verifyOtp(request)
            ResponseEntity.ok(ResponseUtil.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "OTP verification failed"))
        }
    }
    
    @PostMapping("/reset-password")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<OtpResponse>> {
        return try {
            val response = authService.resetPassword(request)
            ResponseEntity.ok(ResponseUtil.success(response))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(ResponseUtil.error(e.message ?: "Password reset failed"))
        }
    }
}
