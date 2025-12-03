package com.integmobile.backend.repository

import com.integmobile.backend.model.entity.CartItem
import com.integmobile.backend.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CartItemRepository : JpaRepository<CartItem, String> {
    fun findByUser(user: User): List<CartItem>
    fun findByUserAndProductId(user: User, productId: String): CartItem?
    fun deleteByUser(user: User)
}
