package com.integmobile.backend.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "otp_verifications")
data class OtpVerification(
    @Id
    val id: String = UUID.randomUUID().toString(),
    
    @Column(nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val otp: String,
    
    @Column(nullable = false)
    val purpose: String, // EMAIL_VERIFICATION or PASSWORD_RESET
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    
    @Column(nullable = false)
    var verified: Boolean = false,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
