package com.integmobile

import android.app.Application
import com.integmobile.utils.TokenManager

/**
 * Application class for global initialization
 */
class CarPartsApplication : Application() {

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize TokenManager
        tokenManager = TokenManager.getInstance(this)
    }

    companion object {
        lateinit var instance: CarPartsApplication
            private set
    }
}
