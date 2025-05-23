package com.mustfaibra.roffu.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val image: Int,
    val price: Double,
    val description: String,
    val imagePath: String? = null,
    val manufacturerId: Int,
    val basicColorName: String,
    val barcode: String,
    // --- Bổ sung thuộc tính phân loại và size ---
    val type: String = "", // Loại (color, design, ...)
    val size: String = ""  // Size (41, 42, L, XL, ...)
) {
    @Ignore
    var manufacturer: Manufacturer? = null
    @Ignore
    var colors: List<ProductColor>? = null
    @Ignore
    var reviews: List<Review>? = null
    @Ignore
    var sizes: List<ProductSize>? = null
}
