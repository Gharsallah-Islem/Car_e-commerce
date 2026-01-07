package com.example.carpartsecom.data.remote.dto

import com.example.carpartsecom.data.local.entities.ProductEntity

data class ProductResponse(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String,
    val description: String,
    val stockQuantity: Int,
    val rating: Double,
    val imageUrl: String
)

// Mapper
fun ProductResponse.toEntity(): ProductEntity {
    return ProductEntity(
        id = this.id,
        name = this.name,
        price = this.price,
        category = this.category,
        description = this.description,
        stockQuantity = this.stockQuantity,
        rating = this.rating,
        imageUrl = this.imageUrl
    )
}
