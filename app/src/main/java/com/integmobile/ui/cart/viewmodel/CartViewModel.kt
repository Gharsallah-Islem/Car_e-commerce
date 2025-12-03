package com.integmobile.ui.cart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.integmobile.data.db.entity.CartItem
import com.integmobile.data.model.response.CartSummary
import com.integmobile.data.repository.CartRepository
import com.integmobile.utils.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for cart operations
 * Manages shopping cart with Flow for real-time updates
 */
class CartViewModel(
    private val repository: CartRepository
) : ViewModel() {
    
    // Cart items as LiveData from Flow for reactive UI updates
    val cartItems: LiveData<List<CartItem>> = repository.getCartItems().asLiveData()
    
    // Cart summary
    private val _cartSummary = MutableLiveData<CartSummary>()
    val cartSummary: LiveData<CartSummary> = _cartSummary
    
    // Add to cart result
    private val _addToCartResult = MutableLiveData<Result<CartItem>>()
    val addToCartResult: LiveData<Result<CartItem>> = _addToCartResult
    
    // Update cart result
    private val _updateCartResult = MutableLiveData<Result<Boolean>>()
    val updateCartResult: LiveData<Result<Boolean>> = _updateCartResult
    
    // Remove from cart result
    private val _removeFromCartResult = MutableLiveData<Result<Boolean>>()
    val removeFromCartResult: LiveData<Result<Boolean>> = _removeFromCartResult
    
    // Messages
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    
    /**
     * Add item to cart
     */
    fun addToCart(
        productId: String,
        productName: String,
        price: Double,
        quantity: Int,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val result = repository.addToCart(productId, productName, price, quantity, imageUrl)
            _addToCartResult.value = result
            
            if (result is Result.Success) {
                _message.value = "Added to cart"
                refreshCartSummary()
            } else if (result is Result.Error) {
                _message.value = result.exception.message ?: "Failed to add to cart"
            }
        }
    }
    
    /**
     * Update cart item quantity
     */
    fun updateQuantity(cartItemId: String, quantity: Int) {
        viewModelScope.launch {
            val result = repository.updateCartItemQuantity(cartItemId, quantity)
            _updateCartResult.value = result
            
            if (result is Result.Success) {
                refreshCartSummary()
            } else if (result is Result.Error) {
                _message.value = result.exception.message ?: "Failed to update quantity"
            }
        }
    }
    
    /**
     * Remove item from cart
     */
    fun removeFromCart(cartItemId: String) {
        viewModelScope.launch {
            val result = repository.removeFromCart(cartItemId)
            _removeFromCartResult.value = result
            
            if (result is Result.Success) {
                _message.value = "Item removed from cart"
                refreshCartSummary()
            } else if (result is Result.Error) {
                _message.value = result.exception.message ?: "Failed to remove item"
            }
        }
    }
    
    /**
     * Clear entire cart
     */
    fun clearCart() {
        viewModelScope.launch {
            val result = repository.clearCart()
            
            if (result is Result.Success) {
                _message.value = "Cart cleared"
                refreshCartSummary()
            } else if (result is Result.Error) {
                _message.value = result.exception.message ?: "Failed to clear cart"
            }
        }
    }
    
    /**
     * Refresh cart summary
     */
    fun refreshCartSummary() {
        viewModelScope.launch {
            val summary = repository.getCartSummary()
            _cartSummary.value = summary
        }
    }
    
    /**
     * Get cart item count
     */
    suspend fun getCartItemCount(): Int {
        return repository.getCartItemCount()
    }
}
