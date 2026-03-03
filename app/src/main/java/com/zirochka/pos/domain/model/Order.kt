package com.zirochka.pos.domain.model

data class Order(
    val id: Long = 0,
    val items: List<OrderLine>,
    val total: Double,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)

data class OrderLine(
    val item: MenuItem,
    val quantity: Int
)
