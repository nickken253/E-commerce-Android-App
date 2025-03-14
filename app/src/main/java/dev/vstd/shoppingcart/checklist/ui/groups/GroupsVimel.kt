package dev.vstd.shoppingcart.checklist.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.vstd.shoppingcart.checklist.data.TodoGroup
import dev.vstd.shoppingcart.checklist.data.TodoItem
import dev.vstd.shoppingcart.checklist.data.TodoRepository
import dev.vstd.shoppingcart.checklist.domain.GroupWithTodos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsVimel @Inject constructor(private val repository: TodoRepository) : ViewModel() {
    val groupsWithTodos = MutableSharedFlow<List<GroupWithTodos>>()

    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            groupsWithTodos.emit(repository.getAllGroupsWithTodos())
        }
    }

    fun toggleDoneUndone(todo: TodoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodoItem(todo.copy(isCompleted = !todo.isCompleted))
            fetch()
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertGroup(TodoGroup(title = name))
            fetch()
        }
    }
}