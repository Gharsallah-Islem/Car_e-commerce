package com.integmobile.ui.orders.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.integmobile.data.db.entity.Order
import com.integmobile.data.repository.OrderRepository
import com.integmobile.utils.Constants
import com.integmobile.utils.Result
import kotlinx.coroutines.launch

/**
 * ViewModel for order operations
 * Manages order history, detail view, cancellation, and claims
 */
class OrderViewModel(
    private val repository: OrderRepository
) : ViewModel() {
    
    // All orders as LiveData from Flow
    val allOrders: LiveData<List<Order>> = repository.getAllOrders().asLiveData()
    
    // Filtered orders by status
    private val _filteredOrders = MutableLiveData<List<Order>>()
    val filteredOrders: LiveData<List<Order>> = _filteredOrders
    
    // Order detail
    private val _orderDetail = MutableLiveData<Result<Order>>()
    val orderDetail: LiveData<Result<Order>> = _orderDetail
    
    // Cancel order result
    private val _cancelOrderResult = MutableLiveData<Result<Boolean>>()
    val cancelOrderResult: LiveData<Result<Boolean>> = _cancelOrderResult
    
    // Submit claim result
    private val _submitClaimResult = MutableLiveData<Result<String>>()
    val submitClaimResult: LiveData<Result<String>> = _submitClaimResult
    
    // Messages
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message
    
    // Current filter
    private val _currentFilter = MutableLiveData<String?>(null)
    val currentFilter: LiveData<String?> = _currentFilter
    
    /**
     * Load order by ID
     */
    fun loadOrderById(orderId: String) {
        viewModelScope.launch {
            _orderDetail.value = Result.Loading
            val result = repository.getOrderById(orderId)
            _orderDetail.value = result
        }
    }
    
    /**
     * Filter orders by status
     */
    fun filterOrdersByStatus(status: String?) {
        _currentFilter.value = status
        
        viewModelScope.launch {
            if (status == null) {
                // Show all orders - handled by allOrders LiveData
                _filteredOrders.value = emptyList()
            } else {
                repository.getOrdersByStatus(status).asLiveData().observeForever { orders ->
                    _filteredOrders.value = orders
                }
            }
        }
    }
    
    /**
     * Get pending orders
     */
    fun getPendingOrders(): LiveData<List<Order>> {
        return repository.getOrdersByStatus(Constants.OrderStatus.PENDING).asLiveData()
    }
    
    /**
     * Get delivered orders
     */
    fun getDeliveredOrders(): LiveData<List<Order>> {
        return repository.getOrdersByStatus(Constants.OrderStatus.DELIVERED).asLiveData()
    }
    
    /**
     * Get cancelled orders
     */
    fun getCancelledOrders(): LiveData<List<Order>> {
        return repository.getOrdersByStatus(Constants.OrderStatus.CANCELLED).asLiveData()
    }
    
    /**
     * Cancel order
     */
    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.cancelOrder(orderId, reason)
            _cancelOrderResult.value = result
            
            when (result) {
                is Result.Success -> {
                    _message.value = "Order cancelled successfully"
                    // Refresh order detail
                    loadOrderById(orderId)
                }
                is Result.Error -> {
                    _message.value = result.exception.message ?: "Failed to cancel order"
                }
                else -> {}
            }
        }
    }
    
    /**
     * Submit claim for order
     */
    fun submitClaim(
        orderId: String,
        reason: String,
        description: String,
        photos: List<String>
    ) {
        viewModelScope.launch {
            val result = repository.submitClaim(orderId, reason, description, photos)
            _submitClaimResult.value = result
            
            when (result) {
                is Result.Success -> {
                    _message.value = "Claim submitted successfully. Claim ID: ${result.data}"
                }
                is Result.Error -> {
                    _message.value = result.exception.message ?: "Failed to submit claim"
                }
                else -> {}
            }
        }
    }
    
    /**
     * Sync orders from API
     */
    fun syncOrders() {
        viewModelScope.launch {
            val result = repository.syncOrders()
            
            when (result) {
                is Result.Success -> {
                    _message.value = "Orders synced successfully"
                }
                is Result.Error -> {
                    _message.value = "Failed to sync orders"
                }
                else -> {}
            }
        }
    }
}
