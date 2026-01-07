package com.example.carpartsecom.util

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    fun saveToken(token: String) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(Constants.KEY_TOKEN, null)
    }
    
    fun clearToken() {
        prefs.edit().remove(Constants.KEY_TOKEN).apply()
    }
    
    fun hasToken(): Boolean {
        return getToken() != null
    }
}
