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
            // First try to get from API
            val apiEvent = eventRepository.getEventFromApi(id)
            if (apiEvent != null) {
                Result.success(apiEvent)
            } else {
                // If API doesn't have the event, try local database
                val localEvent = eventRepository.getEventById(id).first()
                if (localEvent != null) {
                    Result.success(localEvent)
                } else {
                    Result.failure(Exception("Event not found"))
                }
            }
        } catch (e: Exception) {
            // If API fails, try local database
            try {
                val localEvent = eventRepository.getEventById(id).first()
                if (localEvent != null) {
                    Result.success(localEvent)
                } else {
                    Result.failure(Exception("Event not found"))
                }
            } catch (localError: Exception) {
                Result.failure(localError)
            }
        }
    }
}
