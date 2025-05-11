package com.mustfaibra.roffu.models.dto

<<<<<<< HEAD
=======

>>>>>>> hieuluu2
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Image(
    val id: Int,
    val product_id: Int,
    val image_url: String,
    val is_primary: Boolean,
    val upload_date: String
)