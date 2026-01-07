package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.local.entities.UserEntity
import com.example.carpartsecom.data.remote.dto.LoginResponse
import com.example.carpartsecom.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    val authenticatedUser: LiveData<UserEntity?> = authRepository.getCurrentUser()
    
    private val _registerStatus = MutableLiveData<Result<String>>()
    val registerStatus: LiveData<Result<String>> = _registerStatus
    
    private val _verifyStatus = MutableLiveData<Result<String>>()
    val verifyStatus: LiveData<Result<String>> = _verifyStatus
    
    private val _loginStatus = MutableLiveData<Result<LoginResponse>>()
    val loginStatus: LiveData<Result<LoginResponse>> = _loginStatus
    
    private val _logoutStatus = MutableLiveData<Result<Unit>>()
    val logoutStatus: LiveData<Result<Unit>> = _logoutStatus
    
    private val _forgotPasswordStatus = MutableLiveData<Result<String>>()
    val forgotPasswordStatus: LiveData<Result<String>> = _forgotPasswordStatus
    
    private val _resetPasswordStatus = MutableLiveData<Result<String>>()
    val resetPasswordStatus: LiveData<Result<String>> = _resetPasswordStatus
    
    private val _googleSignInStatus = MutableLiveData<Result<LoginResponse>>()
    val googleSignInStatus: LiveData<Result<LoginResponse>> = _googleSignInStatus
    
    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _registerStatus.value = authRepository.register(email, password, firstName, lastName)
        }
    }
    
    fun verifyEmail(email: String, code: String) {
        viewModelScope.launch {
            _verifyStatus.value = authRepository.verifyEmail(email, code)
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginStatus.value = authRepository.login(email, password)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _logoutStatus.value = authRepository.logout()
        }
    }
    
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordStatus.value = authRepository.forgotPassword(email)
        }
    }
    
    fun resetPassword(email: String, code: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordStatus.value = authRepository.resetPassword(email, code, newPassword)
        }
    }
    
    fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _googleSignInStatus.value = authRepository.googleSignIn(idToken)
        }
    }

    /**
     * Clear all auth status LiveData
     * Call this on logout to prevent old success values from triggering navigation
     */
    fun clearAuthState() {
        _loginStatus.value = Result.failure(Exception("Logged out"))
        _googleSignInStatus.value = Result.failure(Exception("Logged out"))
        _registerStatus.value = Result.failure(Exception("Logged out"))
        _verifyStatus.value = Result.failure(Exception("Logged out"))
    }
}
