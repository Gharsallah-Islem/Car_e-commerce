package com.example.carpartsecom.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carpartsecom.data.local.entities.ClaimEntity

@Dao
interface ClaimDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity)
    
    @Query("SELECT * FROM claims")
    fun getUserClaims(): LiveData<List<ClaimEntity>>
}
