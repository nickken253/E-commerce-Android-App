package dev.vstd.shoppingcart.shopping.data.service

data class CardRespDto(
    val id: Long = 0,
    val cardNumber: String,
    val expirationDate: String,
    val cvv: String,
    val balance: Int,
)