package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.remote.dto.PaymentIntentResponse
import com.example.carpartsecom.data.remote.dto.PaymentVerifyResponse
import com.example.carpartsecom.data.repository.OrderRepository
import com.example.carpartsecom.data.repository.PaymentRepository
import kotlinx.coroutines.launch

class CheckoutViewModel(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    
    private val _selectedLocation = MutableLiveData<Pair<Double, Double>>()
    val selectedLocation: LiveData<Pair<Double, Double>> = _selectedLocation
    
    private val _selectedPaymentMethod = MutableLiveData<String>()
    val selectedPaymentMethod: LiveData<String> = _selectedPaymentMethod
    
    private val _deliveryAddress = MutableLiveData<String>()
    val deliveryAddress: LiveData<String> = _deliveryAddress
    
    private val _contactPhone = MutableLiveData<String>()
    val contactPhone: LiveData<String> = _contactPhone
    
    private val _deliveryNotes = MutableLiveData<String>()
    val deliveryNotes: LiveData<String> = _deliveryNotes
    
    private val _createOrderStatus = MutableLiveData<Result<Long>>()
    val createOrderStatus: LiveData<Result<Long>> = _createOrderStatus
    
    private val _paymentIntentStatus = MutableLiveData<Result<PaymentIntentResponse>>()
    val paymentIntentStatus: LiveData<Result<PaymentIntentResponse>> = _paymentIntentStatus
    
    private val _paymentVerifyStatus = MutableLiveData<Result<PaymentVerifyResponse>>()
    val paymentVerifyStatus: LiveData<Result<PaymentVerifyResponse>> = _paymentVerifyStatus

    // Store paymentIntentId for card orders
    private var currentPaymentIntentId: String? = null

    fun setLocation(lat: Double, lng: Double) {
        _selectedLocation.value = Pair(lat, lng)
    }
    
    fun setPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }
    
    fun setDeliveryAddress(address: String) {
        _deliveryAddress.value = address
    }
    
    fun setContactPhone(phone: String) {
        _contactPhone.value = phone
    }
    
    fun setDeliveryNotes(notes: String) {
        _deliveryNotes.value = notes
    }
    
    fun createOrder(paymentIntentId: String? = null) {
        val location = _selectedLocation.value
        val lat = location?.first ?: 0.0
        val lng = location?.second ?: 0.0
        val method = _selectedPaymentMethod.value ?: "cash"
        val address = _deliveryAddress.value ?: ""
        val phone = _contactPhone.value ?: ""
        val notes = _deliveryNotes.value ?: ""
        
        android.util.Log.d("CheckoutViewModel", "Creating order with lat=$lat, lng=$lng, method=$method")

        viewModelScope.launch {
            _createOrderStatus.value = orderRepository.createOrder(
                lat,
                lng,
                method,
                address,
                phone,
                notes,
                paymentIntentId ?: currentPaymentIntentId
            )
        }
    }
    
    // Creates payment intent from cart (backend calculates amount)
    fun createPaymentIntent() {
        viewModelScope.launch {
            val result = paymentRepository.createPaymentIntent()
            result.onSuccess { response ->
                currentPaymentIntentId = response.paymentIntentId
            }
            _paymentIntentStatus.value = result
        }
    }

    // Legacy method for backward compatibility
    fun createPaymentIntent(amount: Long) {
        createPaymentIntent()
    }

    fun verifyPayment(paymentIntentId: String) {
        viewModelScope.launch {
            _paymentVerifyStatus.value = paymentRepository.verifyPaymentIntent(paymentIntentId)
        }
    }

    fun getCurrentPaymentIntentId(): String? = currentPaymentIntentId
}
