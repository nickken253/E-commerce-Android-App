package com.mustfaibra.roffu.models

import com.google.gson.annotations.SerializedName

data class Brand(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String
) 