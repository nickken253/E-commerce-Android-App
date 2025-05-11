package com.mustfaibra.roffu.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "virtual_cards")
data class VirtualCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val cardNumber: String,
    val expiryMonth: String,
    val expiryYear: String,
    val cvv: String,
    val cardHolder: String
) : Serializable
