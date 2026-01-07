package com.example.carpartsecom.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carpartsecom.data.local.entities.OrderEntity

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)
    
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getUserOrders(): LiveData<List<OrderEntity>>
    
    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): LiveData<OrderEntity?>

    @Query("DELETE FROM orders")
    suspend fun clearOrders()
}
