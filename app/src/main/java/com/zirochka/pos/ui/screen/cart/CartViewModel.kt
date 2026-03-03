package com.zirochka.pos.ui.screen.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirochka.pos.domain.model.MenuItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CartViewModel @Inject constructor() : ViewModel() {

    private val _items = MutableStateFlow<Map<MenuItem, Int>>(emptyMap())
    val items: StateFlow<Map<MenuItem, Int>> = _items

    val total: StateFlow<Double> = _items
        .map { state -> state.entries.sumOf { it.key.price * it.value } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    fun add(item: MenuItem) {
        _items.value = _items.value.toMutableMap().apply {
            put(item, (get(item) ?: 0) + 1)
        }
    }

    fun decrease(item: MenuItem) {
        _items.value = _items.value.toMutableMap().apply {
            val current = get(item) ?: return
            if (current <= 1) remove(item) else put(item, current - 1)
        }
    }

    fun clear() {
        _items.value = emptyMap()
    }
}
