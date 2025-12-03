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
import com.integmobile.databinding.ActivityResetPasswordBinding
import com.integmobile.ui.auth.viewmodel.AuthViewModel
import com.integmobile.ui.auth.viewmodel.AuthViewModelFactory
import com.integmobile.utils.Result
import com.integmobile.utils.showToast

/**
 * Reset Password Activity with OTP flow
 */
class ResetPasswordActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var viewModel: AuthViewModel
    private var currentStep = 1 // 1: Email, 2: OTP, 3: New Password
    private var email = ""
    private var otp = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupObservers()
        setupClickListeners()
        showStep(1)
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
        // Password reset request
        viewModel.passwordResetRequestState.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    showToast("OTP sent to your email")
                    showStep(2)
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "Failed to send OTP")
                }
            }
        }
        
        // OTP verification
        viewModel.otpVerificationState.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    if (result.data) {
                        showToast("OTP verified")
                        showStep(3)
                    } else {
                        showToast("Invalid OTP")
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "OTP verification failed")
                }
            }
        }
        
        // Password reset
        viewModel.passwordResetState.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    showToast("Password reset successful!")
                    finish()
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "Password reset failed")
                }
            }
        }
        
        viewModel.validationError.observe(this) { error ->
            showToast(error)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnNext.setOnClickListener {
            when (currentStep) {
                1 -> {
                    email = binding.etEmail.text.toString().trim()
                    viewModel.requestPasswordReset(email)
                }
                2 -> {
                    otp = binding.etOtp.text.toString().trim()
                    viewModel.verifyOTP(email, otp)
                }
                3 -> {
                    val newPassword = binding.etNewPassword.text.toString()
                    val confirmPassword = binding.etConfirmPassword.text.toString()
                    
                    if (newPassword != confirmPassword) {
                        showToast("Passwords do not match")
                        return@setOnClickListener
                    }
                    
                    viewModel.resetPassword(email, otp, newPassword)
                }
            }
        }
        
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
    
    private fun showStep(step: Int) {
        currentStep = step
        
        when (step) {
            1 -> {
                binding.tvTitle.text = "Reset Password"
                binding.tvSubtitle.text = "Enter your email address"
                binding.tilEmail.visibility = View.VISIBLE
                binding.tilOtp.visibility = View.GONE
                binding.tilNewPassword.visibility = View.GONE
                binding.tilConfirmPassword.visibility = View.GONE
                binding.btnNext.text = "Send OTP"
            }
            2 -> {
                binding.tvTitle.text = "Verify OTP"
                binding.tvSubtitle.text = "Enter the OTP sent to $email"
                binding.tilEmail.visibility = View.GONE
                binding.tilOtp.visibility = View.VISIBLE
                binding.tilNewPassword.visibility = View.GONE
                binding.tilConfirmPassword.visibility = View.GONE
                binding.btnNext.text = "Verify"
            }
            3 -> {
                binding.tvTitle.text = "New Password"
                binding.tvSubtitle.text = "Enter your new password"
                binding.tilEmail.visibility = View.GONE
                binding.tilOtp.visibility = View.GONE
                binding.tilNewPassword.visibility = View.VISIBLE
                binding.tilConfirmPassword.visibility = View.VISIBLE
                binding.btnNext.text = "Reset Password"
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnNext.isEnabled = !isLoading
    }
}
