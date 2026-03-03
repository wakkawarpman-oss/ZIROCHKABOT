package com.zirochka.pos.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zirochka.pos.data.local.dao.CategoryDao
import com.zirochka.pos.data.local.dao.MenuItemDao
import com.zirochka.pos.data.local.dao.OrderDao
import com.zirochka.pos.data.local.entity.CategoryEntity
import com.zirochka.pos.data.local.entity.MenuItemEntity
import com.zirochka.pos.data.local.entity.OrderEntity

@Database(
    entities = [MenuItemEntity::class, CategoryEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderDao(): OrderDao
}
