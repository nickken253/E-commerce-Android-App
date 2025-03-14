package dev.vstd.shoppingcart.checklist.domain

import dev.vstd.shoppingcart.checklist.data.TodoGroup
import dev.vstd.shoppingcart.checklist.data.TodoItem

class GroupWithTodos(
    val group: TodoGroup,
    val todos: List<TodoItem>
    )