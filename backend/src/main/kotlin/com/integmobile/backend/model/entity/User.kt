package com.integmobile.backend.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    val id: String = UUID.randomUUID().toString(),
    
    @Column(unique = true, nullable = false)
    val email: String,
    
    @Column(nullable = false)
    var password: String,
    
    @Column(name = "full_name", nullable = false)
    val fullName: String,
    
    @Column(name = "phone_number")
    val phoneNumber: String? = null,
    
    @Column(name = "is_verified")
    var isVerified: Boolean = false,
    
    @Column(name = "profile_image")
    val profileImage: String? = null,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
