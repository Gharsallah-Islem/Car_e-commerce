package com.integmobile.data.db.dao

import androidx.room.*
import com.integmobile.data.db.entity.CartItem
import kotlinx.coroutines.flow.Flow

/**
 * DAO for CartItem entity operations
 * Handles shopping cart persistence with Flow for real-time updates
 */
@Dao
interface CartItemDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem)
    
    @Update
    suspend fun update(cartItem: CartItem)
    
    @Delete
    suspend fun delete(cartItem: CartItem)
    
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartItem>>
    
    @Query("SELECT * FROM cart_items WHERE id = :cartItemId LIMIT 1")
    suspend fun getCartItemById(cartItemId: String): CartItem?
    
    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProductId(productId: String): CartItem?
    
    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :cartItemId")
    suspend fun updateQuantity(cartItemId: String, quantity: Int)
    
    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteById(cartItemId: String)
    
    @Query("DELETE FROM cart_items")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM cart_items")
    suspend fun getCartItemCount(): Int
    
    @Query("SELECT SUM(price * quantity) FROM cart_items")
    suspend fun getCartTotal(): Double?
}
