package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.repository.EventRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case for creating a new event
 * 
 * This use case encapsulates the business logic for creating events,
 * including validation and coordination between different data sources.
 */
class CreateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /**
     * Execute the use case to create a new event
     * 
     * @param event The event to create
     * @return Result containing the created event or error
     */
    suspend operator fun invoke(event: Event): Result<Event> {
        return try {
            // Validate event data
            if (!event.isValidForCreation()) {
                return Result.failure(IllegalArgumentException("Invalid event data"))
            }
            
            // Create event through repository
            val createdEvent = eventRepository.createEvent(event)
            Result.success(createdEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute the use case with individual parameters
     * 
     * @param title Event title (required)
     * @param description Event description (required)
     * @param typeId Event type ID (required)
     * @param startDate Event start date in ISO format (required)
     * @param location Event location (required)
     * @return Result containing the created event or error
     */
    suspend operator fun invoke(
        title: String,
        description: String,
        typeId: Int,
        startDate: String,
        location: String
    ): Result<Event> {
        val event = Event(
            title = title,
            description = description,
            eventTypeId = typeId,
            startDate = startDate,
            location = location
        )
        return invoke(event)
    }
    
    /**
     * Create event and upload images in one operation
     * 
     * @param event The event to create
     * @param imageFiles List of image files to upload (max 5)
     * @return Result containing the created event with images or error
     */
    suspend fun invokeWithImages(event: Event, imageFiles: List<File>): Result<Event> {
        return try {
            // Validate event data
            if (!event.isValidForCreation()) {
                return Result.failure(IllegalArgumentException("Invalid event data"))
            }
            
            // Validate image count
            if (imageFiles.size > 5) {
                return Result.failure(IllegalArgumentException("Maximum 5 images allowed"))
            }
            
            // Create event first
            val createdEvent = eventRepository.createEvent(event)
            
            // Upload images if provided
            if (imageFiles.isNotEmpty()) {
                val uploadedImages = eventRepository.uploadEventImages(createdEvent.id, imageFiles)
                val eventWithImages = createdEvent.copy(images = uploadedImages)
                Result.success(eventWithImages)
            } else {
                Result.success(createdEvent)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create event and upload images with individual parameters
     * 
     * @param title Event title (required)
     * @param description Event description (required)
     * @param typeId Event type ID (required)
     * @param startDate Event start date in ISO format (required)
     * @param location Event location (required)
     * @param imageFiles List of image files to upload (max 5)
     * @return Result containing the created event with images or error
     */
    suspend fun invokeWithImages(
        title: String,
        description: String,
        typeId: Int,
        startDate: String,
        location: String,
        imageFiles: List<File>
    ): Result<Event> {
        val event = Event(
            title = title,
            description = description,
            eventTypeId = typeId,
            startDate = startDate,
            location = location
        )
        return invokeWithImages(event, imageFiles)
    }
} 