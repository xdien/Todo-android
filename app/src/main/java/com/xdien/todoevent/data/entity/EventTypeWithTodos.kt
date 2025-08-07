package com.xdien.todoevent.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class để biểu diễn quan hệ One-to-Many giữa EventType và Todos
 * Một EventType có thể có nhiều Todo
 */
data class EventTypeWithTodos(
    @Embedded
    val eventType: EventTypeEntity,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "eventTypeId"
    )
    val todos: List<TodoEntity>
)
