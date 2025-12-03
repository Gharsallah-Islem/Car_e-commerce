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
import com.integmobile.databinding.ActivitySignUpBinding
import com.integmobile.ui.auth.viewmodel.AuthViewModel
import com.integmobile.ui.auth.viewmodel.AuthViewModelFactory
import com.integmobile.utils.Result
import com.integmobile.utils.showToast

/**
 * Sign Up Activity with email verification flow
 */
class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: AuthViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
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
        // Register state
        viewModel.registerState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    showToast("Registration successful! Please verify your email.")
                    // Navigate to email verification
                    val intent = Intent(this, VerifyEmailActivity::class.java)
                    intent.putExtra("email", binding.etEmail.text.toString())
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "Registration failed")
                }
            }
        }
        
        // Validation errors
        viewModel.validationError.observe(this) { error ->
            showToast(error)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val fullName = binding.etFullName.text.toString().trim()
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            
            // Check if passwords match
            if (password != confirmPassword) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }
            
            viewModel.register(
                email = email,
                password = password,
                fullName = fullName,
                phoneNumber = phoneNumber.ifEmpty { null }
            )
        }
        
        binding.tvLogin.setOnClickListener {
            finish()
        }
        
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSignUp.isEnabled = !isLoading
    }
}
