package com.example.carpartsecom.util

object Constants {
    // CHOOSE ONE OPTION BASED ON YOUR SETUP:
    
    // Option 1: Android Emulator (uses 10.0.2.2 to access host's localhost)
    // const val BASE_URL = "http://10.0.2.2:8080/"
    
    // Option 2: Physical Device on same WiFi network
    // Replace with your computer's local IP (run 'ipconfig' in cmd to find it)
    // Example: "http://192.168.1.100:8080/"
    const val BASE_URL = "http://10.0.2.2:8080/"  // Must end with /
    
    // SharedPreferences
    const val PREFS_NAME = "carparts_prefs"
    const val KEY_TOKEN = "auth_token"
    
    // Payment
    const val STRIPE_PUBLISHABLE_KEY = "pk_test_51RQBuU2SYDrVwQ9NL7K6rNw1PNVBXzhdMLHs1ijzvoebfIngP7720le7VQEq8VvSxihpgFe4PJDuHjXCdbXs3Tpg00E5kUpK2q"

    // AI Chatbot - Groq API (Free tier: 30 req/min, 6000 req/day)
    // Get your free API key at: https://console.groq.com/keys
    // Leave empty or "REPLACE_ME" to use local fallback assistant
    const val GROQ_API_KEY = ""  // TODO: Add your Groq API key here
}
