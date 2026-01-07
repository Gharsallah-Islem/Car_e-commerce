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

class OTPVerificationFragment : Fragment() {
    private lateinit var authViewModel: AuthViewModel
    private var email: String = ""
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_otp_verification, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        email = arguments?.getString("email") ?: ""
        
        val factory = (requireActivity() as MainActivity).viewModelFactory
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]
        
        val codeLayout = view.findViewById<TextInputLayout>(R.id.codeLayout)
        val codeEdit = view.findViewById<TextInputEditText>(R.id.codeEdit)
        val verifyButton = view.findViewById<MaterialButton>(R.id.verifyButton)
        
        verifyButton.setOnClickListener {
            val code = codeEdit.text.toString().trim()
            
            if (code.isEmpty()) {
                codeLayout?.error = "Please enter the OTP code"
                return@setOnClickListener
            } else if (!code.matches(Regex("^[0-9]+$"))) {
                codeLayout?.error = "OTP code must contain only digits"
                return@setOnClickListener
            } else if (code.length != 6) {
                codeLayout?.error = "OTP code must be 6 digits"
                return@setOnClickListener
            } else {
                codeLayout?.error = null
            }
            
            verifyButton.isEnabled = false
            verifyButton.text = "Verifying..."
            authViewModel.verifyEmail(email, code)
        }
        
        authViewModel.verifyStatus.observe(viewLifecycleOwner) { result ->
            verifyButton.isEnabled = true
            verifyButton.text = "Verify"
            
            result.onSuccess {
                showSnackbar("Email verified successfully! Please login.")
                (requireActivity() as MainActivity).navigateToFragment(LoginFragment(), addToBackStack = false)
            }
            result.onFailure { error ->
                showSnackbar("Verification failed: ${error.message}")
            }
        }
    }
    
    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
        }
    }
}
