package dev.vstd.shoppingcart.common

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.vstd.shoppingcart.auth.data.UserRepository
import dev.vstd.shoppingcart.auth.service.UserService
import dev.vstd.shoppingcart.checklist.data.AppDatabase
import dev.vstd.shoppingcart.checklist.data.BarcodeRepository
import dev.vstd.shoppingcart.checklist.data.TodoRepository
import dev.vstd.shoppingcart.shopping.data.repository.CardRepository
import dev.vstd.shoppingcart.shopping.data.repository.OrderRepository
import dev.vstd.shoppingcart.shopping.data.repository.ProductRepository
import dev.vstd.shoppingcart.shopping.data.service.CardService
import dev.vstd.shoppingcart.shopping.data.service.OrderService
import dev.vstd.shoppingcart.shopping.data.service.ProductService
import okhttp3.Cache
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.createDatabase(context)
    }

    @Provides
    @Singleton
    fun providesTodoRepository(appDatabase: AppDatabase): TodoRepository {
        return TodoRepository(appDatabase.todoGroupDao, appDatabase.todoItemDao)
    }

    @Provides
    @Singleton
    fun providesBarcodeRepository(appDatabase: AppDatabase): BarcodeRepository {
        return BarcodeRepository(appDatabase.barcodeItemDao)
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cache = context.cacheDir
        return OkHttpClient.Builder()
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor {
                val request = it.request()
                val response = it.proceed(request)
                Timber.d("Request: ${request.url}")
                Timber.d("Response:${response.code}\n${response.peekBody(Long.MAX_VALUE).string()}")
                response
            }
            .cache(Cache(cache, 10 * 1024 * 1024))
            .build()
    }

    @Provides
    @Singleton
    fun pvUserService(okHttpClient: OkHttpClient): UserService {
        return UserService.create(okHttpClient)
    }

    @Provides
    @Singleton
    fun pvCardService(okHttpClient: OkHttpClient): CardService {
        return CardService.create(okHttpClient)
    }

    @Provides
    @Singleton
    fun pvOrderService(okHttpClient: OkHttpClient): OrderService {
        return OrderService.create(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userService: UserService, cardService: CardService): UserRepository {
        return UserRepository(userService, cardService)
    }

    @Provides
    @Singleton
    fun pvOrderRepository(orderService: OrderService): OrderRepository {
        return OrderRepository(orderService)
    }

    @Provides
    @Singleton
    fun pvProductService(okHttpClient: OkHttpClient): ProductService {
        return ProductService.create(okHttpClient)
    }

    @Provides
    @Singleton
    fun pvProductRepository(productService: ProductService): ProductRepository {
        return ProductRepository(productService)
    }

    @Provides
    @Singleton
    fun pvCardRepository(cardService: CardService): CardRepository {
        return CardRepository(cardService)
    }
}