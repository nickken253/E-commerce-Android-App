package dev.vstd.shoppingcart.shopping.data.repository

import dev.vstd.shoppingcart.auth.Session
import dev.vstd.shoppingcart.shopping.data.service.CreateOrderBodyDto
import dev.vstd.shoppingcart.shopping.data.service.OrderRespDto
import dev.vstd.shoppingcart.shopping.data.service.OrderService
import retrofit2.Response

class OrderRepository(private val orderService: OrderService) {
    suspend fun getAllOrders(): Response<List<OrderRespDto>> {
        val userId = Session.userEntity.value!!.id
        return orderService.getOrders(userId)
    }
    suspend fun createOrder(body: CreateOrderBodyDto): Response<OrderRespDto> {
        val userId = Session.userEntity.value!!.id
        return orderService.makeOrder(userId, body)
    }
}