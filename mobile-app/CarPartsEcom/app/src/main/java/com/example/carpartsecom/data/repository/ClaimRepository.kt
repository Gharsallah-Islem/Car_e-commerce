package com.example.carpartsecom.data.repository

import androidx.lifecycle.LiveData
import com.example.carpartsecom.data.local.dao.ClaimDao
import com.example.carpartsecom.data.local.entities.ClaimEntity
import com.example.carpartsecom.data.remote.api.ClaimService
import com.example.carpartsecom.data.remote.dto.CreateClaimRequest
import com.example.carpartsecom.data.remote.dto.toEntity
import com.example.carpartsecom.util.NetworkErrorHandler
import com.example.carpartsecom.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ClaimRepository(
    private val claimService: ClaimService,
    private val claimDao: ClaimDao,
    private val tokenManager: TokenManager
) {
    fun getUserClaims(): LiveData<List<ClaimEntity>> = claimDao.getUserClaims()
    
    suspend fun createClaim(orderId: Long, subject: String, description: String): Result<ClaimEntity> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val request = CreateClaimRequest(orderId, subject, description)
            val response = claimService.createClaim("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                val claim = response.body()!!.toEntity()
                claimDao.insertClaim(claim)
                Result.success(claim)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun getClaims(): Result<List<ClaimEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = claimService.getClaims("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val claims = response.body()!!.map { it.toEntity() }
                claims.forEach { claimDao.insertClaim(it) }
                Result.success(claims)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun getClaimsByOrder(orderId: Long): Result<List<ClaimEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = claimService.getClaimsByOrder(orderId, "Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val claims = response.body()!!.map { it.toEntity() }
                claims.forEach { claimDao.insertClaim(it) }
                Result.success(claims)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
}
