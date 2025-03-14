package dev.vstd.shoppingcart.checklist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
)