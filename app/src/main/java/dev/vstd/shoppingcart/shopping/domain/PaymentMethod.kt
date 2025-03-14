package dev.vstd.shoppingcart.shopping.domain

import java.io.Serializable

data class PaymentMethod(
    val id: Long,
    val type: Type,
    val textDescription: String,
    val balance: Int,
) : Serializable {
    enum class Type(val imageUrl: String, val title: String) {
        CREDIT_CARD(
            imageUrl = "https://th.bing.com/th/id/OIP.xVREsbEnxpFwYsgl4hNO7QHaDA?rs=1&pid=ImgDetMain",
            title = "Credit Card"
        ),
        MOMO(
            imageUrl = "https://th.bing.com/th/id/OIP.gG0oc_UlphEFcMgmusdb6gHaHa?rs=1&pid=ImgDetMain",
            title = "Momo"
        ),
        COD(
            imageUrl = "https://img0.etsystatic.com/148/0/10872947/il_340x270.1101464894_r87w.jpg",
            title = "Cash On Delivery"
        ),
    }

    companion object {
        fun getDefaultOptions(): List<PaymentMethod> {
            return listOf(
                PaymentMethod(-1, Type.MOMO, "Số dư: 1.832đ", balance = 1832),
                PaymentMethod(-2, Type.COD, "Chuyển khoản khi nhận hàng", balance = 0),
            )
        }
    }
}