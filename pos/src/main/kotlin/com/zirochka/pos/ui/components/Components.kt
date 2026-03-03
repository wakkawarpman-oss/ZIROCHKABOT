package com.zirochka.pos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.zirochka.pos.data.model.Category
import com.zirochka.pos.data.model.MenuItem
import com.zirochka.pos.data.model.OrderItem

@Composable
fun CategoryList(
    categories: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        items(categories.size) { index ->
            val name = categories[index]
            val isSelected = name == selected
            val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(name) }
                    .background(bg)
                    .padding(16.dp),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun MenuGrid(
    items: List<MenuItem>,
    onAdd: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        modifier = modifier
    ) {
        items(items.size) { idx ->
            val item = items[idx]
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onAdd(item) },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    Text(item.description, style = MaterialTheme.typography.bodyMedium)
                    Text("${item.price} грн", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CartPanel(
    items: List<OrderItem>,
    total: Int,
    onInc: (OrderItem) -> Unit,
    onDec: (OrderItem) -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items.size) { idx ->
                val item = items[idx]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("${item.price} грн x ${item.quantity}")
                    }
                    Row {
                        IconButton(onClick = { onDec(item) }) { Text("-") }
                        Text("${item.quantity}", modifier = Modifier.padding(horizontal = 6.dp))
                        IconButton(onClick = { onInc(item) }) { Text("+") }
                    }
                }
                HorizontalDivider()
            }
        }
        Spacer(modifier = Modifier.padding(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Разом: $total грн", fontWeight = FontWeight.Bold)
            Button(onClick = onCheckout, enabled = items.isNotEmpty()) {
                Text("Підтвердити")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfirmationDialog(
    open: Boolean,
    summary: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!open) return
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Підтвердити замовлення") },
        text = { Text(summary) },
        confirmButton = { Button(onClick = onConfirm) { Text("Відправити") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScaffold(
    categories: List<Category>,
    selectedCategory: String?,
    cartItems: List<OrderItem>,
    total: Int,
    onSelectCategory: (String) -> Unit,
    onAddToCart: (MenuItem) -> Unit,
    onInc: (OrderItem) -> Unit,
    onDec: (OrderItem) -> Unit,
    onCheckout: () -> Unit
) {
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                CategoryList(
                    categories = categories.map { it.name },
                    selected = selectedCategory,
                    onSelect = {
                        onSelectCategory(it)
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Зірочка POS") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }) { Icon(Icons.Default.Menu, contentDescription = "Меню категорій") }
                    }
                )
            }
        ) { padding ->
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                CategoryList(
                    categories = categories.map { it.name },
                    selected = selectedCategory,
                    onSelect = onSelectCategory,
                    modifier = Modifier.weight(0.25f)
                )
                val items = categories.firstOrNull { it.name == selectedCategory }?.items.orEmpty()
                MenuGrid(
                    items = items,
                    onAdd = onAddToCart,
                    modifier = Modifier.weight(0.55f)
                )
                CartPanel(
                    items = cartItems,
                    total = total,
                    onInc = onInc,
                    onDec = onDec,
                    onCheckout = onCheckout,
                    modifier = Modifier.weight(0.2f)
                )
            }
        }
    }
}
