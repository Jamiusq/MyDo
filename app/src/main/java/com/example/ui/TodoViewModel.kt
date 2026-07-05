package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TaskRepository) : ViewModel() {

    // All tasks from the repository (reactive)
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search and filter query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<Int?>(null) // null = All, 0 = Low, 1 = Medium, 2 = High
    val selectedPriorityFilter = _selectedPriorityFilter.asStateFlow()

    private val _hideCompleted = MutableStateFlow(false)
    val hideCompleted = _hideCompleted.asStateFlow()

    // Combined filtered tasks
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        searchQuery,
        selectedPriorityFilter,
        hideCompleted
    ) { tasks, query, priority, hideDone ->
        tasks.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
            val matchesPriority = priority == null || task.priority == priority
            val matchesCompletion = !hideDone || !task.isCompleted
            matchesQuery && matchesPriority && matchesCompletion
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setPriorityFilter(priority: Int?) {
        _selectedPriorityFilter.value = priority
    }

    fun toggleHideCompleted() {
        _hideCompleted.value = !_hideCompleted.value
    }

    // CRUD operations
    fun addTask(title: String, description: String, priority: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTask = Task(
                title = title,
                description = description,
                priority = priority,
                isCompleted = false
            )
            repository.insertTask(newTask)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun updateTaskDetails(task: Task, newTitle: String, newDescription: String, newPriority: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(
                task.copy(
                    title = newTitle,
                    description = newDescription,
                    priority = newPriority
                )
            )
        }
    }

    fun updateTaskPriorityDirectly(task: Task, newPriority: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task.copy(priority = newPriority))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCompletedTasks()
        }
    }

    // Reorder lists (drag-and-drop mechanism)
    fun reorderTasks(fromIndex: Int, toIndex: Int) {
        val currentList = allTasks.value
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val updatedList = currentList.toMutableList()
            val movedItem = updatedList.removeAt(fromIndex)
            updatedList.add(toIndex, movedItem)

            // Reassign sequence indexes sequentially
            val withNewOrder = updatedList.mapIndexed { index, task ->
                task.copy(orderIndex = index)
            }

            viewModelScope.launch(Dispatchers.IO) {
                repository.updateTasks(withNewOrder)
            }
        }
    }

    // Explicit position move buttons (perfect for accessible/tap prioritization fallback)
    fun moveTaskUp(task: Task) {
        val currentList = allTasks.value
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index > 0) {
            reorderTasks(index, index - 1)
        }
    }

    fun moveTaskDown(task: Task) {
        val currentList = allTasks.value
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index != -1 && index < currentList.size - 1) {
            reorderTasks(index, index + 1)
        }
    }
}

class TodoViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
