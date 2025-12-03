package com.integmobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.integmobile.CarPartsApplication
import com.integmobile.R
import com.integmobile.data.api.RetrofitClient
import com.integmobile.data.db.AppDatabase
import com.integmobile.data.repository.AuthRepository
import com.integmobile.databinding.ActivityLoginBinding
import com.integmobile.ui.MainActivity
import com.integmobile.ui.auth.viewmodel.AuthViewModel
import com.integmobile.ui.auth.viewmodel.AuthViewModelFactory
import com.integmobile.utils.Constants
import com.integmobile.utils.Result
import com.integmobile.utils.showToast

/**
 * Login Activity with email/password and Google Sign-In
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var googleSignInClient: GoogleSignInClient
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewModel()
        setupGoogleSignIn()
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
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Constants.GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }
    
    private fun setupObservers() {
        // Login state
        viewModel.loginState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    showToast("Login successful!")
                    navigateToMain()
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "Login failed")
                }
            }
        }
        
        // Google Sign-In state
        viewModel.googleSignInState.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    showToast("Google Sign-In successful!")
                    navigateToMain()
                }
                is Result.Error -> {
                    showLoading(false)
                    showToast(result.exception.message ?: "Google Sign-In failed")
                }
            }
        }
        
        // Validation errors
        viewModel.validationError.observe(this) { error ->
            showToast(error)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }
        
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }
    
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            
            if (idToken != null) {
                viewModel.googleSignIn(idToken)
            } else {
                showToast("Failed to get Google ID token")
            }
        } catch (e: ApiException) {
            showToast("Google Sign-In failed: ${e.message}")
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoogleSignIn.isEnabled = !isLoading
    }
    
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
