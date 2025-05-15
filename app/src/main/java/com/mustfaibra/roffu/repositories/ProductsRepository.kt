package com.mustfaibra.roffu.repositories

import android.content.Context
import retrofit2.HttpException

import android.os.Build
import androidx.annotation.RequiresExtension
import com.mustfaibra.roffu.api.RetrofitClient
import com.mustfaibra.roffu.data.local.RoomDao
import com.mustfaibra.roffu.models.BookmarkItem
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.models.Order
import com.mustfaibra.roffu.models.OrderDetails
import com.mustfaibra.roffu.models.OrderItem
import com.mustfaibra.roffu.models.OrderPayment
import com.mustfaibra.roffu.models.OrderWithItemsAndProducts
import com.mustfaibra.roffu.models.Product
import com.mustfaibra.roffu.sealed.DataResponse
import com.mustfaibra.roffu.sealed.Error
import com.mustfaibra.roffu.utils.UserPref
import com.mustfaibra.roffu.utils.getFormattedDate
import com.mustfaibra.roffu.utils.getStructuredProducts
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

import kotlinx.serialization.json.Json
import java.io.IOException

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductsRepository @Inject constructor(
    private val dao: RoomDao,

    private val client: HttpClient,
    private val json: Json,

    private val context: Context

) {
    suspend fun updateCartState(productId: Int, size: String, color: String) {
        // Lấy tất cả item trong cart hiện tại
        val cartItems = dao.getCartItemsNow()
        val existing = cartItems.find {
            it.productId == productId
        }
        if (existing != null) {
            // Nếu đã có biến thể này, tăng số lượng
            dao.updateCartItemQuantity(existing.cartId!!, existing.quantity + 1)
        } else {
            // Nếu chưa có, thêm mới
            val cartItem = CartItem(
                productId = productId,
                quantity = 1
            )
            dao.insertCartItem(cartItem)
        }
    }



    suspend fun getProductByBarcode(barcode: String): com.mustfaibra.roffu.models.dto.Product? {
        return try {
            val response: HttpResponse = client.get("http://103.90.226.131:8000/api/v1/products/barcode/$barcode") {
                header("accept", "application/json")
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<com.mustfaibra.roffu.models.dto.Product>()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun getProductDetails(productId: Int): DataResponse<com.mustfaibra.roffu.models.dto.Product> {
        return try {
            val response = client.get("http://103.90.226.131:8000/api/v1/products/$productId") {
                header("accept", "application/json")
            }
            if (response.status == HttpStatusCode.OK) {
                DataResponse.Success(response.body<com.mustfaibra.roffu.models.dto.Product>())
            } else {
                DataResponse.Error(com.mustfaibra.roffu.sealed.Error.Network)
            }
        } catch (e: Exception) {
            DataResponse.Error(com.mustfaibra.roffu.sealed.Error.Network)
        }
    }

    suspend fun toggleBookmark(productId: Int) {
        val alreadyOnBookmark = dao.isProductBookmarked(productId)
        updateBookmarkState(productId, alreadyOnBookmark)
    }




    suspend fun saveOrders(
        items: List<CartItem>,
        providerId: String?,
        total: Double,
        deliveryAddressId: Int?,
        onFinished: () -> Unit,
    ) {
        UserPref.user.value?.let {
            val order = Order(
                orderId = Date().getFormattedDate("yyyyMMddHHmmSS"),
                userId = it.userId,
                total = total,
                locationId = deliveryAddressId,
            )
            val orderPayment = OrderPayment(
                orderId = order.orderId,
                providerId = providerId,
            )

            /** Fake the success of delivering the previous order */
            dao.updateOrdersAsDelivered()

            dao.insertOrder(order = order)
            dao.insertOrderPayment(payment = orderPayment)
            dao.insertOrderItems(
                items = items.map { cartItem ->
                    OrderItem(
                        orderId = order.orderId,
                        quantity = cartItem.quantity,
                        productId = cartItem.productId,
                        userId = it.userId,
                    )
                },
            )
            /** Then clear our cart */
            dao.clearCart()
            onFinished()
        }
    }

    fun getCartProductsIdsFlow() = dao.getCartProductsIds()

    fun getLocalCart() = dao.getCartItems()

    suspend fun clearCart() {
        dao.clearCart()
    }

    fun getBookmarksProductsIdsFlow() = dao.getBookmarkProductsIds()

    suspend fun updateBookmarkState(productId: Int, alreadyOnBookmark: Boolean) {
        /** Handle the local storing process */
        handleLocalBookmark(productId = productId, alreadyOnBookmark = alreadyOnBookmark)
        /** Handle the remote process */
    }

    private suspend fun handleLocalBookmark(productId: Int, alreadyOnBookmark: Boolean) {
        if (alreadyOnBookmark) {
            /** Already on local , delete it */
            dao.deleteBookmarkItem(productId = productId)
        } else {
            /** Add it to bookmark items */
            dao.insertBookmarkItem(bookmarkItem = BookmarkItem(productId = productId))
        }
    }

    fun getLocalBookmarks() = dao.getBookmarkItems()



    suspend fun getOrdersHistory(): DataResponse<List<OrderDetails>> {
        /** Check the local storage */
        dao.getLocalOrders().let {
            if (it.isNotEmpty())
                return DataResponse.Success(data = it)
        }
        /** Doesn't exist on the local, check remote */
        return DataResponse.Error(error = Error.Empty)
    }

    suspend fun getProductResponseDetails(productId: Int): DataResponse<com.mustfaibra.roffu.models.ProductResponse> {
        return try {
            val token = UserPref.getToken(context)
            if (token == null) {
                return DataResponse.Error(error = Error.Unknown)
            }

            // Set token cho RetrofitClient
            RetrofitClient.init(token)

            val response = RetrofitClient.apiService.getProductDetails(productId)

            if (response.isSuccessful && response.body() != null) {
                DataResponse.Success(data = response.body()!!)
            } else {
                DataResponse.Error(error = Error.Empty)
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> DataResponse.Error(error = Error.Unauthorized)
                403 -> DataResponse.Error(error = Error.Forbidden)
                else -> DataResponse.Error(error = Error.Network)
            }
        } catch (e: IOException) {
            DataResponse.Error(error = Error.Network)
        } catch (e: Exception) {
            DataResponse.Error(error = Error.Unknown)
        }
    }

    suspend fun getOrderItems(orderId: Int): DataResponse<List<com.mustfaibra.roffu.models.dto.OrderItem>> {
        return try {
            val token = UserPref.getToken(context)
            if (token == null) {
                return DataResponse.Error(error = Error.Unknown)
            }

            // Set token cho RetrofitClient
            RetrofitClient.init(token)

            val response = RetrofitClient.orderApiService.getOrderItems(orderId)

            if (response.isNotEmpty()) {
                DataResponse.Success(data = response)
            } else {
                DataResponse.Error(error = Error.Empty)
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> DataResponse.Error(error = Error.Unauthorized)
                403 -> DataResponse.Error(error = Error.Forbidden)
                else -> DataResponse.Error(error = Error.Network)
            }
        } catch (e: IOException) {
            DataResponse.Error(error = Error.Network)
        } catch (e: Exception) {
            DataResponse.Error(error = Error.Unknown)
        }
    }

    suspend fun getOrdersWithProducts(): DataResponse<List<com.mustfaibra.roffu.models.dto.OrderWithItemsAndProducts>> {
        return try {
            val token = UserPref.getToken(context)
            if (token == null) {
                return DataResponse.Error(error = Error.Unknown)
            }

            // Set token cho RetrofitClient
            RetrofitClient.init(token)

            val response = RetrofitClient.orderApiService.getOrders(
                page = 1,
                limit = 10
            )

            if (response.isNotEmpty()) {
                DataResponse.Success(data = response)
            } else {
                DataResponse.Error(error = Error.Empty)
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> DataResponse.Error(error = Error.Unauthorized)
                403 -> DataResponse.Error(error = Error.Forbidden)
                else -> DataResponse.Error(error = Error.Network)
            }
        } catch (e: IOException) {
            DataResponse.Error(error = Error.Network)
        } catch (e: Exception) {
            DataResponse.Error(error = Error.Unknown)
        }
    }

    fun getManufacturers(): Flow<List<Manufacturer>> {
        return dao.getAllManufacturers()
    }

    suspend fun addManufacturer(manufacturer: Manufacturer) {
        dao.insertManufacturer(manufacturer)
    }

    // TODO: Thay thế bằng API call thực tế
    suspend fun getProductById(productId: Int): Product? {
        return withContext(Dispatchers.IO) {
            dao.getProductDetails(productId)?.getStructuredProducts()
        }
    }

    suspend fun getAllProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            val products = dao.getAllProducts()
            if (products is Iterable<*>) {
                products.mapNotNull { it as? Product }
            } else {
                emptyList()
            }
        }
    }
}
