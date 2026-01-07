package com.example.carpartsecom.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val price: Double,
    val category: String,
    val description: String,
    val stockQuantity: Int,
    val rating: Double,
    val imageUrl: String
)
