package com.zirochka.pos.ui.screen.cart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zirochka.pos.domain.model.OrderLine
import com.zirochka.pos.ui.components.CartItem
import com.zirochka.pos.ui.components.OrderConfirmDialog

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onSubmit: (List<OrderLine>, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val cart by viewModel.items.collectAsState()
    val total by viewModel.total.collectAsState(initial = 0.0)
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(cart.toList()) { (item, qty) ->
                CartItem(
                    item = item,
                    quantity = qty,
                    onIncrease = { viewModel.add(item) },
                    onDecrease = { viewModel.decrease(item) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Разом: ${"%.2f".format(total)} грн",
                style = MaterialTheme.typography.titleLarge
            )
            Button(
                onClick = { showDialog = true },
                enabled = cart.isNotEmpty(),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Оформити")
            }
        }
    }
    if (showDialog) {
        OrderConfirmDialog(
            total = total,
            onDismiss = { showDialog = false },
            onConfirm = {
                onSubmit(
                    cart.map { (item, qty) -> OrderLine(item, qty) },
                    total
                )
                viewModel.clear()
                showDialog = false
            }
        )
    }
}
