package dev.vstd.shoppingcart.pricecompare

import java.text.NumberFormat
import java.util.Locale

fun Number.toVietnameseCurrencyFormat(): String {
    val vietnameseLocale = Locale("vi", "VN")
    val formatter = NumberFormat.getCurrencyInstance(vietnameseLocale)
    return formatter.format(this)
}