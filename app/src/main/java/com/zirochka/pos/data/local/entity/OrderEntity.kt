package com.zirochka.pos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val total: Double,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)
