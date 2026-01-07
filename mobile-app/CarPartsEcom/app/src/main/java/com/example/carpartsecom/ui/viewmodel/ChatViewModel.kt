package com.example.carpartsecom.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carpartsecom.data.local.dao.ChatDao
import com.example.carpartsecom.data.local.dao.ProductDao
import com.example.carpartsecom.data.local.entities.ChatMessageEntity
import com.example.carpartsecom.data.local.entities.ProductEntity
import com.example.carpartsecom.util.CarAssistant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatDao: ChatDao,
    private val productDao: ProductDao
) : ViewModel() {

    val messages: LiveData<List<ChatMessageEntity>> = chatDao.getAllMessages()

    private val _isTyping = MutableLiveData<Boolean>(false)
    val isTyping: LiveData<Boolean> = _isTyping

    private val _recommendedProducts = MutableLiveData<List<ProductEntity>>()
    val recommendedProducts: LiveData<List<ProductEntity>> = _recommendedProducts

    init {
        // Add welcome message if no messages exist
        viewModelScope.launch {
            if (chatDao.getMessageCount() == 0) {
                addWelcomeMessage()
            }
        }
    }

    private suspend fun addWelcomeMessage() {
        val welcomeMessage = ChatMessageEntity(
            message = """
                👋 Hi! I'm your Virtual Car Assistant!
                
                I can help you with:
                🔧 Finding the right parts for your car
                🔍 Basic diagnostics for common issues
                💡 Maintenance recommendations
                
                Just tell me about your car or describe any issues you're experiencing!
            """.trimIndent(),
            isFromUser = false
        )
        chatDao.insertMessage(welcomeMessage)
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            // Save user message
            val userMsg = ChatMessageEntity(
                message = userMessage.trim(),
                isFromUser = true
            )
            chatDao.insertMessage(userMsg)

            // Show typing indicator
            _isTyping.value = true

            // Simulate thinking delay (makes it feel more natural)
            delay(800 + (Math.random() * 700).toLong())

            // Get assistant response
            val response = CarAssistant.getResponse(userMessage)

            // Save assistant response
            val assistantMsg = ChatMessageEntity(
                message = response.message,
                isFromUser = false,
                productRecommendations = if (response.productRecommendations.isNotEmpty()) {
                    response.productRecommendations.joinToString(",")
                } else null
            )
            chatDao.insertMessage(assistantMsg)

            // Hide typing indicator
            _isTyping.value = false

            // Load recommended products if any
            if (response.productRecommendations.isNotEmpty()) {
                loadRecommendedProducts(response.productRecommendations)
            }
        }
    }

    private suspend fun loadRecommendedProducts(productNames: List<String>) {
        val products = mutableListOf<ProductEntity>()
        for (name in productNames) {
            val found = productDao.searchProductsSync("%$name%")
            products.addAll(found)
        }
        _recommendedProducts.postValue(products.distinctBy { it.id })
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearChat()
            addWelcomeMessage()
            _recommendedProducts.value = emptyList()
        }
    }

    fun clearProductRecommendations() {
        _recommendedProducts.value = emptyList()
    }
}

