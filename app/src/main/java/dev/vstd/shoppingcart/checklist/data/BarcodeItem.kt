package dev.vstd.shoppingcart.checklist.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BarcodeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val barcode: String,

    val name: String,
    val note: String,
    val timestampAdded: Long = System.currentTimeMillis(),
)