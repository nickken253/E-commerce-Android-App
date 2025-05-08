package com.mustfaibra.roffu.repositories

import com.mustfaibra.roffu.data.local.RoomDao
import com.mustfaibra.roffu.models.BookmarkItem
import com.mustfaibra.roffu.models.CartItem
import com.mustfaibra.roffu.models.Manufacturer
import com.mustfaibra.roffu.models.Order
import com.mustfaibra.roffu.models.OrderDetails
import com.mustfaibra.roffu.models.OrderItem
import com.mustfaibra.roffu.models.OrderPayment
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
import java.util.*
import javax.inject.Inject

class ProductsRepository @Inject constructor(
    private val dao: RoomDao,
    private val client: HttpClient,
    private val json: Json,
) {

    suspend fun updateCartState(productId: Int, alreadyOnCart: Boolean) {
        /** Handle the local storing process */
        handleLocalCart(productId = productId, alreadyOnCart = alreadyOnCart)
        /** Handle the remote process */
    }
    suspend fun toggleBookmark(productId: Int) {
        val alreadyOnBookmark = dao.isProductBookmarked(productId)
        updateBookmarkState(productId, alreadyOnBookmark)
    }


    private suspend fun handleLocalCart(productId: Int, alreadyOnCart: Boolean) {
        if (alreadyOnCart) {
            /** Already on local , delete it */
            dao.deleteCartItem(productId = productId)
        } else {
            /** not on local , add it */
            val cartItem = CartItem(
                productId = productId,
                quantity = 1,
            )
            addToLocalCart(cartItem = cartItem)
        }
    }

    suspend fun getProductByBarcode(barcode: String): com.mustfaibra.roffu.models.dto.Product? {
        return try {
            val response: HttpResponse = client.get("http://34.9.68.100:8000/api/v1/products/barcode/$barcode") {
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
            val response = client.get("http://34.9.68.100:8000/api/v1/products/$productId") {
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
    private suspend fun addToLocalCart(cartItem: CartItem) {
        /** Add it to cart items */
        dao.insertCartItem(cartItem = cartItem)
    }

    suspend fun updateCartItemQuantity(id: Int, quantity: Int) {
        /** Update local cart item quantity */
        dao.updateCartItemQuantity(productId = id, quantity = quantity)
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
    fun getManufacturers(): Flow<List<Manufacturer>> {
        return dao.getAllManufacturers()
    }
    suspend fun addManufacturer(manufacturer: Manufacturer) {
        dao.insertManufacturer(manufacturer)
    }
}
