package com.example.carpartsecom.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carpartsecom.data.local.entities.CartItemEntity
import com.example.carpartsecom.data.local.entities.CartItemWithProduct

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)
    
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): LiveData<List<CartItemEntity>>
    
    @Query("""
        SELECT 
            c.id as cartItemId,
            c.productId as productId,
            c.quantity as quantity,
            c.addedAt as addedAt,
            p.name as productName,
            p.price as productPrice,
            p.imageUrl as productImageUrl,
            p.stockQuantity as productStockQuantity,
            p.category as productCategory
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
    """)
    fun getCartItemsWithProducts(): LiveData<List<CartItemWithProduct>>
    
    @Update
    suspend fun updateCartItem(item: CartItemEntity)
    
    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeCartItem(productId: Long)
    
    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("""
        SELECT 
            c.id as cartItemId,
            c.productId as productId,
            c.quantity as quantity,
            c.addedAt as addedAt,
            p.name as productName,
            p.price as productPrice,
            p.imageUrl as productImageUrl,
            p.stockQuantity as productStockQuantity,
            p.category as productCategory
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
    """)
    suspend fun getCartItemsSync(): List<CartItemWithProduct>
}
