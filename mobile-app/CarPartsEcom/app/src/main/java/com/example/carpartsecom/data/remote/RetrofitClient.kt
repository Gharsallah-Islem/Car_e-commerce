package com.example.carpartsecom.data.remote

import com.example.carpartsecom.data.remote.dto.CartItemResponse
import com.example.carpartsecom.data.remote.dto.ProductResponse
import com.example.carpartsecom.util.Constants
import com.example.carpartsecom.util.SingleOrListDeserializer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private var retrofit: Retrofit? = null
    
    fun getClient(): Retrofit {
        if (retrofit == null) {
            // Create custom Gson that handles single object or array responses
            val gson = GsonBuilder()
                .registerTypeAdapter(
                    object : TypeToken<List<ProductResponse>>() {}.type,
                    SingleOrListDeserializer<ProductResponse>()
                )
                .registerTypeAdapter(
                    object : TypeToken<List<CartItemResponse>>() {}.type,
                    SingleOrListDeserializer<CartItemResponse>()
                )
                .create()

            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }
}
