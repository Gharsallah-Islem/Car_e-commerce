package com.integmobile.data.api

import com.integmobile.utils.Constants
import com.integmobile.utils.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client with OkHttp configuration
 * Provides centralized API service creation with interceptors
 */
object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    // Convenience methods for specific services
    val authService: AuthApiService by lazy {
        createService(AuthApiService::class.java)
    }

    val productService: ProductApiService by lazy {
        createService(ProductApiService::class.java)
    }

    val cartService: CartApiService by lazy {
        createService(CartApiService::class.java)
    }

    val orderService: OrderApiService by lazy {
        createService(OrderApiService::class.java)
    }

    val paymentService: PaymentApiService by lazy {
        createService(PaymentApiService::class.java)
    }
}
