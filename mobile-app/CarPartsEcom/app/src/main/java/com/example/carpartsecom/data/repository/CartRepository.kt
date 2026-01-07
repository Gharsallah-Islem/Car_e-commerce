package com.example.carpartsecom.data.repository

import androidx.lifecycle.LiveData
import com.example.carpartsecom.data.local.dao.CartDao
import com.example.carpartsecom.data.local.entities.CartItemEntity
import com.example.carpartsecom.data.local.entities.CartItemWithProduct
import com.example.carpartsecom.data.remote.api.CartService
import com.example.carpartsecom.data.remote.dto.AddToCartRequest
import com.example.carpartsecom.data.remote.dto.UpdateCartRequest
import com.example.carpartsecom.util.NetworkErrorHandler
import com.example.carpartsecom.util.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository(
    private val cartService: CartService,
    private val cartDao: CartDao,
    private val tokenManager: TokenManager
) {
    fun getCartItems(): LiveData<List<CartItemEntity>> = cartDao.getCartItems()
    
    fun getCartItemsWithProducts(): LiveData<List<CartItemWithProduct>> = cartDao.getCartItemsWithProducts()
    
    suspend fun addToCart(productId: Long, quantity: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val request = AddToCartRequest(productId, quantity)
            val response = cartService.addToCart("Bearer $token", request)
            
            if (response.isSuccessful) {
                cartDao.insertCartItem(CartItemEntity(
                    id = System.currentTimeMillis(),
                    userId = null,
                    productId = productId,
                    quantity = quantity,
                    addedAt = System.currentTimeMillis().toString()
                ))
                Result.success(response.body()?.string() ?: "Added to cart")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun updateCart(productId: Long, quantity: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val request = UpdateCartRequest(productId, quantity)
            val response = cartService.updateCart("Bearer $token", request)
            
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "Updated")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun removeFromCart(productId: Long): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = cartService.removeFromCart(productId, "Bearer $token")
            
            if (response.isSuccessful) {
                cartDao.removeCartItem(productId)
                Result.success(response.body()?.string() ?: "Removed")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun clearCart(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = cartService.clearCart("Bearer $token")
            
            if (response.isSuccessful) {
                cartDao.clearCart()
                Result.success(response.body()?.string() ?: "Cleared")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
    
    suspend fun syncCart(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken() 
                ?: return@withContext Result.failure(Exception("Not authenticated"))
            
            val response = cartService.getCart("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val cartItems = response.body()!!
                cartDao.clearCart()
                cartItems.forEach { item ->
                    cartDao.insertCartItem(CartItemEntity(
                        id = item.id,
                        userId = null,
                        productId = item.productId,
                        quantity = item.quantity,
                        addedAt = item.addedAt
                    ))
                }
                Result.success("Cart synced")
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Result.failure(Exception("Failed (${response.code()}): $errorBody"))
            }
        } catch (e: Exception) {
            return@withContext NetworkErrorHandler.handleException(e)
        }
    }
}
