package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.common.UseCase
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for loading events from the repository
 * 
 * This use case encapsulates the business logic for loading events,
 * including error handling for 404 errors and other exceptions.
 */
class LoadEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) : UseCase<LoadEventsInput, Flow<LoadEventsResult>>() {

    override suspend fun execute(input: LoadEventsInput): Flow<LoadEventsResult> {
        return flow {
            try {
                // Fetch events from repository
                eventRepository.getEvents(input.keyword, input.typeId).collect { eventList ->
                    emit(LoadEventsResult.Success(eventList))
                }
            } catch (e: Exception) {
                // Check if it's a 404 error
                val isNotFoundError = e.message?.contains("404") == true || 
                                    e.message?.contains("not found", ignoreCase = true) == true ||
                                    e.message?.contains("không tìm thấy", ignoreCase = true) == true
                
                if (isNotFoundError) {
                    emit(LoadEventsResult.NotFoundError(e.message ?: "Resource not found"))
                } else {
                    emit(LoadEventsResult.Error(e.message ?: "Unknown error occurred"))
                }
            }
        }.catch { e ->
            // Handle any uncaught exceptions
            emit(LoadEventsResult.Error(e.message ?: "Unknown error occurred"))
        }
    }
}

/**
 * Input data for LoadEventsUseCase
 */
data class LoadEventsInput(
    val keyword: String? = null,
    val typeId: Int? = null
)

/**
 * Result of loading events operation
 */
sealed class LoadEventsResult {
    data class Success(val events: List<Event>) : LoadEventsResult()
    data class Error(val message: String) : LoadEventsResult()
    data class NotFoundError(val message: String) : LoadEventsResult()
}