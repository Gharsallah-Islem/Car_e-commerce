package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val isFromUser: Boolean,  // true = user sent, false = assistant response
    val timestamp: Long = System.currentTimeMillis(),
    val productRecommendations: String? = null  // Comma-separated product names
)

