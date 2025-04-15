package com.mustfaibra.roffu.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int,
    val name: String,
    val profile: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val password: String? = null,
    val gender: Int? = 1,
    val token: String? = null,
    @ColumnInfo(defaultValue = "'user'")
    val role: String = "user", // <-- quan trọng     // Vai trò: admin hoặc user
    val city: String? = null,        // Thành phố
    val district: String? = null,    // Quận/Huyện
    val address: String? = null      // Số nhà
)
{
    fun isAdmin(): Boolean {
        return role == "admin"
    }
}
