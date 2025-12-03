package com.integmobile.ui.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.integmobile.data.repository.CartRepository
import com.integmobile.data.repository.OrderRepository
import com.integmobile.data.repository.PaymentRepository

/**
 * Factory for creating CheckoutViewModel with dependencies
 */
class CheckoutViewModelFactory(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(cartRepository, orderRepository, paymentRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
