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
    private val scriptUrl: String,
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
                status = "pending",
                createdAt = order.createdAt
            )
        )
        syncPendingOrders()
        return id
    }

    suspend fun syncPendingOrders() {
        val pendingOrders = orderDao.getPendingOrders()
        pendingOrders.forEach { pending ->
            val sheetsConfigured = sheetsApi != null && scriptUrl.isNotBlank()
            val telegramConfigured = telegramApi != null && botToken.isNotBlank() && chatId.isNotBlank()

            if (!sheetsConfigured && !telegramConfigured) {
                orderDao.updateStatus(pending.id, "local")
                return@forEach
            }

            val sheetsOk = !sheetsConfigured || sendToSheets(pending.id, pending.description, pending.total)
            val telegramOk = !telegramConfigured || sendToTelegram(pending.id, pending.description, pending.total)

            if (sheetsOk && telegramOk) {
                orderDao.updateStatus(pending.id, "synced")
            } else {
                orderDao.updateStatus(pending.id, "pending")
            }
        }
    }

    private suspend fun sendToSheets(id: Long, summary: String, total: Double): Boolean {
        if (sheetsApi == null || scriptUrl.isBlank()) return false
        return kotlin.runCatching {
            val response = sheetsApi.appendBooking(
                url = scriptUrl,
                request = SheetsRequest(
                    username = "POS",
                    message = "Order #$id",
                    orderSummary = summary,
                    total = total
                )
            )
            response.isSuccessful
        }.onFailure {
            Log.w("OrderRepository", "Sheets sync failed", it)
        }.getOrDefault(false)
    }

    private suspend fun sendToTelegram(id: Long, summary: String, total: Double): Boolean {
        if (telegramApi == null || botToken.isBlank() || chatId.isBlank()) return false
        return kotlin.runCatching {
            val response = telegramApi.sendMessage(
                token = botToken,
                body = TelegramRequest(
                    chat_id = chatId,
                    text = "Нове замовлення #$id\n$summary\nРазом: ${"%.2f".format(total)} грн"
                )
            )
            response.isSuccessful
        }.onFailure {
            Log.w("OrderRepository", "Telegram sync failed", it)
        }.getOrDefault(false)
    }

    private fun summarize(order: Order): String {
        if (order.items.isEmpty()) {
            return "Замовлення без позицій"
        }
        return order.items.joinToString(separator = "\n") { line ->
            "• ${line.item.name} ×${line.quantity} — ${"%.2f".format(line.item.price * line.quantity)} грн"
        }
    }
}
