package com.example.carpartsecom.data.repository

import com.example.carpartsecom.data.remote.api.UserService
import com.example.carpartsecom.data.remote.dto.ChangePasswordRequest
import com.example.carpartsecom.data.remote.dto.UpdateProfileRequest
import com.example.carpartsecom.data.remote.dto.UserProfileResponse
import com.example.carpartsecom.util.TokenManager

class UserRepository(
    private val userService: UserService,
    private val tokenManager: TokenManager
) {
    suspend fun getProfile(): Result<UserProfileResponse> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            val response = userService.getProfile("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateProfile(
        firstName: String?,
        lastName: String?,
        phoneNumber: String?
    ): Result<UserProfileResponse> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            val request = UpdateProfileRequest(firstName, lastName, phoneNumber)
            val response = userService.updateProfile("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<String> {
        return try {
            val token = tokenManager.getToken() ?: return Result.failure(Exception("No authentication token"))
            val request = ChangePasswordRequest(currentPassword, newPassword)
            val response = userService.changePassword("Bearer $token", request)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Password changed successfully")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to change password"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
