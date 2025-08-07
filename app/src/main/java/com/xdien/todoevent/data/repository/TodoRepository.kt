package com.xdien.todoevent.data.repository

import com.xdien.todoevent.data.api.TodoApiService
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.entity.TodoEntity
import com.xdien.todoevent.data.entity.TodoWithEventType
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
    
    fun getAllTodosWithEventType(): Flow<List<TodoWithEventType>> {
        return todoDao.getAllTodosWithEventType()
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
            val apiResponse = todoApiService.getTodos()
            
            if (!apiResponse.success) {
                throw Exception(apiResponse.message)
            }
            
            val events = apiResponse.data.events
            events.map { apiTodo ->
                TodoEntity(
                    id = apiTodo.id,
                    title = apiTodo.title,
                    description = apiTodo.description,
                    thumbnailUrl = apiTodo.thumbnailUrl,
                    galleryImages = apiTodo.galleryImages,
                    eventTime = apiTodo.eventTime,
                    eventEndTime = apiTodo.eventEndTime,
                    location = apiTodo.location,
                    eventTypeId = apiTodo.eventType?.toLongOrNull(), // Convert string to Long
                    isCompleted = apiTodo.isCompleted,
                    createdAt = apiTodo.createdAt.toLongOrNull() ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Sync data with server - fetch from API and update local database
     * This method implements the sync strategy:
     * 1. Fetch latest data from server
     * 2. Update local database with server data
     * 3. Return success/failure status
     */
    suspend fun syncWithServer(): SyncResult {
        return try {
            val serverTodos = fetchTodosFromApi()
            
            // Clear existing data and insert new data from server
            // This is a simple sync strategy - in production you might want more sophisticated conflict resolution
            todoDao.deleteAllTodos()
            
            serverTodos.forEach { todo ->
                todoDao.insertTodo(todo)
            }
            
            SyncResult.Success(serverTodos.size)
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Clear all todos from local database
     */
    suspend fun clearAllTodos() {
        todoDao.deleteAllTodos()
    }
}

/**
 * Result class for sync operations
 */
sealed class SyncResult {
    data class Success(val itemCount: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
} 