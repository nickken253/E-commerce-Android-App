package dev.vstd.shoppingcart.checklist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TodoGroupDao {
    @Insert
    suspend fun insert(todoGroup: TodoGroup): Long

    @Update
    suspend fun update(todoGroup: TodoGroup)

    @Delete
    suspend fun delete(todoGroup: TodoGroup)

    @Query("SELECT * FROM TodoGroup")
    suspend fun getAll(): List<TodoGroup>

    @Query("SELECT * FROM TodoGroup WHERE id = :id")
    suspend fun getById(id: Int): TodoGroup

    @Query("SELECT * FROM TodoGroup WHERE title = :title")
    suspend fun findByTitle(title: String): TodoGroup?
}