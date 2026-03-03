package com.zirochka.pos.data.repository

import android.util.Log
import com.zirochka.pos.data.local.dao.OrderDao
import com.zirochka.pos.data.local.entity.OrderEntity
import com.zirochka.pos.data.remote.api.GoogleSheetsApi
import com.zirochka.pos.data.remote.api.TelegramApi
import com.zirochka.pos.data.remote.model.SheetsRequest
import com.zirochka.pos.data.remote.model.TelegramRequest
import com.zirochka.pos.domain.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrderRepository(
    private val orderDao: OrderDao,
    private val sheetsApi: GoogleSheetsApi?,
    private val telegramApi: TelegramApi?,
    private val botToken: String,
    private val chatId: String
) {

    fun observeOrders(): Flow<List<Order>> =
        orderDao.observeOrders().map { entities ->
            entities.map { entity ->
                Order(
                    id = entity.id,
                    items = emptyList(),
                    total = entity.total,
                    status = entity.status,
                    createdAt = entity.createdAt
                )
            }
        }

    suspend fun createOrder(order: Order): Long {
        val summary = summarize(order)
        val id = orderDao.insert(
            OrderEntity(
                description = summary,
                total = order.total,
                status = order.status,
                createdAt = order.createdAt
            )
        )
        sendToSheets(id, summary, order.total)
        sendToTelegram(id, summary, order.total)
        return id
    }

    private suspend fun sendToSheets(id: Long, summary: String, total: Double) {
        if (sheetsApi == null) return
        kotlin.runCatching {
            sheetsApi.appendBooking(
                SheetsRequest(
                    username = "POS",
                    message = "Order #$id",
                    orderSummary = summary,
                    total = total
                )
            )
        }.onFailure {
            Log.w("OrderRepository", "Sheets sync failed", it)
        }
    }

    private suspend fun sendToTelegram(id: Long, summary: String, total: Double) {
        if (telegramApi == null || botToken.isBlank() || chatId.isBlank()) return
        kotlin.runCatching {
            telegramApi.sendMessage(
                token = botToken,
                body = TelegramRequest(
                    chat_id = chatId,
                    text = "*Нове замовлення #$id*\n$summary\nРазом: ${"%.2f".format(total)} грн"
                )
            )
        }.onFailure {
            Log.w("OrderRepository", "Telegram sync failed", it)
        }
    }

    private fun summarize(order: Order): String =
        order.items.joinToString(separator = "\n") { line ->
            "• ${line.item.name} ×${line.quantity} — ${"%.2f".format(line.item.price * line.quantity)} грн"
        }
}
