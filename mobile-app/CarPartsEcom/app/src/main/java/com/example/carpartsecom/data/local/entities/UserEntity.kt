package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val email: String,
    val token: String,
    val isVerified: Boolean,
    val createdAt: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val googleId: String?
)
