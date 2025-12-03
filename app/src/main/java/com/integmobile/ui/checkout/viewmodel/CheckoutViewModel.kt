package com.integmobile.ui.checkout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.integmobile.data.db.entity.Order
import com.integmobile.data.db.entity.OrderItem
import com.integmobile.data.repository.CartRepository
import com.integmobile.data.repository.OrderRepository
import com.integmobile.data.repository.PaymentRepository
import com.integmobile.utils.Constants
import com.integmobile.utils.Result
import kotlinx.coroutines.launch

/**
 * Sealed class for checkout state management
 */
sealed class CheckoutState {
    object Idle : CheckoutState()
    object ProcessingPayment : CheckoutState()
    data class PaymentError(val message: String) : CheckoutState()
    data class OrderSuccess(val orderId: String) : CheckoutState()
}

/**
 * ViewModel for checkout process
 * Manages multi-step checkout with location, payment, and order creation
 */
class CheckoutViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    
    // Checkout state
    private val _checkoutState = MutableLiveData<CheckoutState>(CheckoutState.Idle)
    val checkoutState: LiveData<CheckoutState> = _checkoutState
    
    // Delivery address
    private val _deliveryAddress = MutableLiveData<String>()
    val deliveryAddress: LiveData<String> = _deliveryAddress
    
    // Location coordinates
    private val _latitude = MutableLiveData<Double>()
    private val _longitude = MutableLiveData<Double>()
    
    // Payment method
    private val _paymentMethod = MutableLiveData<String>(Constants.PaymentMethod.CASH_ON_DELIVERY)
    val paymentMethod: LiveData<String> = _paymentMethod
    
    // Phone number
    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> = _phoneNumber
    
    // Order items
    private val _orderItems = MutableLiveData<List<OrderItem>>()
    val orderItems: LiveData<List<OrderItem>> = _orderItems
    
    // Total amount
    private val _totalAmount = MutableLiveData<Double>()
    val totalAmount: LiveData<Double> = _totalAmount
    
    // Stripe client secret
    private val _clientSecret = MutableLiveData<String>()
    val clientSecret: LiveData<String> = _clientSecret
    
    /**
     * Set delivery address and location
     */
    fun setDeliveryLocation(address: String, latitude: Double, longitude: Double) {
        _deliveryAddress.value = address
        _latitude.value = latitude
        _longitude.value = longitude
    }
    
    /**
     * Set payment method
     */
    fun setPaymentMethod(method: String) {
        _paymentMethod.value = method
    }
    
    /**
     * Set phone number
     */
    fun setPhoneNumber(phone: String) {
        _phoneNumber.value = phone
    }
    
    /**
     * Set order items from cart
     */
    fun setOrderItems(items: List<OrderItem>, total: Double) {
        _orderItems.value = items
        _totalAmount.value = total
    }
    
    /**
     * Create payment intent for Stripe
     */
    fun createPaymentIntent() {
        val amount = _totalAmount.value ?: return
        
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.ProcessingPayment
            
            val result = paymentRepository.createPaymentIntent(amount)
            
            when (result) {
                is Result.Success -> {
                    _clientSecret.value = result.data
                }
                is Result.Error -> {
                    _checkoutState.value = CheckoutState.PaymentError(
                        result.exception.message ?: "Failed to create payment intent"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Complete checkout with Cash on Delivery
     */
    fun completeCheckoutWithCash() {
        val items = _orderItems.value ?: return
        val address = _deliveryAddress.value ?: return
        val lat = _latitude.value ?: return
        val lon = _longitude.value ?: return
        val phone = _phoneNumber.value ?: return
        
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.ProcessingPayment
            
            val result = orderRepository.createOrder(
                items = items,
                deliveryAddress = address,
                latitude = lat,
                longitude = lon,
                paymentMethod = Constants.PaymentMethod.CASH_ON_DELIVERY,
                phoneNumber = phone
            )
            
            when (result) {
                is Result.Success -> {
                    // Clear cart after successful order
                    cartRepository.clearCart()
                    _checkoutState.value = CheckoutState.OrderSuccess(result.data.id)
                }
                is Result.Error -> {
                    _checkoutState.value = CheckoutState.PaymentError(
                        result.exception.message ?: "Failed to create order"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Complete checkout with Stripe payment
     */
    fun completeCheckoutWithStripe() {
        val items = _orderItems.value ?: return
        val address = _deliveryAddress.value ?: return
        val lat = _latitude.value ?: return
        val lon = _longitude.value ?: return
        val phone = _phoneNumber.value ?: return
        
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.ProcessingPayment
            
            // Create order with Stripe payment method
            val result = orderRepository.createOrder(
                items = items,
                deliveryAddress = address,
                latitude = lat,
                longitude = lon,
                paymentMethod = Constants.PaymentMethod.STRIPE,
                phoneNumber = phone
            )
            
            when (result) {
                is Result.Success -> {
                    // Clear cart after successful order
                    cartRepository.clearCart()
                    _checkoutState.value = CheckoutState.OrderSuccess(result.data.id)
                }
                is Result.Error -> {
                    _checkoutState.value = CheckoutState.PaymentError(
                        result.exception.message ?: "Failed to create order"
                    )
                }
                else -> {}
            }
        }
    }
    
    /**
     * Reset checkout state
     */
    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}
