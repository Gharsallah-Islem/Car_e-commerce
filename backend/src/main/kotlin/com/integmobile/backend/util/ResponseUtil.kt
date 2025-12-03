package com.integmobile.backend.util

import com.integmobile.backend.model.response.ApiResponse

object ResponseUtil {
    
    fun <T> success(data: T, message: String? = null): ApiResponse<T> {
        return ApiResponse(success = true, message = message, data = data)
    }
    
    fun <T> error(message: String): ApiResponse<T> {
        return ApiResponse(success = false, message = message, data = null)
    }
}
