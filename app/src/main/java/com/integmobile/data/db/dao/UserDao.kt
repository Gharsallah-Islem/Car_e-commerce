package com.integmobile.data.db.dao

import androidx.room.*
import com.integmobile.data.db.entity.User

/**
 * DAO for User entity operations
 * Handles user session persistence
 */
@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)
    
    @Update
    suspend fun update(user: User)
    
    @Delete
    suspend fun delete(user: User)
    
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
