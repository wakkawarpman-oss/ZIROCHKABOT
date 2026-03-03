package com.zirochka.pos.ui.screen.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirochka.pos.data.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: MenuRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    private val _lastSync = MutableStateFlow("Немає")
    val lastSync: StateFlow<String> = _lastSync.asStateFlow()

    fun syncMenu() {
        viewModelScope.launch {
            _syncing.value = true
            repository.preloadFromAssets(appContext.assets)
            delay(300)
            _syncing.value = false
            _lastSync.value = "Оновлено щойно"
        }
    }
}
