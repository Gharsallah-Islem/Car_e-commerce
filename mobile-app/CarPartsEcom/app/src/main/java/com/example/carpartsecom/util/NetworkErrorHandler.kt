package com.example.carpartsecom.util

import retrofit2.Response
import java.io.IOException

object NetworkErrorHandler {
    
    fun <T> handleResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            val errorMessage = when (response.code()) {
                400 -> "Bad request: $errorBody"
                401 -> "Unauthorized: $errorBody"
                403 -> "Access forbidden: $errorBody"
                404 -> "Resource not found: $errorBody"
                500 -> "Server error: $errorBody"
                else -> "Error ${response.code()}: $errorBody"
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    fun handleException(e: Exception): Result<Nothing> {
        val errorMessage = when (e) {
            is IOException -> "Network error. Please check your connection."
            else -> "An error occurred: ${e.message}"
        }
        return Result.failure(Exception(errorMessage))
    }
}
