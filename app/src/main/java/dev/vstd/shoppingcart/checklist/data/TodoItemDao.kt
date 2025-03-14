package dev.vstd.shoppingcart.checklist.data

import androidx.room.*

@Dao
interface TodoItemDao {
    @Query("SELECT * FROM todoitem")
    suspend fun getAll(): List<TodoItem>

    @Query("SELECT * FROM todoitem WHERE id = :id")
    suspend fun getById(id: Int): TodoItem

    @Query("SELECT * FROM todoitem WHERE groupId = :categoryId")
    suspend fun getByGroupId(categoryId: Long): List<TodoItem>

    @Insert
    suspend fun insert(todoItem: TodoItem)

    @Update
    suspend fun update(todoItem: TodoItem)

    @Delete
    suspend fun delete(todoItem: TodoItem)
}