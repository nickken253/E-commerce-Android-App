package dev.vstd.shoppingcart.checklist.data

import dev.vstd.shoppingcart.checklist.domain.GroupWithTodos

class TodoRepository(private val todoGroupDao: TodoGroupDao, private val todoDao: TodoItemDao) {
    suspend fun getAllGroups() = todoGroupDao.getAll()
    suspend fun insertGroup(todoGroup: TodoGroup) = todoGroupDao.insert(todoGroup)
    suspend fun getAllTodos() = todoDao.getAll()
    suspend fun insertTodo(todoItem: TodoItem) = todoDao.insert(todoItem)
    private suspend fun getTodosByGroupId(groupId: Long) = todoDao.getByGroupId(groupId)
    suspend fun getGroupById(groupId: Int) = todoGroupDao.getById(groupId)
    suspend fun updateTodoItem(todoItem: TodoItem) = todoDao.update(todoItem)
    suspend fun deleteTodoItem(todoItem: TodoItem) = todoDao.delete(todoItem)
    suspend fun findGroupByTitle(name: String) = todoGroupDao.findByTitle(name)

    suspend fun getAllGroupsWithTodos(): List<GroupWithTodos> {
        val groups = getAllGroups()
        return groups.map { group ->
            GroupWithTodos(group, getTodosByGroupId(group.id))
        }
    }
}