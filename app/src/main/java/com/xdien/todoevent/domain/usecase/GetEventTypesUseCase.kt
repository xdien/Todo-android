package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.repository.EventRepository
import javax.inject.Inject

/**
 * Use case for getting event types
 * 
 * This use case encapsulates the business logic for retrieving event types
 * from the repository.
 */
class GetEventTypesUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /**
     * Get all event types
     * 
     * @return Result containing the list of event types or error
     */
    suspend operator fun invoke(): Result<List<EventType>> {
        return try {
            val eventTypes = eventRepository.getEventTypes()
            Result.success(eventTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get event type by ID
     * 
     * @param typeId The event type ID
     * @return Result containing the event type or error
     */
    suspend fun getEventTypeById(typeId: Int): Result<EventType> {
        return try {
            val eventTypes = eventRepository.getEventTypes()
            val eventType = eventTypes.find { it.id == typeId }
            if (eventType != null) {
                Result.success(eventType)
            } else {
                Result.failure(IllegalArgumentException("Event type not found with ID: $typeId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get event type by name
     * 
     * @param name The event type name
     * @return Result containing the event type or error
     */
    suspend fun getEventTypeByName(name: String): Result<EventType> {
        return try {
            val eventTypes = eventRepository.getEventTypes()
            val eventType = eventTypes.find { it.name.equals(name, ignoreCase = true) }
            if (eventType != null) {
                Result.success(eventType)
            } else {
                Result.failure(IllegalArgumentException("Event type not found with name: $name"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 