package com.integmobile.data.db.dao

import androidx.room.*
import com.integmobile.data.db.entity.Order
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Order entity operations
 * Handles order history with status filtering
 */
@Dao
interface OrderDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<Order>)
    
    @Update
    suspend fun update(order: Order)
    
    @Delete
    suspend fun delete(order: Order)
    
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): Order?
    
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    fun getOrdersByUserId(userId: String): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: String): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE userId = :userId AND status = :status ORDER BY orderDate DESC")
    fun getOrdersByUserIdAndStatus(userId: String, status: String): Flow<List<Order>>
    
    @Query("UPDATE orders SET status = :status, updatedAt = :updatedAt WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String, updatedAt: Long)
    
    @Query("DELETE FROM orders")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM orders")
    suspend fun getOrderCount(): Int
    
    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun getOrderCountByStatus(status: String): Int
}
