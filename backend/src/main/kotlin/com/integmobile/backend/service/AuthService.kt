package com.integmobile.backend.service

import com.integmobile.backend.model.entity.OtpVerification
import com.integmobile.backend.model.entity.User
import com.integmobile.backend.model.request.*
import com.integmobile.backend.model.response.AuthResponse
import com.integmobile.backend.model.response.OtpResponse
import com.integmobile.backend.model.response.UserResponse
import com.integmobile.backend.repository.OtpVerificationRepository
import com.integmobile.backend.repository.UserRepository
import com.integmobile.backend.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val otpVerificationRepository: OtpVerificationRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {
    
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { Exception("Invalid email or password") }
        
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw Exception("Invalid email or password")
        }
        
        if (!user.isVerified) {
            throw Exception("Email not verified. Please verify your email first.")
        }
        
        val token = jwtTokenProvider.generateToken(user.id, user.email)
        
        return AuthResponse(
            token = token,
            refreshToken = null,
            user = user.toUserResponse()
        )
    }
    
    fun register(request: RegisterRequest): OtpResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw Exception("Email already registered")
        }
        
        // Create user
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            isVerified = false
        )
        userRepository.save(user)
        
        // Generate and send OTP
        val otp = generateOtp()
        saveOtp(request.email, otp, "EMAIL_VERIFICATION")
        
        // TODO: Send email with OTP
        println("OTP for ${request.email}: $otp")
        
        return OtpResponse(
            message = "Registration successful. Please verify your email with the OTP sent.",
            email = request.email
        )
    }
    
    fun verifyEmail(request: VerifyEmailRequest): AuthResponse {
        val otpVerification = otpVerificationRepository
            .findByEmailAndOtpAndPurposeAndExpiresAtAfterAndVerifiedFalse(
                request.email,
                request.otp,
                "EMAIL_VERIFICATION",
                LocalDateTime.now()
            ) ?: throw Exception("Invalid or expired OTP")
        
        // Mark OTP as verified
        otpVerification.verified = true
        otpVerificationRepository.save(otpVerification)
        
        // Mark user as verified
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { Exception("User not found") }
        user.isVerified = true
        userRepository.save(user)
        
        // Generate token
        val token = jwtTokenProvider.generateToken(user.id, user.email)
        
        return AuthResponse(
            token = token,
            refreshToken = null,
            user = user.toUserResponse()
        )
    }
    
    fun requestPasswordReset(request: RequestPasswordResetRequest): OtpResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { Exception("Email not found") }
        
        // Generate and send OTP
        val otp = generateOtp()
        saveOtp(request.email, otp, "PASSWORD_RESET")
        
        // TODO: Send email with OTP
        println("Password reset OTP for ${request.email}: $otp")
        
        return OtpResponse(
            message = "OTP sent to your email",
            email = request.email
        )
    }
    
    fun verifyOtp(request: VerifyOtpRequest): OtpResponse {
        val otpVerification = otpVerificationRepository
            .findByEmailAndOtpAndPurposeAndExpiresAtAfterAndVerifiedFalse(
                request.email,
                request.otp,
                "PASSWORD_RESET",
                LocalDateTime.now()
            ) ?: throw Exception("Invalid or expired OTP")
        
        return OtpResponse(
            message = "OTP verified successfully",
            email = request.email
        )
    }
    
    fun resetPassword(request: ResetPasswordRequest): OtpResponse {
        // Verify OTP
        val otpVerification = otpVerificationRepository
            .findByEmailAndOtpAndPurposeAndExpiresAtAfterAndVerifiedFalse(
                request.email,
                request.otp,
                "PASSWORD_RESET",
                LocalDateTime.now()
            ) ?: throw Exception("Invalid or expired OTP")
        
        // Update password
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { Exception("User not found") }
        user.password = passwordEncoder.encode(request.newPassword)
        userRepository.save(user)
        
        // Mark OTP as verified
        otpVerification.verified = true
        otpVerificationRepository.save(otpVerification)
        
        return OtpResponse(
            message = "Password reset successfully",
            email = request.email
        )
    }
    
    fun googleSignIn(request: GoogleSignInRequest): AuthResponse {
        // TODO: Verify Google ID token
        // For now, just return a mock response
        throw Exception("Google Sign-In not implemented yet")
    }
    
    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }
    
    private fun saveOtp(email: String, otp: String, purpose: String) {
        // Delete old OTPs
        otpVerificationRepository.deleteByEmailAndPurpose(email, purpose)
        
        // Save new OTP
        val otpVerification = OtpVerification(
            email = email,
            otp = otp,
            purpose = purpose,
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )
        otpVerificationRepository.save(otpVerification)
    }
    
    private fun User.toUserResponse() = UserResponse(
        id = id,
        email = email,
        fullName = fullName,
        phoneNumber = phoneNumber,
        isVerified = isVerified,
        profileImage = profileImage
    )
}
