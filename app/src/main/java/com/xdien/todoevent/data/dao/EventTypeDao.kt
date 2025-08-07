package com.xdien.todoevent.data.dao

import androidx.room.*
import com.xdien.todoevent.data.entity.EventTypeEntity
import com.xdien.todoevent.data.entity.EventTypeWithTodos
import kotlinx.coroutines.flow.Flow

@Dao
interface EventTypeDao {
    @Query("SELECT * FROM event_type ORDER BY name ASC")
    fun getAllEventTypes(): Flow<List<EventTypeEntity>>
    
    @Query("SELECT * FROM event_type WHERE id = :id")
    suspend fun getEventTypeById(id: Long): EventTypeEntity?
    
    @Query("SELECT * FROM event_type WHERE name = :name")
    suspend fun getEventTypeByName(name: String): EventTypeEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventType(eventType: EventTypeEntity): Long
    
    @Update
    suspend fun updateEventType(eventType: EventTypeEntity)
    
    @Delete
    suspend fun deleteEventType(eventType: EventTypeEntity)
    
    @Query("DELETE FROM event_type WHERE id = :id")
    suspend fun deleteEventTypeById(id: Long)
    
    // Queries với quan hệ Todos
    @Transaction
    @Query("SELECT * FROM event_type ORDER BY name ASC")
    fun getAllEventTypesWithTodos(): Flow<List<EventTypeWithTodos>>
    
    @Transaction
    @Query("SELECT * FROM event_type WHERE id = :id")
    suspend fun getEventTypeWithTodosById(id: Long): EventTypeWithTodos?
    
    @Transaction
    @Query("SELECT * FROM event_type WHERE id = :id")
    fun getEventTypeWithTodosByIdFlow(id: Long): Flow<EventTypeWithTodos?>
}
