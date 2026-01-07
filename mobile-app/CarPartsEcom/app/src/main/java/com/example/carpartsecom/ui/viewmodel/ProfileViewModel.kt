package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.remote.dto.UserProfileResponse
import com.example.carpartsecom.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    
    private val _profile = MutableLiveData<Result<UserProfileResponse>>()
    val profile: LiveData<Result<UserProfileResponse>> = _profile
    
    private val _updateProfileStatus = MutableLiveData<Result<UserProfileResponse>>()
    val updateProfileStatus: LiveData<Result<UserProfileResponse>> = _updateProfileStatus
    
    private val _changePasswordStatus = MutableLiveData<Result<String>>()
    val changePasswordStatus: LiveData<Result<String>> = _changePasswordStatus
    
    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = userRepository.getProfile()
        }
    }
    
    fun updateProfile(firstName: String?, lastName: String?, phoneNumber: String?) {
        viewModelScope.launch {
            _updateProfileStatus.value = userRepository.updateProfile(firstName, lastName, phoneNumber)
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordStatus.value = userRepository.changePassword(currentPassword, newPassword)
        }
    }
}
