package com.mustfaibra.roffu.screens.profile

import com.mustfaibra.roffu.models.dto.BankCardListResponse
import com.mustfaibra.roffu.models.dto.BankCardRequest
import com.mustfaibra.roffu.models.dto.BankCardResponse
import com.mustfaibra.roffu.models.dto.ProcessPaymentRequest
import com.mustfaibra.roffu.models.dto.ProcessPaymentResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Service API để thực hiện các thao tác liên quan đến thanh toán
 */
interface PaymentApiService {
    /**
     * Lưu thông tin thẻ ngân hàng mới
     * @param request Thông tin thẻ cần lưu
     * @param token JWT token để xác thực
     */
    @POST("api/v1/payments/bank-card")
    suspend fun saveBankCard(
        @Body request: BankCardRequest,
        @Header("Authorization") token: String
    ): Response<BankCardResponse>
    
    /**
     * Lấy danh sách thẻ ngân hàng của người dùng
     * @param token JWT token để xác thực
     */
    @GET("api/v1/payments/cards")
    suspend fun getBankCards(
        @Header("Authorization") token: String
    ): Response<List<BankCardListResponse>>
    
    /**
     * Xử lý thanh toán bằng thẻ
     * @param cardId ID của thẻ dùng để thanh toán
     * @param request Thông tin thanh toán
     * @param token JWT token để xác thực
     */
    @POST("api/v1/payments/process-payment/card/{card_id}")
    suspend fun processCardPayment(
        @Path("card_id") cardId: Int,
        @Body request: ProcessPaymentRequest,
        @Header("Authorization") token: String
    ): Response<ProcessPaymentResponse>
}
