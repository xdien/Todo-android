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
                // If event is null and no exception was thrown, it means event doesn't exist locally
                // This should not happen with our current logic, but handle it gracefully
                Result.failure(Exception("Event not found"))
            }
        } catch (e: Exception) {
            // Check if it's a 404 error and provide a more specific error message
            val errorMessage = when {
                e.message?.contains("404") == true -> "Sự kiện không tồn tại (404)"
                e.message?.contains("not found", ignoreCase = true) == true -> "Sự kiện không tìm thấy"
                e.message?.contains("không tìm thấy", ignoreCase = true) == true -> "Sự kiện không tìm thấy"
                else -> e.message ?: "Không thể tải sự kiện"
            }
            Result.failure(Exception(errorMessage))
        }
    }
}
