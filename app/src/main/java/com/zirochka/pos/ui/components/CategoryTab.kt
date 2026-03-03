package com.zirochka.pos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zirochka.pos.domain.model.Category

@Composable
fun CategoryTab(
    category: Category,
    selected: Boolean,
    onSelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = category.name,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onSelected(category) }
    )
}
