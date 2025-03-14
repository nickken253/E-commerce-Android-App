package dev.vstd.shoppingcart.shopping.data.service

import dev.vstd.shoppingcart.shopping.domain.PaymentMethod

data class CreateOrderBodyDto(
    val products: List<ProductOfOrderDto>,
    val address: String,
    val purchaseMethod: PurchaseMethod,
    val purchaseMethodId: Long?
) {
    enum class PurchaseMethod {
        COD,
        CARD,
        OTHER;

        companion object {
            fun fromPaymentMethod(paymentMethod: PaymentMethod): PurchaseMethod {
                return when (paymentMethod.type) {
                    PaymentMethod.Type.COD -> COD
                    PaymentMethod.Type.CREDIT_CARD -> CARD
                    PaymentMethod.Type.MOMO -> OTHER
                }
            }
        }
    }
    class ProductOfOrderDto(
        val productId: Long,
        val quantity: Int,
    )
}