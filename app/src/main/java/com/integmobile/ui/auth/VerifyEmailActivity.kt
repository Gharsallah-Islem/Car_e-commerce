package com.integmobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.integmobile.CarPartsApplication
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.AuthRepository
import com.integmobile.databinding.ActivityVerifyEmailBinding
import com.integmobile.ui.MainActivity
import com.integmobile.ui.auth.viewmodel.AuthViewModel
import com.integmobile.ui.auth.viewmodel.AuthViewModelFactory
import com.integmobile.utils.Result
import com.integmobile.utils.showToast

/**
 * Email verification activity with OTP input
 */
class VerifyEmailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVerifyEmailBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var email: String
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        email = intent.getStringExtra("email") ?: ""
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
        
        binding.tvEmail.text = "We sent a verification code to $email"
    }
    
    private fun setupViewModel() {
        val database = AppDatabase.getInstance(this)
        val tokenManager = (application as CarPartsApplication).tokenManager
        val repository = AuthRepository(
            RetrofitClient.authService,
            database.userDao(),
            tokenManager
        )
        val factory = AuthViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }
    
    private fun setupObservers() {
        viewModel.verifyEmailState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    if (result.data) {
                        showToast("Email verified successfully!")
                        navigateToMain()
                    } else {
                        showToast("Verification failed")
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "Verification failed")
                }
            }
        }
        
        viewModel.validationError.observe(this) { error ->
            showToast(error)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            viewModel.verifyEmail(email, otp)
        }
        
        binding.tvResendOtp.setOnClickListener {
            // Resend OTP logic (could reuse register endpoint)
            showToast("OTP resent to $email")
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnVerify.isEnabled = !isLoading
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
