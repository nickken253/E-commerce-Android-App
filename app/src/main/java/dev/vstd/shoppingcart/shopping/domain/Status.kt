package dev.vstd.shoppingcart.shopping.domain

enum class Status(val displayName: String) {
    PENDING("Đang duyệt"),
    SHIPPED("Đang vận chuyển"),
    DELIVERED("Đã giao"),
    CANCELLED("Đã hủy"),
}