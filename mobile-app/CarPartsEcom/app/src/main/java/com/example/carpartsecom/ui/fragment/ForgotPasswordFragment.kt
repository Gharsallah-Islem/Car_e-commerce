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

class ForgotPasswordFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private var email: String = ""
    private var isResetMode: Boolean = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val factory = (requireActivity() as MainActivity).viewModelFactory
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]
        
        val emailLayout = view.findViewById<TextInputLayout>(R.id.emailLayout)
        val emailEdit = view.findViewById<TextInputEditText>(R.id.emailEdit)
        val codeLayout = view.findViewById<TextInputLayout>(R.id.codeLayout)
        val codeEdit = view.findViewById<TextInputEditText>(R.id.codeEdit)
        val newPasswordLayout = view.findViewById<TextInputLayout>(R.id.newPasswordLayout)
        val newPasswordEdit = view.findViewById<TextInputEditText>(R.id.newPasswordEdit)
        val submitButton = view.findViewById<MaterialButton>(R.id.submitButton)
        val backButton = view.findViewById<MaterialButton>(R.id.backButton)
        
        // Initially hide reset fields
        codeLayout.visibility = View.GONE
        newPasswordLayout.visibility = View.GONE
        
        submitButton.setOnClickListener {
            if (!isResetMode) {
                // Request reset code
                email = emailEdit.text.toString().trim()
                
                if (email.isEmpty()) {
                    emailLayout?.error = "Email is required"
                    return@setOnClickListener
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    emailLayout?.error = "Invalid email format"
                    return@setOnClickListener
                } else {
                    emailLayout?.error = null
                }
                
                submitButton.isEnabled = false
                submitButton.text = "Sending..."
                authViewModel.forgotPassword(email)
            } else {
                // Reset password with code
                val code = codeEdit.text.toString().trim()
                val newPassword = newPasswordEdit.text.toString().trim()
                
                var isValid = true

                // OTP code validation
                if (code.isEmpty()) {
                    codeLayout?.error = "Code is required"
                    isValid = false
                } else if (!code.matches(Regex("^[0-9]+$"))) {
                    codeLayout?.error = "Code must contain only digits"
                    isValid = false
                } else if (code.length != 6) {
                    codeLayout?.error = "Code must be 6 digits"
                    isValid = false
                } else {
                    codeLayout?.error = null
                }
                
                // Password validation
                if (newPassword.isEmpty()) {
                    newPasswordLayout?.error = "New password is required"
                    isValid = false
                } else if (newPassword.length < 6) {
                    newPasswordLayout?.error = "Password must be at least 6 characters"
                    isValid = false
                } else if (!newPassword.matches(Regex(".*[A-Za-z].*"))) {
                    newPasswordLayout?.error = "Password must contain at least one letter"
                    isValid = false
                } else if (!newPassword.matches(Regex(".*[0-9].*"))) {
                    newPasswordLayout?.error = "Password must contain at least one number"
                    isValid = false
                } else {
                    newPasswordLayout?.error = null
                }
                
                if (!isValid) return@setOnClickListener
                
                submitButton.isEnabled = false
                submitButton.text = "Resetting..."
                authViewModel.resetPassword(email, code, newPassword)
            }
        }
        
        backButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToFragment(LoginFragment(), addToBackStack = false)
        }
        
        authViewModel.forgotPasswordStatus.observe(viewLifecycleOwner) { result ->
            submitButton.isEnabled = true
            
            result.onSuccess {
                showSnackbar("Reset code sent to your email!")
                isResetMode = true
                emailLayout.visibility = View.GONE
                codeLayout.visibility = View.VISIBLE
                newPasswordLayout.visibility = View.VISIBLE
                submitButton.text = "Reset Password"
            }
            result.onFailure { error ->
                submitButton.text = "Send Reset Code"
                showSnackbar("Failed: ${error.message}")
            }
        }
        
        authViewModel.resetPasswordStatus.observe(viewLifecycleOwner) { result ->
            submitButton.isEnabled = true
            submitButton.text = "Reset Password"
            
            result.onSuccess {
                showSnackbar("Password reset successful! Please login.")
                (requireActivity() as MainActivity).navigateToFragment(LoginFragment(), addToBackStack = false)
            }
            result.onFailure { error ->
                showSnackbar("Failed: ${error.message}")
            }
        }
    }
    
    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
