package com.xdien.todoevent.data.repository

import com.xdien.todoevent.data.api.TodoApiService
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    private val todoApiService: TodoApiService
) {
    
    fun getAllTodos(): Flow<List<TodoEntity>> {
        return todoDao.getAllTodos()
    }
    
    suspend fun getTodoByIdSuspend(id: Long): TodoEntity? {
        return todoDao.getTodoByIdSuspend(id)
    }
    
    fun getTodoById(id: Long): Flow<TodoEntity?> {
        return todoDao.getTodoById(id)
    }
    
    suspend fun insertTodo(todo: TodoEntity): Long {
        return todoDao.insertTodo(todo)
    }
    
    suspend fun updateTodo(todo: TodoEntity) {
        todoDao.updateTodo(todo)
    }
    
    suspend fun deleteTodo(todo: TodoEntity) {
        todoDao.deleteTodo(todo)
    }
    
    suspend fun deleteTodoById(id: Long) {
        todoDao.deleteTodoById(id)
    }
    
    // API methods
    suspend fun fetchTodosFromApi(): List<TodoEntity> {
        return try {
            val apiTodos = todoApiService.getTodos()
            apiTodos.map { apiTodo ->
                TodoEntity(
                    id = apiTodo.id,
                    title = apiTodo.title,
                    description = apiTodo.description,
                    isCompleted = apiTodo.isCompleted,
                    createdAt = apiTodo.createdAt.toLongOrNull() ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
} 