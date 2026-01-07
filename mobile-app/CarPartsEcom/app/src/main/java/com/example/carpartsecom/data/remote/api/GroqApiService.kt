package com.example.carpartsecom.data.remote.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Groq API Service for AI Chat
 * Free tier: 30 requests/minute, 6000 requests/day
 * Get your free API key at: https://console.groq.com/keys
 */
interface GroqApiService {

    @POST("openai/v1/chat/completions")
    suspend fun chat(
        @Header("Authorization") apiKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: RequestBody
    ): Response<ResponseBody>
}

