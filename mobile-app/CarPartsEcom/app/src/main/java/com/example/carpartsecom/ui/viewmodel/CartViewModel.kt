package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.local.entities.CartItemEntity
import com.example.carpartsecom.data.local.entities.CartItemWithProduct
import com.example.carpartsecom.data.repository.CartRepository
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {
    
    val cartItems: LiveData<List<CartItemEntity>> = cartRepository.getCartItems()
    val cartItemsWithProducts: LiveData<List<CartItemWithProduct>> = cartRepository.getCartItemsWithProducts()
    
    private val _addToCartStatus = MutableLiveData<Result<String>>()
    val addToCartStatus: LiveData<Result<String>> = _addToCartStatus
    
    private val _updateCartStatus = MutableLiveData<Result<String>>()
    val updateCartStatus: LiveData<Result<String>> = _updateCartStatus
    
    private val _removeFromCartStatus = MutableLiveData<Result<String>>()
    val removeFromCartStatus: LiveData<Result<String>> = _removeFromCartStatus
    
    private val _syncCartStatus = MutableLiveData<Result<String>>()
    val syncCartStatus: LiveData<Result<String>> = _syncCartStatus
    
    init {
        // Sync cart on initialization
        syncCart()
    }
    
    fun syncCart() {
        viewModelScope.launch {
            _syncCartStatus.value = cartRepository.syncCart()
        }
    }
    
    fun addToCart(productId: Long, quantity: Int) {
        viewModelScope.launch {
            val result = cartRepository.addToCart(productId, quantity)
            _addToCartStatus.value = result
            // Refresh cart after adding
            if (result.isSuccess) {
                syncCart()
            }
        }
    }
    
    fun updateCart(productId: Long, quantity: Int) {
        viewModelScope.launch {
            val result = cartRepository.updateCart(productId, quantity)
            _updateCartStatus.value = result
            // Refresh cart after updating
            if (result.isSuccess) {
                syncCart()
            }
        }
    }
    
    fun removeFromCart(productId: Long) {
        viewModelScope.launch {
            val result = cartRepository.removeFromCart(productId)
            _removeFromCartStatus.value = result
            // Refresh cart after removing
            if (result.isSuccess) {
                syncCart()
            }
        }
    }
    
    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }
}
