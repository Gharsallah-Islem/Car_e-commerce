package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "otp_codes")
data class OtpCodeEntity(
    @PrimaryKey val id: Long,
    val email: String?,
    val code: String?,
    val purpose: String?,
    val expiresAt: String?,
    val isUsed: Boolean
)
