package com.example.carpartsecom.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.viewmodel.AuthViewModel
import com.example.carpartsecom.util.GoogleSignInHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private lateinit var googleSignInButton: MaterialButton

    // Activity result launcher for Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        googleSignInButton.isEnabled = true
        googleSignInButton.text = "Continue with Google"

        if (result.resultCode == Activity.RESULT_OK) {
            val signInResult = googleSignInHelper.handleSignInResult(result.data)

            signInResult.onSuccess { googleResult ->
                // Send the ID token to backend for verification
                googleSignInButton.text = "Authenticating..."
                googleSignInButton.isEnabled = false
                authViewModel.googleSignIn(googleResult.idToken)
            }

            signInResult.onFailure { error ->
                showSnackbar(error.message ?: "Google Sign-In failed")
            }
        } else {
            showSnackbar("Google Sign-In cancelled")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = (requireActivity() as MainActivity).viewModelFactory
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]
        googleSignInHelper = GoogleSignInHelper(requireContext())

        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailLayout)
        val emailEdit = view.findViewById<TextInputEditText>(R.id.emailEdit)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordLayout)
        val passwordEdit = view.findViewById<TextInputEditText>(R.id.passwordEdit)
        val loginButton = view.findViewById<MaterialButton>(R.id.loginButton)
        val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)
        val forgotPasswordButton = view.findViewById<MaterialButton>(R.id.forgotPasswordButton)
        googleSignInButton = view.findViewById(R.id.googleSignInButton)

        loginButton.setOnClickListener {
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) {
                emailLayout?.error = "Email is required"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout?.error = "Invalid email format"
                isValid = false
            } else {
                emailLayout?.error = null
            }

            if (password.isEmpty()) {
                passwordLayout?.error = "Password is required"
                isValid = false
            } else if (password.length < 6) {
                passwordLayout?.error = "Password must be at least 6 characters"
                isValid = false
            } else {
                passwordLayout?.error = null
            }

            if (!isValid) return@setOnClickListener

            loginButton.isEnabled = false
            loginButton.text = "Signing in..."
            authViewModel.login(email, password)
        }

        registerButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(RegisterFragment())
        }

        forgotPasswordButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(ForgotPasswordFragment())
        }

        googleSignInButton.setOnClickListener {
            googleSignInButton.isEnabled = false
            googleSignInButton.text = "Signing in..."

            val signInIntent = googleSignInHelper.getSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        }

        authViewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            loginButton.isEnabled = true
            loginButton.text = "Sign In"

            result.onSuccess { _ ->
                showSnackbar("Welcome back!")
                (requireActivity() as MainActivity).showMainApp()
            }
            result.onFailure { error ->
                showSnackbar("Login failed: ${error.message}")
            }
        }

        // Observe Google Sign-In status
        authViewModel.googleSignInStatus.observe(viewLifecycleOwner) { result ->
            googleSignInButton.isEnabled = true
            googleSignInButton.text = "Continue with Google"

            result.onSuccess { _ ->
                showSnackbar("Welcome!")
                (requireActivity() as MainActivity).showMainApp()
            }
            result.onFailure { error ->
                showSnackbar("Google Sign-In failed: ${error.message}")
            }
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }
}

