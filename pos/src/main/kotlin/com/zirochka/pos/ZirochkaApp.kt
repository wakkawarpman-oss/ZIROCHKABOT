package com.zirochka.pos

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zirochka.pos.data.model.Category
import com.zirochka.pos.ui.components.PosScaffold
import com.zirochka.pos.ui.viewmodel.CartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Composable
fun PosApp(
    categoriesLoader: suspend () -> List<Category>,
    cartViewModel: CartViewModel = viewModel(),
    onCheckout: () -> Unit = {}
) {
    val cartItems by cartViewModel.items.collectAsState()
    val total by cartViewModel.total.collectAsState()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selected by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val loaded = categoriesLoader()
        categories = loaded
        selected = loaded.firstOrNull()?.name
    }

    PosScaffold(
        categories = categories,
        selectedCategory = selected,
        cartItems = cartItems,
        total = total,
        onSelectCategory = { selected = it },
        onAddToCart = cartViewModel::add,
        onInc = cartViewModel::inc,
        onDec = cartViewModel::dec,
        onCheckout = onCheckout
    )
}

suspend fun loadCategoriesFromJson(menuFile: File): List<Category> = withContext(Dispatchers.IO) {
    runCatching {
        val text = menuFile.readText()
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString<MenuRoot>(text).categories
    }.onFailure { Log.w("PosApp", "Failed to load menu JSON", it) }
        .getOrDefault(emptyList())
}

@kotlinx.serialization.Serializable
data class MenuRoot(val categories: List<Category>)
