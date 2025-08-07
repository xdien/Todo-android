package com.xdien.todoevent.data.example

import com.xdien.todoevent.data.dao.EventTypeDao
import com.xdien.todoevent.data.dao.TodoDao
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.xdien.todoevent.data.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Ví dụ cách sử dụng quan hệ One-to-Many giữa EventType và Todo
 */
class RelationshipExample(
    private val eventTypeDao: EventTypeDao,
    private val todoDao: TodoDao
) {
    
    /**
     * Tạo một EventType mới
     */
    suspend fun createEventType(name: String): Long {
        val eventType = EventTypeEntity(name = name)
        return eventTypeDao.insertEventType(eventType)
    }
    
    /**
     * Tạo một Todo và liên kết với EventType
     */
    suspend fun createTodoWithEventType(
        title: String,
        description: String?,
        eventTypeId: Long
    ): Long {
        val todo = TodoEntity(
            title = title,
            description = description,
            eventTypeId = eventTypeId
        )
        return todoDao.insertTodo(todo)
    }
    
    /**
     * Lấy tất cả EventType cùng với danh sách Todo của chúng
     */
    fun getAllEventTypesWithTodos(): Flow<List<com.xdien.todoevent.data.entity.EventTypeWithTodos>> {
        return eventTypeDao.getAllEventTypesWithTodos()
    }
    
    /**
     * Lấy tất cả Todo cùng với EventType của chúng
     */
    fun getAllTodosWithEventType(): Flow<List<com.xdien.todoevent.data.entity.TodoWithEventType>> {
        return todoDao.getAllTodosWithEventType()
    }
    
    /**
     * Lấy tất cả Todo của một EventType cụ thể
     */
    fun getTodosByEventType(eventTypeId: Long): Flow<List<com.xdien.todoevent.data.entity.TodoWithEventType>> {
        return todoDao.getTodosByEventType(eventTypeId)
    }
    
    /**
     * Lấy tất cả Todo đã hoàn thành của một EventType
     */
    fun getCompletedTodosByEventType(eventTypeId: Long): Flow<List<com.xdien.todoevent.data.entity.TodoWithEventType>> {
        return todoDao.getTodosByEventTypeAndStatus(eventTypeId, true)
    }
    
    /**
     * Lấy tất cả Todo chưa hoàn thành của một EventType
     */
    fun getPendingTodosByEventType(eventTypeId: Long): Flow<List<com.xdien.todoevent.data.entity.TodoWithEventType>> {
        return todoDao.getTodosByEventTypeAndStatus(eventTypeId, false)
    }
    
    /**
     * Xóa một EventType và tất cả Todo liên quan (do CASCADE)
     */
    suspend fun deleteEventTypeAndRelatedTodos(eventTypeId: Long) {
        eventTypeDao.deleteEventTypeById(eventTypeId)
        // Các Todo sẽ tự động bị xóa do FOREIGN KEY CASCADE
    }
}
