package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun insertTask(task: Task): Long {
        val maxOrder = taskDao.getMaxOrderIndex() ?: -1
        val taskWithOrder = if (task.orderIndex == 0) {
            task.copy(orderIndex = maxOrder + 1)
        } else {
            task
        }
        return taskDao.insertTask(taskWithOrder)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun updateTasks(tasks: List<Task>) {
        taskDao.updateTasks(tasks)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedTasks()
    }
}
