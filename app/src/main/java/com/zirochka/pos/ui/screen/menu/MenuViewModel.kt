package com.zirochka.pos.ui.screen.menu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirochka.pos.data.repository.MenuRepository
import com.zirochka.pos.domain.model.Category
import com.zirochka.pos.domain.model.MenuItem
import com.zirochka.pos.domain.usecase.GetMenuUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val getMenuUseCase: GetMenuUseCase,
    private val repository: MenuRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<MenuItem>>(emptyList())
    val items: StateFlow<List<MenuItem>> = _items.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    init {
        viewModelScope.launch {
            repository.preloadFromAssets(appContext.assets)
            getMenuUseCase().collect { (categories, items) ->
                _categories.value = categories
                _items.value = items
                if (_selectedCategoryId.value == null && categories.isNotEmpty()) {
                    _selectedCategoryId.value = categories.first().id
                }
            }
        }
    }

    fun selectCategory(id: Int) {
        _selectedCategoryId.value = id
    }

    fun filteredItems(): List<MenuItem> =
        _items.value.filter { item ->
            _selectedCategoryId.value?.let { item.categoryId == it } ?: true
        }

    fun refreshMenu() {
        viewModelScope.launch {
            repository.preloadFromAssets(appContext.assets)
        }
    }
}
