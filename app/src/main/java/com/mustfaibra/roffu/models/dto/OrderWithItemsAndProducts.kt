package com.mustfaibra.roffu.models.dto
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class OrderWithItemsAndProducts(
    @SerializedName("id") val orderId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("total_amount") val total: Double,
    @SerializedName("status") val status: String,
    @SerializedName("shipping_address_id") val shippingAddressId: Int?,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("shipping_carrier") val shippingCarrier: String?,
    @SerializedName("tracking_number") val trackingNumber: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("order_date") val orderDate: String,
    @SerializedName("items") var orderItems: List<OrderItem> = emptyList(),
    @SerializedName("shipping_address") val shippingAddress: ShippingAddress?
)