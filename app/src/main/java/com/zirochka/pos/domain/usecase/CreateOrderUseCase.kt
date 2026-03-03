package com.zirochka.pos.domain.usecase

import com.zirochka.pos.data.repository.OrderRepository
import com.zirochka.pos.domain.model.Order

class CreateOrderUseCase(
    private val repository: OrderRepository
) {
    suspend operator fun invoke(order: Order) = repository.createOrder(order)
}
