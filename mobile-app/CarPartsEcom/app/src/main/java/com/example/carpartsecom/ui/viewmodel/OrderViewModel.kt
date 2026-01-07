package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.local.entities.OrderEntity
import com.example.carpartsecom.data.local.entities.OrderItemEntity
import com.example.carpartsecom.data.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    
    val orders: LiveData<List<OrderEntity>> = orderRepository.getUserOrders()
    
    private val _cancelOrderStatus = MutableLiveData<Result<String>>()
    val cancelOrderStatus: LiveData<Result<String>> = _cancelOrderStatus
    
    fun getOrderById(orderId: Long): LiveData<OrderEntity?> {
        return orderRepository.getOrderById(orderId)
    }
    
    fun getOrderItems(orderId: Long): LiveData<List<OrderItemEntity>> {
        return orderRepository.getOrderItems(orderId)
    }

    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            _cancelOrderStatus.value = orderRepository.cancelOrder(orderId)
        }
    }
    
    fun refreshOrders() {
        viewModelScope.launch {
            orderRepository.getOrders()
        }
    }
}
