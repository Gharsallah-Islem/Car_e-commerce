package com.integmobile.data.repository

import com.integmobile.data.api.CartApiService
import com.integmobile.data.db.dao.CartItemDao
import com.integmobile.data.db.entity.CartItem
import com.integmobile.data.model.request.AddToCartRequest
import com.integmobile.data.model.request.UpdateCartItemRequest
import com.integmobile.data.model.response.CartSummary
import com.integmobile.utils.Result
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for cart operations
 * Manages shopping cart with Room persistence and API sync
 * Provides Flow for real-time cart updates
 */
class CartRepository(
    private val apiService: CartApiService,
    private val cartItemDao: CartItemDao
) {
    
    /**
     * Get all cart items as Flow for real-time updates
     */
    fun getCartItems(): Flow<List<CartItem>> {
        return cartItemDao.getAllCartItems()
    }
    
    /**
     * Add item to cart
     */
    suspend fun addToCart(
        productId: String,
        productName: String,
        price: Double,
        quantity: Int,
        imageUrl: String
    ): Result<CartItem> {
        return try {
            // Check if item already exists
            val existingItem = cartItemDao.getCartItemByProductId(productId)
            
            if (existingItem != null) {
                // Update quantity
                val newQuantity = existingItem.quantity + quantity
                cartItemDao.updateQuantity(existingItem.id, newQuantity)
                val updatedItem = existingItem.copy(quantity = newQuantity)
                
                // Sync with API
                try {
                    apiService.updateCartItem(
                        existingItem.id,
                        UpdateCartItemRequest(newQuantity)
                    )
                } catch (e: Exception) {
                    // Continue even if API sync fails
                }
                
                Result.Success(updatedItem)
            } else {
                // Create new cart item
                val cartItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    productName = productName,
                    price = price,
                    quantity = quantity,
                    imageUrl = imageUrl
                )
                
                // Save to Room
                cartItemDao.insert(cartItem)
                
                // Sync with API
                try {
                    val request = AddToCartRequest(
                        productId, productName, price, quantity, imageUrl
                    )
                    apiService.addToCart(request)
                } catch (e: Exception) {
                    // Continue even if API sync fails
                }
                
                Result.Success(cartItem)
            }
        } catch (e: Exception) {
            Result.Error(e, "Failed to add to cart: ${e.message}")
        }
    }
    
    /**
     * Update cart item quantity
     */
    suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int): Result<Boolean> {
        return try {
            if (quantity <= 0) {
                // Remove item if quantity is 0 or negative
                return removeFromCart(cartItemId)
            }
            
            // Update in Room
            cartItemDao.updateQuantity(cartItemId, quantity)
            
            // Sync with API
            try {
                apiService.updateCartItem(cartItemId, UpdateCartItemRequest(quantity))
            } catch (e: Exception) {
                // Continue even if API sync fails
            }
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "Failed to update cart item: ${e.message}")
        }
    }
    
    /**
     * Remove item from cart
     */
    suspend fun removeFromCart(cartItemId: String): Result<Boolean> {
        return try {
            // Remove from Room
            cartItemDao.deleteById(cartItemId)
            
            // Sync with API
            try {
                apiService.removeFromCart(cartItemId)
            } catch (e: Exception) {
                // Continue even if API sync fails
            }
            
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "Failed to remove from cart: ${e.message}")
        }
    }
    
    /**
     * Clear entire cart
     */
    suspend fun clearCart(): Result<Boolean> {
        return try {
            cartItemDao.deleteAll()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "Failed to clear cart: ${e.message}")
        }
    }
    
    /**
     * Get cart summary with totals
     */
    suspend fun getCartSummary(): CartSummary {
        val subtotal = cartItemDao.getCartTotal() ?: 0.0
        val tax = subtotal * 0.19 // 19% tax (adjust as needed)
        val shipping = if (subtotal > 0) 500.0 else 0.0 // Flat shipping fee in DZD
        val total = subtotal + tax + shipping
        val itemCount = cartItemDao.getCartItemCount()
        
        return CartSummary(
            subtotal = subtotal,
            tax = tax,
            shipping = shipping,
            total = total,
            itemCount = itemCount
        )
    }
    
    /**
     * Get cart item count
     */
    suspend fun getCartItemCount(): Int {
        return cartItemDao.getCartItemCount()
    }
}
