package com.zirochka.pos.data.repository

import android.content.res.AssetManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.zirochka.pos.data.local.dao.CategoryDao
import com.zirochka.pos.data.local.dao.MenuItemDao
import com.zirochka.pos.data.local.entity.CategoryEntity
import com.zirochka.pos.data.local.entity.MenuItemEntity
import com.zirochka.pos.domain.model.Category
import com.zirochka.pos.domain.model.MenuItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class MenuRepository(
    private val categoryDao: CategoryDao,
    private val menuItemDao: MenuItemDao
) {

    data class MenuSeed(
        @SerializedName("categories") val categories: List<CategorySeed>
    )

    data class CategorySeed(
        val id: Int,
        val name: String,
        val items: List<MenuItemSeed>
    )

    data class MenuItemSeed(
        val id: Int,
        val name: String,
        val description: String,
        val price: Double,
        val available: Boolean = true
    )

    fun observeMenu(): Flow<Pair<List<Category>, List<MenuItem>>> =
        combine(
            categoryDao.observeCategories(),
            menuItemDao.observeMenu()
        ) { categories, items ->
            categories.map { Category(it.id, it.name) } to items.map {
                MenuItem(it.id, it.name, it.description, it.price, it.categoryId, it.available)
            }
        }.distinctUntilChanged()

    suspend fun preloadFromAssets(assets: AssetManager, fileName: String = "menu_zirochka.json") {
        runCatching {
            val json = assets.open(fileName).bufferedReader().use { it.readText() }
            val seed = Gson().fromJson(json, MenuSeed::class.java) ?: return
            categoryDao.clear()
            menuItemDao.clear()
            categoryDao.insertAll(seed.categories.map { CategoryEntity(it.id, it.name) })
            val menuItems = seed.categories.flatMap { category ->
                category.items.map {
                    MenuItemEntity(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        price = it.price,
                        categoryId = category.id,
                        available = it.available
                    )
                }
            }
            menuItemDao.insertAll(menuItems)
        }.onFailure {
            Log.e("MenuRepository", "Не вдалося прочитати $fileName: ${it.message}", it)
        }
    }
}
