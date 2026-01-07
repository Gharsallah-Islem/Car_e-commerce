package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.local.entities.ClaimEntity
import com.example.carpartsecom.data.repository.ClaimRepository
import kotlinx.coroutines.launch

class ClaimViewModel(private val claimRepository: ClaimRepository) : ViewModel() {
    
    val claims: LiveData<List<ClaimEntity>> = claimRepository.getUserClaims()
    
    private val _createClaimStatus = MutableLiveData<Result<ClaimEntity>>()
    val createClaimStatus: LiveData<Result<ClaimEntity>> = _createClaimStatus
    
    fun createClaim(orderId: Long, subject: String, description: String) {
        viewModelScope.launch {
            _createClaimStatus.value = claimRepository.createClaim(orderId, subject, description)
        }
    }
}
