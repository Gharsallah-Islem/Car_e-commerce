package com.integmobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.integmobile.CarPartsApplication
import com.integmobile.databinding.ActivitySplashBinding
import com.integmobile.ui.auth.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen with session check
 * Redirects to Login or Main based on authentication status
 */
class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check authentication status after delay
        lifecycleScope.launch {
            delay(2000) // 2 second splash
            checkAuthenticationStatus()
        }
    }
    
    private fun checkAuthenticationStatus() {
        val tokenManager = (application as CarPartsApplication).tokenManager
        
        if (tokenManager.isLoggedIn()) {
            // User is logged in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // User not logged in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
