package com.zirochka.pos.data.model

import java.time.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val active: Boolean = true,
    val category: String = ""
)

@Serializable
data class Category(
    val name: String,
    val items: List<MenuItem> = emptyList()
)

data class OrderItem(
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val price: Int
) {
    val subtotal: Int get() = quantity * price
}

data class Order(
    val id: String,
    val createdAt: LocalDateTime,
    val table: String,
    val items: List<OrderItem>,
    val total: Int,
    val payment: String,
    val note: String = "",
    val status: String = "active"
)
