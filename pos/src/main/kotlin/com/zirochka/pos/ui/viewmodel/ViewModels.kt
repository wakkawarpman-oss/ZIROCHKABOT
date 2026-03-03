package com.zirochka.pos.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirochka.pos.data.local.CategoryEntity
import com.zirochka.pos.data.local.MenuDao
import com.zirochka.pos.data.local.MenuItemEntity
import com.zirochka.pos.data.local.OrderDao
import com.zirochka.pos.data.local.OrderEntity
import com.zirochka.pos.data.local.OrderItemEntity
import com.zirochka.pos.data.model.Category
import com.zirochka.pos.data.model.MenuItem
import com.zirochka.pos.data.model.Order
import com.zirochka.pos.data.model.OrderItem
import com.zirochka.pos.data.remote.GoogleSheetsService
import com.zirochka.pos.data.remote.SheetsAppendRequest
import com.zirochka.pos.data.remote.TelegramMessageRequest
import com.zirochka.pos.data.remote.TelegramService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MenuViewModel(
    private val menuDao: MenuDao
) : ViewModel() {
    val categories: StateFlow<List<Category>> = menuDao.observeCategories()
        .map { list -> list.map { Category(it.category.name, it.items.map(MenuItemEntity::toDomain)) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun importMenu(categories: List<Category>) {
        viewModelScope.launch {
            menuDao.clearCategories()
            menuDao.insertCategories(categories.map { CategoryEntity(it.name) })
            menuDao.insertMenuItems(
                categories.flatMap { category ->
                    category.items.map {
                        MenuItemEntity(
                            id = it.id,
                            name = it.name,
                            description = it.description,
                            price = it.price,
                            active = it.active,
                            category = category.name
                        )
                    }
                }
            )
        }
    }
}

class CartViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<OrderItem>>(emptyList())
    val items: StateFlow<List<OrderItem>> = _items
    val total: StateFlow<Int> = _items
        .map { list -> list.sumOf { it.subtotal } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun add(item: MenuItem) {
        val existing = _items.value.firstOrNull { it.menuItemId == item.id }
        _items.value = if (existing == null) {
            _items.value + OrderItem(item.id, item.name, 1, item.price)
        } else {
            _items.value.map {
                if (it.menuItemId == item.id) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    fun inc(item: OrderItem) {
        _items.value = _items.value.map {
            if (it.menuItemId == item.menuItemId) it.copy(quantity = it.quantity + 1) else it
        }
    }

    fun dec(item: OrderItem) {
        _items.value = _items.value.mapNotNull {
            if (it.menuItemId != item.menuItemId) return@mapNotNull it
            val newQty = it.quantity - 1
            if (newQty > 0) it.copy(quantity = newQty) else null
        }
    }

    fun clear() {
        _items.value = emptyList()
    }
}

class OrdersViewModel(
    private val orderDao: OrderDao,
    private val sheetsService: GoogleSheetsService,
    private val telegramService: TelegramService,
    private val sheetsRange: String,
    private val telegramChatId: String
) : ViewModel() {
    val orders = orderDao.observeOrders()
        .map { list -> list.map { it.order.toDomain(it.items) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun submitOrder(
        table: String,
        payment: String,
        note: String,
        items: List<OrderItem>
    ) {
        if (items.isEmpty()) return
        val id = buildOrderId()
        val createdAt = LocalDateTime.now()
        val total = items.sumOf { it.subtotal }
        val entity = OrderEntity(id, createdAt, table, total, payment, note, "active")
        viewModelScope.launch {
            orderDao.insertOrder(entity)
            orderDao.insertOrderItems(
                items.map { OrderItemEntity(id, it.menuItemId, it.name, it.quantity, it.price) }
            )
            try {
                postToSheets(entity, items)
            } catch (e: Exception) {
                Log.w("OrdersViewModel", "Google Sheets sync failed", e)
            }
            try {
                notifyTelegram(entity, items)
            } catch (e: Exception) {
                Log.w("OrdersViewModel", "Telegram notification failed", e)
            }
        }
    }

    private suspend fun postToSheets(order: OrderEntity, items: List<OrderItem>) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val itemsJson = json.encodeToString(items.map { SheetsItem(it.name, it.quantity, it.price) })
        val row = listOf(
            order.id,
            formatter.format(order.createdAt),
            order.table,
            itemsJson,
            order.total,
            order.payment,
            order.status
        )
        sheetsService.appendOrder(
            SheetsAppendRequest(
                range = sheetsRange,
                values = listOf(row)
            )
        )
    }

    private suspend fun notifyTelegram(order: OrderEntity, items: List<OrderItem>) {
        val lines = items.joinToString("\n") {
            val name = escapeMarkdown(it.name)
            val qty = escapeMarkdown(it.quantity.toString())
            val subtotal = escapeMarkdown(it.subtotal.toString())
            "• $name x$qty = $subtotal грн"
        }
        val tableEscaped = escapeMarkdown(order.table)
        val timeEscaped = escapeMarkdown(DateTimeFormatter.ofPattern("HH:mm").format(order.createdAt))
        val totalEscaped = escapeMarkdown(order.total.toString())
        val paymentEscaped = escapeMarkdown(order.payment)
        val text = """
            🧾 Нове замовлення - "Зірочка"
            Столик: $tableEscaped
            Час: $timeEscaped
            
            Позиції:
            $lines
            
            Загалом: $totalEscaped грн
            Оплата: $paymentEscaped
        """.trimIndent()
        telegramService.sendMessage(
            TelegramMessageRequest(
                chatId = telegramChatId,
                text = text
            )
        )
    }

    private fun buildOrderId(): String {
        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        val suffix = UUID.randomUUID().toString().take(4).uppercase()
        return "ORDER-$stamp-$suffix"
    }

    private fun escapeMarkdown(input: String): String =
        markdownV2Specials.replace(input) { "\\${it.value}" }

    @Serializable
    private data class SheetsItem(val name: String, val qty: Int, val price: Int)

    private val json = Json { encodeDefaults = false }
    private val markdownV2Specials = Regex("""([\\_*`\[\]()~#\+\-=|{}\.!>])""")
}
