package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for getting an event by ID with API priority and local fallback
 * 
 * This use case first tries to fetch the event from the API,
 * and falls back to local database if API fails or event doesn't exist.
 */
class GetEventByIdUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    /**
     * Execute the use case to get an event by ID
     * 
     * @param id The event ID
     * @return Result containing the event or error
     */
    suspend operator fun invoke(id: Int): Result<Event> {
        return try {
            // Try to get from API first, then local database
            val event = eventRepository.getEventById(id).first()
            if (event != null) {
                Result.success(event)
            } else {
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
