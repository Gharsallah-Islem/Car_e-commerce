package com.integmobile.data.repository

import com.integmobile.data.api.OrderApiService
import com.integmobile.data.db.dao.OrderDao
import com.integmobile.data.db.entity.Order
import com.integmobile.data.db.entity.OrderItem
import com.integmobile.data.model.request.CancelOrderRequest
import com.integmobile.data.model.request.CreateOrderRequest
import com.integmobile.data.model.request.SubmitClaimRequest
import com.integmobile.utils.Constants
import com.integmobile.utils.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository for order operations
 * Handles order creation, tracking, cancellation, and claims
 */
class OrderRepository(
    private val apiService: OrderApiService,
    private val orderDao: OrderDao
) {
    
    /**
     * Get all orders as Flow
     */
    fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders()
    }
    
    /**
     * Get orders by status
     */
    fun getOrdersByStatus(status: String): Flow<List<Order>> {
        return orderDao.getOrdersByStatus(status)
    }
    
    /**
     * Get order by ID
     */
    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            // Try API first
            val response = apiService.getOrderById(orderId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val order = response.body()?.data
                if (order != null) {
                    // Update cache
                    orderDao.insert(order)
                    Result.Success(order)
                } else {
                    // Fallback to cache
                    val cachedOrder = orderDao.getOrderById(orderId)
                    if (cachedOrder != null) {
                        Result.Success(cachedOrder)
                    } else {
                        Result.Error(Exception("Order not found"))
                    }
                }
            } else {
                // Fallback to cache
                val cachedOrder = orderDao.getOrderById(orderId)
                if (cachedOrder != null) {
                    Result.Success(cachedOrder)
                } else {
                    Result.Error(Exception("Order not found"))
                }
            }
        } catch (e: Exception) {
            // Try cache on network error
            val cachedOrder = orderDao.getOrderById(orderId)
            if (cachedOrder != null) {
                Result.Success(cachedOrder)
            } else {
                Result.Error(e, "Network error: ${e.message}")
            }
        }
    }
    
    /**
     * Create new order
     */
    suspend fun createOrder(
        items: List<OrderItem>,
        deliveryAddress: String,
        latitude: Double,
        longitude: Double,
        paymentMethod: String,
        phoneNumber: String
    ): Result<Order> {
        return try {
            val request = CreateOrderRequest(
                items = items,
                deliveryAddress = deliveryAddress,
                latitude = latitude,
                longitude = longitude,
                paymentMethod = paymentMethod,
                phoneNumber = phoneNumber
            )
            
            val response = apiService.createOrder(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val order = response.body()?.data
                if (order != null) {
                    // Save to Room
                    orderDao.insert(order)
                    Result.Success(order)
                } else {
                    Result.Error(Exception("Failed to create order"))
                }
            } else {
                val errorMessage = response.body()?.message ?: "Order creation failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Cancel order (only if status is PENDING or CONFIRMED)
     */
    suspend fun cancelOrder(orderId: String, reason: String): Result<Boolean> {
        return try {
            // Check order status first
            val order = orderDao.getOrderById(orderId)
            if (order == null) {
                return Result.Error(Exception("Order not found"))
            }
            
            if (order.status != Constants.OrderStatus.PENDING && 
                order.status != Constants.OrderStatus.CONFIRMED) {
                return Result.Error(Exception("Cannot cancel order with status: ${order.status}"))
            }
            
            val request = CancelOrderRequest(reason)
            val response = apiService.cancelOrder(orderId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                // Update local status
                orderDao.updateOrderStatus(
                    orderId,
                    Constants.OrderStatus.CANCELLED,
                    System.currentTimeMillis()
                )
                Result.Success(true)
            } else {
                val errorMessage = response.body()?.message ?: "Cancellation failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Submit claim for order
     */
    suspend fun submitClaim(
        orderId: String,
        reason: String,
        description: String,
        photos: List<String>
    ): Result<String> {
        return try {
            val request = SubmitClaimRequest(
                orderId = orderId,
                reason = reason,
                description = description,
                photos = photos
            )
            
            val response = apiService.submitClaim(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val claimId = response.body()?.claimId ?: ""
                Result.Success(claimId)
            } else {
                val errorMessage = response.body()?.message ?: "Claim submission failed"
                Result.Error(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
    
    /**
     * Sync orders from API
     */
    suspend fun syncOrders(): Result<List<Order>> {
        return try {
            val response = apiService.getOrders()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val orders = response.body()?.data ?: emptyList()
                if (orders.isNotEmpty()) {
                    orderDao.insertAll(orders)
                }
                Result.Success(orders)
            } else {
                Result.Error(Exception("Failed to sync orders"))
            }
        } catch (e: Exception) {
            Result.Error(e, "Network error: ${e.message}")
        }
    }
}
