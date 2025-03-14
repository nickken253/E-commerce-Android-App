package dev.vstd.shoppingcart.shopping.data.repository

import dev.vstd.shoppingcart.auth.Session
import dev.vstd.shoppingcart.shopping.data.service.CardRespDto
import dev.vstd.shoppingcart.shopping.data.service.CardService
import retrofit2.Response

class CardRepository(private val cardService: CardService) {

    suspend fun getCard(): Response<CardRespDto> {
        val userId = Session.userEntity.value!!.id
        return cardService.getCard(userId)
    }

    suspend fun registerCard(): Response<CardRespDto> {
        val userId = Session.userEntity.value!!.id
        return cardService.registerCard(userId)
    }

    suspend fun payByCard(orderId: Long, cvv: String): Response<String> {
        val userId = Session.userEntity.value!!.id
        return cardService.payByCard(userId, orderId, cvv)
    }
}