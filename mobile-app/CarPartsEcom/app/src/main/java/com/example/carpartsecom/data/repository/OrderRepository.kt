package com.example.carpartsecom.data.repository

import androidx.lifecycle.LiveData
import com.example.carpartsecom.data.local.dao.CartDao
import com.example.carpartsecom.data.local.dao.OrderDao
import com.example.carpartsecom.data.local.dao.OrderItemDao
import com.example.carpartsecom.data.local.entities.OrderEntity
import com.example.carpartsecom.data.local.entities.OrderItemEntity
import com.example.carpartsecom.data.remote.api.OrderService
import com.example.carpartsecom.data.remote.dto.CreateOrderRequest
import com.example.carpartsecom.data.remote.dto.toEntity
import com.example.carpartsecom.util.NetworkErrorHandler
import com.example.carpartsecom.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(
    private val orderService: OrderService,
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val cartDao: CartDao,
    private val tokenManager: TokenManager
) {
    fun getUserOrders(): LiveData<List<OrderEntity>> = orderDao.getUserOrders()
    
    fun getOrderById(orderId: Long): LiveData<OrderEntity?> = orderDao.getOrderById(orderId)
    
    fun getOrderItems(orderId: Long): LiveData<List<OrderItemEntity>> = orderItemDao.getOrderItems(orderId)

    suspend fun createOrder(
        deliveryLat: Double,
        deliveryLng: Double,
        paymentMethod: String,
        deliveryAddress: String = "",
        contactPhone: String = "",
        deliveryNotes: String = "",
        paymentIntentId: String? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            // Get cart items BEFORE creating the order (to save as order items)
            val cartItems = cartDao.getCartItemsSync()

            val request = CreateOrderRequest(
                paymentMethod = paymentMethod,
                deliveryLatitude = deliveryLat,
                deliveryLongitude = deliveryLng,
                deliveryAddress = deliveryAddress,
                contactPhone = contactPhone,
                deliveryNotes = deliveryNotes,
                paymentIntentId = paymentIntentId
            )
            val response = orderService.createOrder("Bearer $token", request)

            if (response.isSuccessful && response.body() != null) {
                val orderResponse = response.body()!!
                val orderEntity = orderResponse.toEntity()
                orderDao.insertOrder(orderEntity)

                // Save order items from cart
                val orderItems = cartItems.mapIndexed { index, cartItem ->
                    OrderItemEntity(
                        id = System.currentTimeMillis() + index, // Generate local ID
                        orderId = orderResponse.id,
                        productId = cartItem.productId,
                        productName = cartItem.productName,
                        quantity = cartItem.quantity,
                        priceAtPurchase = cartItem.productPrice
                    )
                }
                orderItemDao.insertOrderItems(orderItems)

                // Clear the cart after saving order items
                cartDao.clearCart()
                Result.success(orderResponse.id)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun getOrders(): Result<List<OrderEntity>> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = orderService.getOrders("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!.map { it.toEntity() }
                orders.forEach { orderDao.insertOrder(it) }
                Result.success(orders)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun cancelOrder(orderId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = orderService.cancelOrder(orderId, "Bearer $token")
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Cancelled")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
}
