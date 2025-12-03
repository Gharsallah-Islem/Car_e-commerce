package com.integmobile.backend.model.entity

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.*
import org.hibernate.annotations.Type
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "products")
data class Product(
    @Id
    val id: String = UUID.randomUUID().toString(),
    
    @Column(nullable = false)
    val name: String,
    
    @Column(columnDefinition = "TEXT")
    val description: String,
    
    @Column(nullable = false)
    val price: Double,
    
    @Column(name = "original_price")
    val originalPrice: Double? = null,
    
    val discount: Int? = null,
    
    val brand: String,
    
    val category: String,
    
    @Column(name = "image_urls", columnDefinition = "TEXT[]")
    val imageUrls: Array<String> = emptyArray(),
    
    @Column(name = "in_stock")
    val inStock: Boolean = true,
    
    @Column(name = "quantity_available")
    val quantityAvailable: Int = 0,
    
    @Column(columnDefinition = "jsonb")
    val specifications: Map<String, String> = emptyMap(),
    
    @Column(columnDefinition = "TEXT[]")
    val compatibility: Array<String> = emptyArray(),
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Product
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
