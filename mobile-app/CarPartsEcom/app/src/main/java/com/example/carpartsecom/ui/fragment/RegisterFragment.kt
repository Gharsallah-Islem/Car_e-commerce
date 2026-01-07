package com.example.carpartsecom.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.carpartsecom.MainActivity
import com.example.carpartsecom.R
import com.example.carpartsecom.ui.viewmodel.AuthViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = (requireActivity() as MainActivity).viewModelFactory
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]
        
        val firstNameLayout = view.findViewById<TextInputLayout>(R.id.firstNameLayout)
        val firstNameEdit = view.findViewById<TextInputEditText>(R.id.firstNameEdit)
        val lastNameLayout = view.findViewById<TextInputLayout>(R.id.lastNameLayout)
        val lastNameEdit = view.findViewById<TextInputEditText>(R.id.lastNameEdit)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailLayout)
        val emailEdit = view.findViewById<TextInputEditText>(R.id.emailEdit)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.passwordLayout)
        val passwordEdit = view.findViewById<TextInputEditText>(R.id.passwordEdit)
        val confirmPasswordLayout = view.findViewById<TextInputLayout>(R.id.confirmPasswordLayout)
        val confirmPasswordEdit = view.findViewById<TextInputEditText>(R.id.confirmPasswordEdit)
        val registerButton = view.findViewById<MaterialButton>(R.id.registerButton)
        
        registerButton.setOnClickListener {
            val firstName = firstNameEdit.text.toString().trim()
            val lastName = lastNameEdit.text.toString().trim()
            val email = emailEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()
            val confirmPassword = confirmPasswordEdit.text.toString().trim()
            
            // Validate inputs
            var isValid = true
            
            if (firstName.isEmpty()) {
                firstNameLayout?.error = "First name is required"
                isValid = false
            } else {
                firstNameLayout?.error = null
            }
            
            if (lastName.isEmpty()) {
                lastNameLayout?.error = "Last name is required"
                isValid = false
            } else {
                lastNameLayout?.error = null
            }
            
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
            
            if (confirmPassword.isEmpty()) {
                confirmPasswordLayout?.error = "Please confirm your password"
                isValid = false
            } else if (password != confirmPassword) {
                confirmPasswordLayout?.error = "Passwords do not match"
                isValid = false
            } else {
                confirmPasswordLayout?.error = null
            }
            
            if (!isValid) return@setOnClickListener
            
            registerButton.isEnabled = false
            registerButton.text = "Creating account..."
            authViewModel.register(email, password, firstName, lastName)
        }
        
        authViewModel.registerStatus.observe(viewLifecycleOwner) { result ->
            registerButton.isEnabled = true
            registerButton.text = "Create Account"
            
            result.onSuccess { _ ->
                showSnackbar("Account created! Check your email for OTP.")
                val otpFragment = OTPVerificationFragment().apply {
                    arguments = Bundle().apply {
                        putString("email", emailEdit.text.toString().trim())
                    }
                }
                (requireActivity() as MainActivity).navigateToFragment(otpFragment)
            }
            result.onFailure { error ->
                showSnackbar("Registration failed: ${error.message}")
            }
        }
    }
    
    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
