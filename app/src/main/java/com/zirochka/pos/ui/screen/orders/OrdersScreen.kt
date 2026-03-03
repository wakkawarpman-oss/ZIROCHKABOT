package com.zirochka.pos.ui.screen.orders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrdersScreen(
    viewModel: OrdersViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()
    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn {
            items(orders) { order ->
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Замовлення #${order.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Статус: ${order.status} | Сума: ${"%.2f".format(order.total)} грн"
                    )
                }
            }
        }
    }
}
