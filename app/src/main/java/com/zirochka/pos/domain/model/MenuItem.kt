package com.zirochka.pos.domain.model

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val categoryId: Int,
    val available: Boolean = true
)
