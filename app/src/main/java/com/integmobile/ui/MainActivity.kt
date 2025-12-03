package com.integmobile.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.integmobile.CarPartsApplication
import com.integmobile.R
import com.integmobile.databinding.ActivityMainBinding
import com.integmobile.ui.auth.LoginActivity

/**
 * Main Activity with BottomNavigationView
 * Container for Products, Cart, Orders, and Profile fragments
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if user is logged in
        val tokenManager = (application as CarPartsApplication).tokenManager
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }
        
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigationView.setupWithNavController(navController)
    }
    
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
