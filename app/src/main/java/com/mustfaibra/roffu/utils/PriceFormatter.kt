package com.mustfaibra.roffu.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Tiện ích để định dạng giá tiền
 */
object PriceFormatter {
    /**
     * Định dạng giá tiền với dấu phân cách hàng nghìn
     * @param price Giá tiền cần định dạng
     * @return Chuỗi giá tiền đã định dạng
     */
    fun formatPrice(price: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        return formatter.format(price.toInt())
    }
}
