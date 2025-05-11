package com.mustfaibra.roffu.repositories

import android.content.Context
import retrofit2.HttpException

import android.os.Build
import androidx.annotation.RequiresExtension
import com.mustfaibra.roffu.RetrofitClient
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import javax.inject.Inject

class ProductsRepository @Inject constructor(
    private val dao: RoomDao,
    private val context: Context
) {

    suspend fun updateCartState(productId: Int, size: String, color: String) {
        // Lấy tất cả item trong cart hiện tại
        val cartItems = dao.getCartItemsNow()
        val existing = cartItems.find {
            it.productId == productId && it.size == size && it.color == color
        }
        if (existing != null) {
            // Nếu đã có biến thể này, tăng số lượng
            dao.updateCartItemQuantity(existing.cartId!!, existing.quantity + 1)
        } else {
            // Nếu chưa có, thêm mới
            val cartItem = CartItem(
                productId = productId,
                quantity = 1,
                size = size,
                color = color
            )
            dao.insertCartItem(cartItem)
        }
    }

    suspend fun toggleBookmark(productId: Int) {
        val alreadyOnBookmark = dao.isProductBookmarked(productId)
        updateBookmarkState(productId, alreadyOnBookmark)
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        return withContext(Dispatchers.IO) {
            dao.getProductByBarcode(barcode)
        }
    }

    suspend fun updateCartItemQuantity(cartId: Int, quantity: Int) {
        /** Update local cart item quantity */
        dao.updateCartItemQuantity(cartId = cartId, quantity = quantity)
    }

    suspend fun deleteCartItemById(cartId: Int) {
        dao.deleteCartItemById(cartId)
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

    suspend fun getProductDetails(productId: Int): DataResponse<Product> {
        /** Check the local storage */
        dao.getProductDetails(productId = productId)?.let {
            return DataResponse.Success(data = it.getStructuredProducts())
        }
        /** Doesn't exist on the local, check remote */
        return DataResponse.Error(error = Error.Network)
    }

    suspend fun getOrdersHistory(): DataResponse<List<OrderDetails>> {
        /** Check the local storage */
        dao.getLocalOrders().let {
            if (it.isNotEmpty())
                return DataResponse.Success(data = it)
        }
        /** Doesn't exist on the local, check remote */
        return DataResponse.Error(error = Error.Empty)
    }

    suspend fun getOrdersWithProducts(): DataResponse<List<com.mustfaibra.roffu.models.dto.OrderWithItemsAndProducts>> {
        return try {
            val token = UserPref.getToken(context)
            if (token == null) {
                return DataResponse.Error(error = Error.Unknown)
            }

            val response = RetrofitClient.orderApiService.getOrders(
                authToken = "Bearer $token",
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
}
