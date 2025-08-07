package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for updating an existing event
 * 
 * This use case encapsulates the business logic for updating events,
 * including validation and coordination between different data sources.
 */
class UpdateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /**
     * Execute the use case to update an existing event
     * 
     * @param event The event to update
     * @return Result containing the updated event or error
     */
    suspend operator fun invoke(event: Event): Result<Event> {
        return try {
            // Validate event data
            if (!event.isValidForUpdate()) {
                return Result.failure(IllegalArgumentException("Invalid event data"))
            }
            
            // Update event through repository (API first, local DB only if API succeeds)
            eventRepository.updateEvent(event)
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Execute the use case with individual parameters
     * 
     * @param id Event ID (required)
     * @param title Event title (required)
     * @param description Event description (required)
     * @param typeId Event type ID (required)
     * @param startDate Event start date in ISO format (required)
     * @param location Event location (required)
     * @return Result containing the updated event or error
     */
    suspend operator fun invoke(
        id: Int,
        title: String,
        description: String,
        typeId: Int,
        startDate: String,
        location: String
    ): Result<Event> {
        val event = Event(
            id = id,
            title = title,
            description = description,
            typeId = typeId,
            startDate = startDate,
            location = location
        )
        return invoke(event)
    }
} 