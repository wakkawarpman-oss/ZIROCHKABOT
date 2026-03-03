package com.zirochka.pos.di

import android.content.Context
import androidx.room.Room
import com.zirochka.pos.BuildConfig
import com.zirochka.pos.data.local.dao.CategoryDao
import com.zirochka.pos.data.local.dao.MenuItemDao
import com.zirochka.pos.data.local.dao.OrderDao
import com.zirochka.pos.data.local.database.AppDatabase
import com.zirochka.pos.data.remote.api.GoogleSheetsApi
import com.zirochka.pos.data.remote.api.TelegramApi
import com.zirochka.pos.data.repository.MenuRepository
import com.zirochka.pos.data.repository.OrderRepository
import com.zirochka.pos.domain.usecase.CreateOrderUseCase
import com.zirochka.pos.domain.usecase.GetMenuUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "zirochka.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMenuItemDao(db: AppDatabase): MenuItemDao = db.menuItemDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideOrderDao(db: AppDatabase): OrderDao = db.orderDao()

    @Provides
    @Singleton
    fun provideSheetsApi(): GoogleSheetsApi? {
        val url = BuildConfig.SHEETS_SCRIPT_URL
        if (url.isBlank()) return null
        return Retrofit.Builder()
            .baseUrl("https://script.google.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleSheetsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTelegramApi(): TelegramApi? {
        if (BuildConfig.TELEGRAM_BOT_TOKEN.isBlank()) return null
        return Retrofit.Builder()
            .baseUrl("https://api.telegram.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TelegramApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMenuRepository(
        categoryDao: CategoryDao,
        menuItemDao: MenuItemDao
    ): MenuRepository = MenuRepository(categoryDao, menuItemDao)

    @Provides
    @Singleton
    fun provideOrderRepository(
        orderDao: OrderDao,
        sheetsApi: GoogleSheetsApi?,
        telegramApi: TelegramApi?
    ): OrderRepository = OrderRepository(
        orderDao = orderDao,
        sheetsApi = sheetsApi,
        telegramApi = telegramApi,
        scriptUrl = BuildConfig.SHEETS_SCRIPT_URL,
        botToken = BuildConfig.TELEGRAM_BOT_TOKEN,
        chatId = BuildConfig.TELEGRAM_CHAT_ID
    )

    @Provides fun provideGetMenuUseCase(repo: MenuRepository) = GetMenuUseCase(repo)
    @Provides fun provideCreateOrderUseCase(repo: OrderRepository) = CreateOrderUseCase(repo)
}
