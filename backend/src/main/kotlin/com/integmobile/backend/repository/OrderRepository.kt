package com.integmobile.backend.repository

import com.integmobile.backend.model.entity.Order
import com.integmobile.backend.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, String> {
    fun findByUser(user: User): List<Order>
    fun findByUserAndStatus(user: User, status: String): List<Order>
    fun findByUserOrderByCreatedAtDesc(user: User): List<Order>
}
