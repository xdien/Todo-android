package com.xdien.todoevent.data.dao

import androidx.room.*
import com.xdien.todoevent.data.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun getAllEvents(): Flow<List<EventEntity>>
    
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Int): EventEntity?
    
    @Query("""
        SELECT * FROM events 
        WHERE title LIKE '%' || :keyword || '%' 
        OR description LIKE '%' || :keyword || '%' 
        OR location LIKE '%' || :keyword || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun searchEvents(keyword: String): List<EventEntity>
    
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getEvents(): List<EventEntity>
    
    @Query("""
        SELECT * FROM events 
        WHERE title LIKE '%' || :keyword || '%' 
        OR description LIKE '%' || :keyword || '%' 
        OR location LIKE '%' || :keyword || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun getEventsWithKeyword(keyword: String): List<EventEntity>
    
    @Query("""
        SELECT * FROM events 
        WHERE eventTypeId = :typeId
        ORDER BY createdAt DESC
    """)
    suspend fun getEventsByType(typeId: Int): List<EventEntity>
    
    @Query("""
        SELECT * FROM events 
        WHERE (title LIKE '%' || :keyword || '%' 
        OR description LIKE '%' || :keyword || '%' 
        OR location LIKE '%' || :keyword || '%')
        AND eventTypeId = :typeId
        ORDER BY createdAt DESC
    """)
    suspend fun getEventsWithKeywordAndType(keyword: String, typeId: Int): List<EventEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
    
    @Update
    suspend fun updateEvent(event: EventEntity)
    
    @Delete
    suspend fun deleteEvent(event: EventEntity)
    
    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Int)
    
    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()
}
