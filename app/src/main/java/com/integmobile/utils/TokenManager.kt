package com.integmobile.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages JWT token storage using encrypted SharedPreferences
 * Provides secure token persistence for session management
 */
class TokenManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        Constants.PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        sharedPreferences.edit().putString(Constants.KEY_AUTH_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString(Constants.KEY_AUTH_TOKEN, null)
    }

    fun saveRefreshToken(refreshToken: String) {
        sharedPreferences.edit().putString(Constants.KEY_REFRESH_TOKEN, refreshToken).apply()
    }

    fun getRefreshToken(): String? {
        return sharedPreferences.getString(Constants.KEY_REFRESH_TOKEN, null)
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString(Constants.KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null)
    }

    fun saveUserEmail(email: String) {
        sharedPreferences.edit().putString(Constants.KEY_USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null)
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false) && 
               !getToken().isNullOrEmpty()
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
