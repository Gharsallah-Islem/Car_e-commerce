package com.example.carpartsecom.data.remote.dto

import com.example.carpartsecom.data.local.entities.ClaimEntity

data class CreateClaimRequest(
    val orderId: Long,
    val subject: String,
    val description: String
)

data class ClaimResponse(
    val id: Long,
    val orderId: Long,
    val userId: Long,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: String
)

// Mapper
fun ClaimResponse.toEntity(): ClaimEntity {
    return ClaimEntity(
        id = this.id,
        orderId = this.orderId,
        userId = this.userId,
        subject = this.subject,
        description = this.description,
        status = this.status,
        createdAt = this.createdAt
    )
}
