package dev.vstd.shoppingcart.checklist.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(LocalDateTimeConverter::class)
@Database(entities = [
    TodoGroup::class, TodoItem::class, BarcodeItem::class
], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {
    abstract val todoGroupDao: TodoGroupDao
    abstract val todoItemDao: TodoItemDao
    abstract val barcodeItemDao: BarcodeItemDao

    companion object {
        fun createDatabase(context: Context): AppDatabase {
            // synchronized
            return synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "todo.db"
                ).build()
            }
        }
    }
}