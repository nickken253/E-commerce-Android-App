package dev.vstd.shoppingcart.checklist.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeConverter {
    @TypeConverter
    fun ldtToMillisec(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @TypeConverter
    fun millisecToLdt(millisec: Long): LocalDateTime {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(millisec), ZoneOffset.UTC)
    }
}