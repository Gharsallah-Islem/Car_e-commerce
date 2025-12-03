package com.integmobile.data.repository

import com.integmobile.data.api.AuthApiService
import com.integmobile.data.db.dao.UserDao
import com.integmobile.data.db.entity.User
import com.integmobile.data.model.request.*
import com.integmobile.data.model.response.UserData
import com.integmobile.utils.Result
import com.integmobile.utils.TokenManager

/**
 * Repository for authentication operations
 * Handles API calls and Room persistence for user session
 * Single source of truth for authentication state
 */
class AuthRepository(
    private val apiService: AuthApiService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) {
    
    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<UserData> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    // Save to Room
                    saveUserToDatabase(userData)
                    // Save token to SharedPreferences
                    tokenManager.saveToken(userData.token)
                    userData.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                    tokenManager.saveUserId(userData.id)
                    tokenManager.saveUserEmail(userData.email)
                    tokenManager.setLoggedIn(true)
                    
                    Result.Success(userData)
                } else {
                    Result.Error(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Login failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Register new user
     */
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        phoneNumber: String?
    ): Result<UserData> {
        return try {
            val request = SignUpRequest(email, password, fullName, phoneNumber)
            val response = apiService.register(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    Result.Success(userData)
                } else {
                    Result.Error(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Registration failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Verify email with OTP
     */
    suspend fun verifyEmail(email: String, otp: String): Result<Boolean> {
        return try {
            val response = apiService.verifyEmail(VerifyEmailRequest(email, otp))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.verified ?: false)
            } else {
                val errorMessage = response.body()?.message ?: "Verification failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Request password reset (sends OTP)
     */
    suspend fun requestPasswordReset(email: String): Result<Boolean> {
        return try {
            val response = apiService.requestPasswordReset(RequestPasswordResetRequest(email))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(true)
            } else {
                val errorMessage = response.body()?.message ?: "Request failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Verify OTP for password reset
     */
    suspend fun verifyOTP(email: String, otp: String): Result<Boolean> {
        return try {
            val response = apiService.verifyOTP(VerifyEmailRequest(email, otp))
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(response.body()?.verified ?: false)
            } else {
                val errorMessage = response.body()?.message ?: "OTP verification failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Reset password with OTP
     */
    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Boolean> {
        return try {
            val request = ResetPasswordRequest(email, otp, newPassword)
            val response = apiService.resetPassword(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.Success(true)
            } else {
                val errorMessage = response.body()?.message ?: "Password reset failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Google Sign-In
     */
    suspend fun googleSignIn(idToken: String): Result<UserData> {
        return try {
            val response = apiService.googleSignIn(GoogleSignInRequest(idToken))
            
            if (response.isSuccessful && response.body()?.success == true) {
                val userData = response.body()?.data
                if (userData != null) {
                    // Save to Room
                    saveUserToDatabase(userData)
                    // Save token to SharedPreferences
                    tokenManager.saveToken(userData.token)
                    userData.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                    tokenManager.saveUserId(userData.id)
                    tokenManager.saveUserEmail(userData.email)
                    tokenManager.setLoggedIn(true)
                    
                    Result.Success(userData)
                } else {
                    Result.Error(Exception("Invalid response data"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Google sign-in failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Logout user
     */
    suspend fun logout() {
        userDao.deleteAll()
        tokenManager.clearAll()
    }
    
    /**
     * Get current user from Room
     */
    suspend fun getCurrentUser(): User? {
        return userDao.getCurrentUser()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    /**
     * Save user data to Room database
     */
    private suspend fun saveUserToDatabase(userData: UserData) {
        val user = User(
            id = userData.id,
            email = userData.email,
            fullName = userData.fullName,
            phoneNumber = userData.phoneNumber,
            token = userData.token,
            refreshToken = userData.refreshToken,
            profileImage = userData.profileImage,
            isVerified = userData.isVerified
        )
        userDao.insert(user)
    }
}
