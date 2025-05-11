package com.mustfaibra.roffu.screens.cart

import com.mustfaibra.roffu.models.dto.CartResponse
import retrofit2.Response
import retrofit2.http.*

interface CartApiService {
    /**
     * Lấy thông tin giỏ hàng của người dùng
     * Yêu cầu xác thực với JWT token
     */
    @GET("api/v1/carts")
    suspend fun getCart(@Header("Authorization") token: String): Response<CartResponse>
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * @param itemId ID của item trong giỏ hàng
     * @param quantityRequest Dữ liệu số lượng mới trong body
     * @param token JWT token để xác thực
     */
    @PUT("api/v1/carts/items/{itemId}")
    suspend fun updateCartItemQuantity(
        @Path("itemId") itemId: Int,
        @Body quantityRequest: Map<String, Int>,
        @Header("Authorization") token: String
    ): Response<CartResponse>
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * @param itemId ID của item trong giỏ hàng
     * @param token JWT token để xác thực
     */
    @DELETE("api/v1/carts/items/{itemId}")
    suspend fun deleteCartItem(
        @Path("itemId") itemId: Int,
        @Header("Authorization") token: String
    ): Response<CartResponse>
}
