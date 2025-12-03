package com.integmobile.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for user session persistence
 * Stores user data and JWT token for authentication
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String? = null,
    val token: String,
    val refreshToken: String? = null,
    val profileImage: String? = null,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
