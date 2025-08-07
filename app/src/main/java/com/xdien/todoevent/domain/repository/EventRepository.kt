package com.xdien.todoevent.domain.repository

import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.model.EventImage
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for Event operations
 * 
 * This interface defines the contract for event data operations
 * and is implemented by the data layer.
 */
interface EventRepository {
    /**
     * Create a new event
     * 
     * @param event The event to create
     * @return The created event with server-generated ID
     */
    suspend fun createEvent(event: Event): Event
    
    /**
     * Get all events as a Flow
     * 
     * @return Flow of all events
     */
    fun getAllEvents(): Flow<List<Event>>
    
    /**
     * Get event by ID
     * 
     * @param id The event ID
     * @return Flow of the event or null if not found
     */
    fun getEventById(id: Int): Flow<Event?>
    
    /**
     * Update an existing event
     * 
     * @param event The event to update
     */
    suspend fun updateEvent(event: Event)
    
    /**
     * Delete an event
     * 
     * @param event The event to delete
     */
    suspend fun deleteEvent(event: Event)
    
    /**
     * Delete event by ID
     * 
     * @param id The event ID to delete
     */
    suspend fun deleteEventById(id: Int)
    
    /**
     * Fetch events from remote API
     * 
     * @param keyword Search keyword (optional)
     * @param typeId Filter by event type ID (optional)
     * @return List of events from API
     */
    suspend fun fetchEventsFromApi(keyword: String? = null, typeId: Int? = null): List<Event>
    
    /**
     * Get all event types
     * 
     * @return List of available event types
     */
    suspend fun getEventTypes(): List<EventType>
    
    /**
     * Upload images for an event (max 5 images)
     * 
     * @param eventId The event ID
     * @param imageFiles List of image files to upload
     * @return List of uploaded image details
     */
    suspend fun uploadEventImages(eventId: Int, imageFiles: List<File>): List<EventImage>
    
    /**
     * Delete an image from an event
     * 
     * @param eventId The event ID
     * @param imageId The image ID to delete
     */
    suspend fun deleteEventImage(eventId: Int, imageId: Int)
} 