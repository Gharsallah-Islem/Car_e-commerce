package com.example.carpartsecom.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.carpartsecom.data.remote.api.GroqApiService
import com.example.carpartsecom.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Repository for AI Chat functionality using Groq API
 * Groq provides free, fast LLM inference
 * Supports text and image analysis (vision)
 */
class AiChatRepository {

    companion object {
        private const val TAG = "AiChatRepository"
        private const val GROQ_BASE_URL = "https://api.groq.com/"

        // Vision model for image analysis - using llava model which supports vision
        private const val VISION_MODEL = "llava-v1.5-7b-4096-preview"
        private const val TEXT_MODEL = "llama-3.1-8b-instant"

        // System prompt to make the AI a car assistant
        private const val SYSTEM_PROMPT = """You are AutoBot, a helpful virtual car assistant for a car parts e-commerce app. Your role is to:

1. DIAGNOSE car problems based on user descriptions (noises, warning lights, symptoms)
2. RECOMMEND products from our catalog when appropriate:
   - Brake Pads (for brake issues)
   - Oil Filter (for oil changes, maintenance)
   - Spark Plugs (for engine issues, misfires, poor fuel economy)
   - Car Battery (for starting issues, electrical problems)

3. PROVIDE maintenance advice and schedules
4. WARN users when they should see a mechanic for serious issues

Guidelines:
- Be friendly and helpful
- Use emojis occasionally for readability
- Keep responses concise but informative
- Always prioritize safety - recommend professional help for serious issues
- When recommending products, mention them by exact name so the app can show them
- Format important points with **bold** or bullet points
- If asked about something unrelated to cars, politely redirect to car topics

When you recommend a product, include the exact product name in your response:
- "Brake Pads" for brake issues
- "Oil Filter" for oil/maintenance
- "Spark Plug" for ignition/engine
- "Car Battery" for electrical/starting"""

        private const val VISION_SYSTEM_PROMPT = """You are AutoBot, an AI car assistant with vision capabilities. You can analyze images of:
- Dashboard warning lights
- Car parts (worn, damaged, or new)
- Engine components
- Tire conditions
- Any car-related images

When analyzing an image:
1. Identify what you see (warning light, part, damage, etc.)
2. Explain what it means
3. Recommend action or relevant products from our catalog:
   - Brake Pads, Oil Filter, Spark Plugs, Car Battery
4. Warn if professional help is needed

Be helpful, concise, and always prioritize safety."""
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(GROQ_BASE_URL)
        .client(okHttpClient)
        .build()

    private val groqService = retrofit.create(GroqApiService::class.java)

    // Conversation history for context
    private val conversationHistory = mutableListOf<Pair<String, String>>() // role, content

    /**
     * Analyze an image with optional text prompt
     */
    suspend fun analyzeImage(
        context: Context,
        imageUri: Uri,
        userPrompt: String = "What do you see in this image? If it's car-related, provide diagnosis and recommendations."
    ): Result<AiResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = Constants.GROQ_API_KEY

            // Check if API key is configured
            if (apiKey.isBlank() || apiKey == "REPLACE_ME" || !apiKey.startsWith("gsk_")) {
                Log.w(TAG, "Groq API key not configured, using fallback for image")
                return@withContext Result.success(AiResponse(
                    message = getImageFallbackResponse(),
                    productRecommendations = emptyList()
                ))
            }

            // Convert image to base64
            val base64Image = imageToBase64(context, imageUri)
            if (base64Image == null) {
                return@withContext Result.success(AiResponse(
                    message = "❌ Sorry, I couldn't process that image. Please try again with a different photo.",
                    productRecommendations = emptyList()
                ))
            }

            Log.d(TAG, "Image converted to base64, length: ${base64Image.length}")

            // Build messages array with image
            val messagesArray = JSONArray()

            // System message
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", VISION_SYSTEM_PROMPT)
            })

            // User message with image
            val contentArray = JSONArray().apply {
                // Text part
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", userPrompt)
                })
                // Image part
                put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", "data:image/jpeg;base64,$base64Image")
                    })
                })
            }

            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", contentArray)
            })

            // Request body
            val requestJson = JSONObject().apply {
                put("model", VISION_MODEL)
                put("messages", messagesArray)
                put("temperature", 0.7)
                put("max_tokens", 1000)
            }

            val mediaType = MediaType.parse("application/json")
            val requestBody = RequestBody.create(mediaType, requestJson.toString())

            Log.d(TAG, "Sending image to Groq Vision API")

            val response = groqService.chat(
                apiKey = "Bearer $apiKey",
                request = requestBody
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!.string()
                Log.d(TAG, "Groq Vision response received")

                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")

                if (choices.length() > 0) {
                    val assistantMessage = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    // Extract product recommendations from the response
                    val products = extractProductsFromResponse(assistantMessage)

                    return@withContext Result.success(AiResponse(
                        message = assistantMessage,
                        productRecommendations = products
                    ))
                }
            }

            // API failed - use smart fallback based on user prompt
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Log.e(TAG, "Groq Vision API failed: ${response.code()} $errorBody")

            // Try to provide helpful response based on the user's text prompt
            return@withContext Result.success(getSmartImageFallback(userPrompt))

        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing image", e)
            // Try to provide helpful response based on the user's text prompt
            return@withContext Result.success(getSmartImageFallback(userPrompt))
        }
    }

    /**
     * Smart fallback that analyzes the user's text prompt to provide relevant advice
     */
    private fun getSmartImageFallback(userPrompt: String): AiResponse {
        val promptLower = userPrompt.lowercase()

        // Detect tire-related keywords
        if (promptLower.contains("tire") || promptLower.contains("tyre") ||
            promptLower.contains("flat") || promptLower.contains("puncture") ||
            promptLower.contains("wheel")) {
            return AiResponse(
                message = """
                    🔍 **Tire Analysis**
                    
                    Based on your image of what appears to be a tire issue:
                    
                    **Common Tire Problems:**
                    • **Flat tire** - Loss of air pressure, needs repair or replacement
                    • **Worn tread** - If tread depth < 2/32", replace immediately
                    • **Bulge/bubble** - Internal damage, replace ASAP (dangerous!)
                    • **Uneven wear** - May indicate alignment or suspension issues
                    
                    **What to do:**
                    1. If flat: Use spare tire or call roadside assistance
                    2. Check tire pressure (should be 30-35 PSI typically)
                    3. Inspect for nails, screws, or punctures
                    4. Consider tire rotation every 5,000-7,500 miles
                    
                    ⚠️ **Safety Warning:** Don't drive on a flat or severely damaged tire!
                    
                    Would you like help finding replacement tires or repair services?
                """.trimIndent(),
                productRecommendations = listOf("Car Battery") // Recommend battery as tires aren't in catalog
            )
        }

        // Detect brake-related keywords
        if (promptLower.contains("brake") || promptLower.contains("pad") ||
            promptLower.contains("rotor") || promptLower.contains("caliper") ||
            promptLower.contains("squeak") || promptLower.contains("grind")) {
            return AiResponse(
                message = """
                    🔍 **Brake Analysis**
                    
                    Based on your brake-related image:
                    
                    **Signs of Worn Brake Pads:**
                    • Less than 3mm thickness remaining
                    • Visible wear indicator (metal tab)
                    • Grooves or scoring on the pad surface
                    • Uneven wear patterns
                    
                    **Signs of Worn Rotors:**
                    • Deep grooves or scoring
                    • Blue discoloration (overheating)
                    • Visible cracks
                    • Lip on outer edge
                    
                    **Recommendation:**
                    If pads are less than 3mm or show wear indicators, it's time to replace them!
                    
                    Our **Brake Pads** are high-quality and suitable for all weather conditions.
                    
                    ⚠️ **Safety:** Worn brakes significantly increase stopping distance!
                """.trimIndent(),
                productRecommendations = listOf("Brake Pads")
            )
        }

        // Detect engine-related keywords
        if (promptLower.contains("engine") || promptLower.contains("motor") ||
            promptLower.contains("spark") || promptLower.contains("plug") ||
            promptLower.contains("oil") || promptLower.contains("filter")) {
            return AiResponse(
                message = """
                    🔍 **Engine Component Analysis**
                    
                    Based on your engine-related image:
                    
                    **Common Issues:**
                    • **Spark Plugs**: Check for carbon buildup, worn electrodes
                    • **Oil Filter**: Replace every 3,000-7,500 miles
                    • **Air Filter**: Replace if dirty or clogged
                    
                    **Spark Plug Condition Guide:**
                    • Light tan/gray = Normal
                    • Black/sooty = Rich fuel mixture
                    • White/blistered = Lean mixture or overheating
                    • Oil-fouled = Possible engine wear
                    
                    **Recommendations:**
                    Check out our **Spark Plugs** and **Oil Filters** for quality replacements!
                """.trimIndent(),
                productRecommendations = listOf("Spark Plug", "Oil Filter")
            )
        }

        // Detect battery-related keywords
        if (promptLower.contains("battery") || promptLower.contains("terminal") ||
            promptLower.contains("corrosion") || promptLower.contains("start")) {
            return AiResponse(
                message = """
                    🔍 **Battery Analysis**
                    
                    Based on your battery-related image:
                    
                    **Signs of Battery Issues:**
                    • White/green corrosion on terminals
                    • Swollen or bulging case
                    • Cracks or damage
                    • Age > 3-5 years
                    
                    **What to Check:**
                    1. Clean corrosion with baking soda + water
                    2. Check terminal connections are tight
                    3. Test voltage (should be 12.6V when off, 13.7-14.7V running)
                    
                    Our **Car Battery** is long-lasting and reliable for all conditions!
                    
                    💡 **Tip:** Most auto stores will test your battery for free!
                """.trimIndent(),
                productRecommendations = listOf("Car Battery")
            )
        }

        // Generic fallback for any car image
        return AiResponse(
            message = """
                📷 **Image Received**
                
                I can see you've shared an image! While I'm having trouble analyzing it in detail right now, I'd be happy to help.
                
                **Please tell me more about what you're seeing:**
                • Is this a tire, brake, engine part, or something else?
                • What concerns do you have about it?
                • Are there any visible signs of damage or wear?
                
                **Common car issues I can help with:**
                🛞 Tires - flat, worn, punctured
                🛑 Brakes - squeaking, grinding, worn pads
                🔋 Battery - won't start, corrosion
                ⚙️ Engine - check engine light, misfires
                
                Just describe what you see and I'll provide detailed advice!
            """.trimIndent(),
            productRecommendations = listOf("Brake Pads", "Car Battery")
        )
    }

    /**
     * Convert image URI to base64 string
     */
    private fun imageToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap from URI")
                return null
            }

            // Resize if too large (max 1024px on longest side)
            val maxSize = 1024
            val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
                val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
                val newWidth = (bitmap.width * scale).toInt()
                val newHeight = (bitmap.height * scale).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            // Convert to JPEG base64
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to base64", e)
            null
        }
    }

    /**
     * Fallback response when image analysis is unavailable
     */
    private fun getImageFallbackResponse(): String {
        return """
            📷 **Image Analysis**
            
            I received your image! However, I'm currently unable to analyze it in detail.
            
            **Tips for better results:**
            • Make sure the image is clear and well-lit
            • Focus on the specific part or issue
            • Try describing what you see in text
            
            **In the meantime, you can:**
            • Describe your car issue in text
            • Browse our product categories
            • Contact support for assistance
            
            How can I help you today?
        """.trimIndent()
    }

    /**
     * Send a message to the AI and get a response
     */
    suspend fun chat(userMessage: String): Result<AiResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = Constants.GROQ_API_KEY

            // Check if API key is configured
            if (apiKey.isBlank() || apiKey == "REPLACE_ME" || !apiKey.startsWith("gsk_")) {
                Log.w(TAG, "Groq API key not configured, using fallback")
                return@withContext Result.success(AiResponse(
                    message = getFallbackResponse(userMessage),
                    productRecommendations = extractProductRecommendations(userMessage)
                ))
            }

            // Add user message to history
            conversationHistory.add("user" to userMessage)

            // Keep last 10 messages for context
            if (conversationHistory.size > 10) {
                conversationHistory.removeAt(0)
            }

            // Build messages array
            val messagesArray = JSONArray()

            // System message
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", SYSTEM_PROMPT)
            })

            // Conversation history
            conversationHistory.forEach { (role, content) ->
                messagesArray.put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }

            // Request body
            val requestJson = JSONObject().apply {
                put("model", TEXT_MODEL) // Fast, free model
                put("messages", messagesArray)
                put("temperature", 0.7)
                put("max_tokens", 500)
            }

            val mediaType = MediaType.parse("application/json")
            val requestBody = RequestBody.create(mediaType, requestJson.toString())

            Log.d(TAG, "Sending request to Groq API")

            val response = groqService.chat(
                apiKey = "Bearer $apiKey",
                request = requestBody
            )

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!.string()
                Log.d(TAG, "Groq response: $responseBody")

                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.getJSONArray("choices")

                if (choices.length() > 0) {
                    val assistantMessage = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")

                    // Add assistant response to history
                    conversationHistory.add("assistant" to assistantMessage)

                    // Extract product recommendations from the response
                    val products = extractProductsFromResponse(assistantMessage)

                    return@withContext Result.success(AiResponse(
                        message = assistantMessage,
                        productRecommendations = products
                    ))
                }
            }

            // API failed, use fallback
            Log.w(TAG, "Groq API failed: ${response.code()} ${response.errorBody()?.string()}")
            return@withContext Result.success(AiResponse(
                message = getFallbackResponse(userMessage),
                productRecommendations = extractProductRecommendations(userMessage)
            ))

        } catch (e: Exception) {
            Log.e(TAG, "Error calling Groq API", e)
            // Return fallback response instead of failure
            return@withContext Result.success(AiResponse(
                message = getFallbackResponse(userMessage),
                productRecommendations = extractProductRecommendations(userMessage)
            ))
        }
    }

    /**
     * Extract product names from AI response
     */
    private fun extractProductsFromResponse(response: String): List<String> {
        val products = mutableListOf<String>()
        val responseLower = response.lowercase()

        if (responseLower.contains("brake pad")) products.add("Brake Pads")
        if (responseLower.contains("oil filter")) products.add("Oil Filter")
        if (responseLower.contains("spark plug")) products.add("Spark Plug")
        if (responseLower.contains("car battery") || responseLower.contains("battery")) products.add("Car Battery")

        return products.distinct()
    }

    /**
     * Extract product recommendations based on user message keywords
     */
    private fun extractProductRecommendations(message: String): List<String> {
        val products = mutableListOf<String>()
        val messageLower = message.lowercase()

        if (messageLower.contains("brake") || messageLower.contains("stop") || messageLower.contains("squeak")) {
            products.add("Brake Pads")
        }
        if (messageLower.contains("oil") || messageLower.contains("filter")) {
            products.add("Oil Filter")
        }
        if (messageLower.contains("spark") || messageLower.contains("ignition") || messageLower.contains("misfire")) {
            products.add("Spark Plug")
        }
        if (messageLower.contains("battery") || messageLower.contains("start") || messageLower.contains("electrical")) {
            products.add("Car Battery")
        }

        return products.distinct()
    }

    /**
     * Fallback response when API is unavailable
     */
    private fun getFallbackResponse(userMessage: String): String {
        val message = userMessage.lowercase()

        return when {
            message.contains("hello") || message.contains("hi") || message.contains("hey") -> {
                """
                    👋 Hello! I'm AutoBot, your virtual car assistant!
                    
                    I can help you with:
                    🔧 Finding the right car parts
                    🔍 Diagnosing car problems
                    💡 Maintenance advice
                    
                    What can I help you with today?
                """.trimIndent()
            }
            message.contains("brake") -> {
                """
                    🛑 **Brake Issues**
                    
                    Based on your message about brakes:
                    
                    **Common brake problems:**
                    • Squeaking = Worn pads (time to replace)
                    • Grinding = Pads gone, damaging rotors (urgent!)
                    • Soft pedal = Air in lines or fluid issue
                    • Vibration = Warped rotors
                    
                    I recommend checking our **Brake Pads** - they're high quality and all-weather!
                    
                    ⚠️ If you have grinding or very soft brakes, please see a mechanic ASAP for safety.
                """.trimIndent()
            }
            message.contains("engine") || message.contains("check engine") -> {
                """
                    💡 **Engine Issues**
                    
                    A check engine light can mean many things:
                    
                    **Common causes:**
                    • Loose gas cap (easy fix!)
                    • Oxygen sensor
                    • Catalytic converter
                    • Spark plugs (very common)
                    • Mass airflow sensor
                    
                    Many auto stores will read the code for free!
                    
                    Our **Spark Plugs** are often the fix - they're iridium and last up to 100k miles.
                """.trimIndent()
            }
            message.contains("battery") || message.contains("start") || message.contains("won't start") -> {
                """
                    🔋 **Battery/Starting Issues**
                    
                    If your car won't start:
                    
                    **Quick diagnosis:**
                    • No sound = Dead battery or connections
                    • Clicking = Weak battery
                    • Cranks but won't start = Fuel or spark issue
                    
                    **Try this:**
                    1. Check battery terminals for corrosion
                    2. Try a jump start
                    3. If that works, battery likely needs replacing
                    
                    Our **Car Battery** is long-lasting and reliable!
                """.trimIndent()
            }
            message.contains("oil") -> {
                """
                    🛢️ **Oil Change**
                    
                    Regular oil changes are essential!
                    
                    **Change intervals:**
                    • Conventional: 3,000-5,000 miles
                    • Synthetic: 7,500-10,000 miles
                    
                    **Always replace the oil filter too!**
                    
                    Our **Oil Filter** provides excellent engine protection.
                """.trimIndent()
            }
            message.contains("noise") || message.contains("sound") -> {
                """
                    🔊 **Car Noises**
                    
                    Car noises can indicate various issues:
                    
                    **Common noises:**
                    • Squeaking when braking = Worn brake pads
                    • Knocking from engine = Low oil or serious issue
                    • Clicking when turning = CV joint
                    • Humming while driving = Wheel bearing or tires
                    
                    Can you describe the noise more? When does it happen?
                """.trimIndent()
            }
            else -> {
                """
                    🚗 I'm here to help with car issues!
                    
                    **Tell me about:**
                    • Any warning lights
                    • Strange noises or smells
                    • Starting problems
                    • Maintenance questions
                    
                    **Or ask about parts:**
                    • Brake pads
                    • Oil filters
                    • Spark plugs
                    • Batteries
                    
                    What's going on with your car?
                """.trimIndent()
            }
        }
    }

    /**
     * Clear conversation history
     */
    fun clearHistory() {
        conversationHistory.clear()
    }

    data class AiResponse(
        val message: String,
        val productRecommendations: List<String> = emptyList()
    )
}

