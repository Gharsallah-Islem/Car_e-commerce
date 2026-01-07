package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.carpartsecom.data.repository.*

class ViewModelFactory(
    private val authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val claimRepository: ClaimRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(authRepository) as T
            ProductViewModel::class.java -> ProductViewModel(productRepository) as T
            CartViewModel::class.java -> CartViewModel(cartRepository) as T
            CheckoutViewModel::class.java -> CheckoutViewModel(orderRepository, paymentRepository) as T
            OrderViewModel::class.java -> OrderViewModel(orderRepository) as T
            ClaimViewModel::class.java -> ClaimViewModel(claimRepository) as T
            ProfileViewModel::class.java -> ProfileViewModel(userRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
