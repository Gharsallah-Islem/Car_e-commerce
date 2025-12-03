package com.integmobile.utils

import com.integmobile.CarPartsApplication
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor to add Authorization header to all requests
 * Automatically injects JWT token from TokenManager
 */
class TokenInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = CarPartsApplication.instance.tokenManager
        val token = tokenManager.getToken()
        
        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        
        return chain.proceed(request)
    }
}
