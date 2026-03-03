package com.zirochka.pos.ui.screen.menu

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zirochka.pos.domain.model.MenuItem
import com.zirochka.pos.ui.components.CategoryTab
import com.zirochka.pos.ui.components.MenuItemCard

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onAdd: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    val selected by viewModel.selectedCategoryId.collectAsState()
    val items = viewModel.filteredItems()

    Column(modifier = modifier.fillMaxSize()) {
        if (categories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                categories.forEach { category ->
                    CategoryTab(
                        category = category,
                        selected = category.id == selected,
                        onSelected = { viewModel.selectCategory(it.id) }
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items) { menuItem ->
                MenuItemCard(
                    item = menuItem,
                    onAdd = onAdd,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
    if (items.isEmpty()) {
        Text(
            text = "Меню завантажується...",
            modifier = Modifier.padding(16.dp)
        )
    }
}
