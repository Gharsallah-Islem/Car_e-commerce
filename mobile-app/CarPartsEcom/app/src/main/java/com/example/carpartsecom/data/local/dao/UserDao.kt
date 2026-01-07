package com.example.carpartsecom.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carpartsecom.data.local.entities.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): LiveData<UserEntity?>
    
    @Query("DELETE FROM users")
    suspend fun clearUser()
}
