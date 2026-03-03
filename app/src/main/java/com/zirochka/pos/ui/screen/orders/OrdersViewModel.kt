package com.zirochka.pos.ui.screen.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zirochka.pos.domain.model.Order
import com.zirochka.pos.domain.model.OrderLine
import com.zirochka.pos.domain.usecase.CreateOrderUseCase
import com.zirochka.pos.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrderRepository,
    private val createOrderUseCase: CreateOrderUseCase
) : ViewModel() {
    val orders: StateFlow<List<Order>> = repository.observeOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            while (true) {
                repository.syncPendingOrders()
                delay(30_000)
            }
        }
    }

    suspend fun createOrder(lines: List<OrderLine>, total: Double) {
        createOrderUseCase(
            Order(
                items = lines,
                total = total
            )
        )
    }
}
