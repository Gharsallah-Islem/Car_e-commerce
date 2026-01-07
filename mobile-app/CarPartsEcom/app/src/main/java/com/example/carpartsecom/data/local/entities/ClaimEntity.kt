package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "claims")
data class ClaimEntity(
    @PrimaryKey val id: Long,
    val orderId: Long,
    val userId: Long,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: String
)
