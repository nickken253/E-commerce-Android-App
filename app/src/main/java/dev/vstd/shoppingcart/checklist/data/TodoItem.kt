package dev.vstd.shoppingcart.checklist.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val groupId: Long,
    val isCompleted: Boolean = false,
    val createdTime: LocalDateTime
)