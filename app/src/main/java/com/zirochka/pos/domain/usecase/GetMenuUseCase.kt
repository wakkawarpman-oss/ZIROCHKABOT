package com.zirochka.pos.domain.usecase

import com.zirochka.pos.data.repository.MenuRepository
import com.zirochka.pos.domain.model.Category
import com.zirochka.pos.domain.model.MenuItem
import kotlinx.coroutines.flow.Flow

class GetMenuUseCase(
    private val repository: MenuRepository
) {
    operator fun invoke(): Flow<Pair<List<Category>, List<MenuItem>>> = repository.observeMenu()
}
