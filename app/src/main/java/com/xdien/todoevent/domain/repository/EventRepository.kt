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
     * Get events with optional filtering
     * 
     * @param keyword Search keyword (optional)
     * @param typeId Filter by event type ID (optional)
     * @return Flow of events
     */
    suspend fun getEvents(keyword: String? = null, typeId: Int? = null): Flow<List<Event>>
    
    /**
     * Get event by ID
     * 
     * @param id The event ID
     * @return Flow of the event or null if not found
     */
    suspend fun getEventById(id: Int): Flow<Event?>
    
    /**
     * Update an existing event
     * 
     * @param id The event ID
     * @param title Event title
     * @param description Event description
     * @param typeId Event type ID
     * @param startDate Event start date
     * @param location Event location
     * @return Result containing the updated event or error
     */
    suspend fun updateEvent(
        id: Int,
        title: String,
        description: String,
        typeId: Int,
        startDate: String,
        location: String
    ): Result<Event>
    
    /**
     * Delete an event
     * 
     * @param id The event ID to delete
     */
    suspend fun deleteEvent(id: Int)
    
    /**
     * Get all event types
     * 
     * @return Result containing list of available event types or error
     */
    suspend fun getEventTypes(): Result<List<EventType>>
    
    /**
     * Upload images for an event (max 5 images)
     * 
     * @param eventId The event ID
     * @param imageFiles List of image files to upload
     * @return List of uploaded image details with full URLs
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