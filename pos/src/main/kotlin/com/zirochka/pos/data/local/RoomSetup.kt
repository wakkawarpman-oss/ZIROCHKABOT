package com.zirochka.pos.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.zirochka.pos.data.model.MenuItem
import com.zirochka.pos.data.model.Order
import com.zirochka.pos.data.model.OrderItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val name: String
)

@Entity(
    tableName = "menu_items",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["name"],
            childColumns = ["category"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("category")]
)
data class MenuItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val active: Boolean,
    val category: String
) {
    fun toDomain() = MenuItem(id, name, description, price, active, category)
}

data class CategoryWithItems(
    @androidx.room.Embedded val category: CategoryEntity,
    @Relation(parentColumn = "name", entityColumn = "category")
    val items: List<MenuItemEntity>
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val createdAt: LocalDateTime,
    val table: String,
    val total: Int,
    val payment: String,
    val note: String,
    val status: String
) {
    fun toDomain(items: List<OrderItemEntity>) = Order(
        id = id,
        createdAt = createdAt,
        table = table,
        items = items.map { it.toDomain() },
        total = total,
        payment = payment,
        note = note,
        status = status
    )
}

@Entity(
    tableName = "order_items",
    primaryKeys = ["orderId", "menuItemId"],
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("menuItemId")]
)
data class OrderItemEntity(
    val orderId: String,
    val menuItemId: String,
    val name: String,
    val quantity: Int,
    val price: Int
) {
    fun toDomain() = OrderItem(menuItemId, name, quantity, price)
}

@Dao
interface MenuDao {
    @Transaction
    @Query("SELECT * FROM categories")
    fun observeCategories(): Flow<List<CategoryWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<MenuItemEntity>)

    @Query("DELETE FROM categories")
    suspend fun clearCategories()
}

data class OrderWithItems(
    @androidx.room.Embedded val order: OrderEntity,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity>
)

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeOrders(): Flow<List<OrderWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)
}

@Database(
    entities = [CategoryEntity::class, MenuItemEntity::class, OrderEntity::class, OrderItemEntity::class],
    version = 1
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : androidx.room.RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun orderDao(): OrderDao
}

class DateTimeConverters {
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, formatter) }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.format(formatter)

    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
}
