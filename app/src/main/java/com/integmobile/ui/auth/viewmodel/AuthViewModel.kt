package com.integmobile.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.integmobile.data.model.response.UserData
import com.integmobile.data.repository.AuthRepository
import com.integmobile.utils.Result
import com.integmobile.utils.Validators
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication operations
 * Manages authentication state and delegates data operations to AuthRepository
 * Uses ViewModelScope for coroutines
 */
class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {
    
    // Login state
    private val _loginState = MutableLiveData<Result<UserData>>()
    val loginState: LiveData<Result<UserData>> = _loginState
    
    // Register state
    private val _registerState = MutableLiveData<Result<UserData>>()
    val registerState: LiveData<Result<UserData>> = _registerState
    
    // Email verification state
    private val _verifyEmailState = MutableLiveData<Result<Boolean>>()
    val verifyEmailState: LiveData<Result<Boolean>> = _verifyEmailState
    
    // Password reset request state
    private val _passwordResetRequestState = MutableLiveData<Result<Boolean>>()
    val passwordResetRequestState: LiveData<Result<Boolean>> = _passwordResetRequestState
    
    // OTP verification state
    private val _otpVerificationState = MutableLiveData<Result<Boolean>>()
    val otpVerificationState: LiveData<Result<Boolean>> = _otpVerificationState
    
    // Password reset state
    private val _passwordResetState = MutableLiveData<Result<Boolean>>()
    val passwordResetState: LiveData<Result<Boolean>> = _passwordResetState
    
    // Google Sign-In state
    private val _googleSignInState = MutableLiveData<Result<UserData>>()
    val googleSignInState: LiveData<Result<UserData>> = _googleSignInState
    
    // Validation errors
    private val _validationError = MutableLiveData<String>()
    val validationError: LiveData<String> = _validationError
    
    /**
     * Login with email and password
     */
    fun login(email: String, password: String) {
        // Validate input
        if (!Validators.isValidEmail(email)) {
            _validationError.value = "Invalid email address"
            return
        }
        
        if (password.isEmpty()) {
            _validationError.value = "Password is required"
            return
        }
        
        viewModelScope.launch {
            _loginState.value = Result.Loading
            val result = repository.login(email, password)
            _loginState.value = result
        }
    }
    
    /**
     * Register new user
     */
    fun register(email: String, password: String, fullName: String, phoneNumber: String?) {
        // Validate input
        if (!Validators.isValidEmail(email)) {
            _validationError.value = "Invalid email address"
            return
        }
        
        if (!Validators.isValidPassword(password)) {
            _validationError.value = Validators.getPasswordStrengthMessage(password)
            return
        }
        
        if (!Validators.isValidName(fullName)) {
            _validationError.value = "Invalid name"
            return
        }
        
        if (phoneNumber != null && !Validators.isValidPhoneNumber(phoneNumber)) {
            _validationError.value = "Invalid phone number"
            return
        }
        
        viewModelScope.launch {
            _registerState.value = Result.Loading
            val result = repository.register(email, password, fullName, phoneNumber)
            _registerState.value = result
        }
    }
    
    /**
     * Verify email with OTP
     */
    fun verifyEmail(email: String, otp: String) {
        if (!Validators.isValidOTP(otp)) {
            _validationError.value = "Invalid OTP code"
            return
        }
        
        viewModelScope.launch {
            _verifyEmailState.value = Result.Loading
            val result = repository.verifyEmail(email, otp)
            _verifyEmailState.value = result
        }
    }
    
    /**
     * Request password reset (sends OTP)
     */
    fun requestPasswordReset(email: String) {
        if (!Validators.isValidEmail(email)) {
            _validationError.value = "Invalid email address"
            return
        }
        
        viewModelScope.launch {
            _passwordResetRequestState.value = Result.Loading
            val result = repository.requestPasswordReset(email)
            _passwordResetRequestState.value = result
        }
    }
    
    /**
     * Verify OTP for password reset
     */
    fun verifyOTP(email: String, otp: String) {
        if (!Validators.isValidOTP(otp)) {
            _validationError.value = "Invalid OTP code"
            return
        }
        
        viewModelScope.launch {
            _otpVerificationState.value = Result.Loading
            val result = repository.verifyOTP(email, otp)
            _otpVerificationState.value = result
        }
    }
    
    /**
     * Reset password with OTP
     */
    fun resetPassword(email: String, otp: String, newPassword: String) {
        if (!Validators.isValidPassword(newPassword)) {
            _validationError.value = Validators.getPasswordStrengthMessage(newPassword)
            return
        }
        
        viewModelScope.launch {
            _passwordResetState.value = Result.Loading
            val result = repository.resetPassword(email, otp, newPassword)
            _passwordResetState.value = result
        }
    }
    
    /**
     * Google Sign-In
     */
    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _googleSignInState.value = Result.Loading
            val result = repository.googleSignIn(idToken)
            _googleSignInState.value = result
        }
    }
    
    /**
     * Logout
     */
    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return repository.isLoggedIn()
    }
}
