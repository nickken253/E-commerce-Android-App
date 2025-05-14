package com.mustfaibra.roffu.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mustfaibra.roffu.sealed.AdvertisementType
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Advertisement(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val title: String,
    val imageUrl: String,
    val advertisedId: Int = 0,
    val type: Int = 0,
) {
    val advertisementType: AdvertisementType
        get() = when(type) {
            0 -> AdvertisementType.Product
            else -> AdvertisementType.Store
        }
}