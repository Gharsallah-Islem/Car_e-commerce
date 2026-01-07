package com.example.carpartsecom.data.repository

import androidx.lifecycle.LiveData
import com.example.carpartsecom.data.local.dao.UserDao
import com.example.carpartsecom.data.local.entities.UserEntity
import com.example.carpartsecom.data.remote.api.AuthService
import com.example.carpartsecom.data.remote.dto.*
import com.example.carpartsecom.util.NetworkErrorHandler
import com.example.carpartsecom.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val authService: AuthService,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) {
    fun getCurrentUser(): LiveData<UserEntity?> = userDao.getCurrentUser()
    
    suspend fun register(email: String, password: String, firstName: String, lastName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(email, password, firstName, lastName)
            val response = authService.register(request)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Registration successful")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Registration failed: $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun verifyEmail(email: String, code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = VerifyEmailRequest(email, code)
            val response = authService.verifyEmail(request)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Email verified")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Verification failed: $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val request = LoginRequest(email, password)
            val response = authService.login(request)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveToken(loginResponse.token)
                userDao.insertUser(loginResponse.user.toEntity(loginResponse.token))
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun googleSignIn(idToken: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val request = GoogleSignInRequest(idToken)
            val response = authService.googleSignIn(request)
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                tokenManager.saveToken(loginResponse.token)
                userDao.insertUser(loginResponse.user.toEntity(loginResponse.token))
                Result.success(loginResponse)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Google Sign-In failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun forgotPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = ForgotPasswordRequest(email)
            val response = authService.forgotPassword(request)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Reset code sent")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed: $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = ResetPasswordRequest(email, code, newPassword)
            val response = authService.resetPassword(request)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Password reset successful")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed: $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userDao.clearUser()
            tokenManager.clearToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
