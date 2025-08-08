package com.xdien.todoevent.domain.usecase

import com.xdien.todoevent.common.UseCase
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for refreshing events with synchronization
 * 
 * This use case combines the sync functionality with the regular event loading
 * to provide a comprehensive refresh operation.
 */
class RefreshEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val syncEventsUseCase: SyncEventsUseCase
) : UseCase<RefreshEventsInput, Flow<RefreshEventsResult>>() {

    override suspend fun execute(input: RefreshEventsInput): Flow<RefreshEventsResult> {
        return try {
            // First, perform synchronization
            val syncResult = syncEventsUseCase.execute(
                SyncEventsInput(
                    keyword = input.keyword,
                    typeId = input.typeId,
                    allowLocalDeletion = input.allowLocalDeletion
                )
            )
            
            // Then, return the updated events
            eventRepository.getEvents(input.keyword, input.typeId).map { events ->
                when (syncResult) {
                    is SyncEventsResult.Success -> {
                        RefreshEventsResult.Success(
                            events = events,
                            syncInfo = RefreshEventsResult.SyncInfo(
                                addedCount = syncResult.addedCount,
                                updatedCount = syncResult.updatedCount,
                                deletedCount = syncResult.deletedCount,
                                totalEvents = syncResult.totalEvents
                            )
                        )
                    }
                    is SyncEventsResult.Error -> {
                        RefreshEventsResult.Error(syncResult.message)
                    }
                }
            }
            
        } catch (e: Exception) {
            kotlinx.coroutines.flow.flow {
                emit(RefreshEventsResult.Error(e.message ?: "Unknown error occurred"))
            }
        }
    }
}

/**
 * Input data for RefreshEventsUseCase
 */
data class RefreshEventsInput(
    val keyword: String? = null,
    val typeId: Int? = null,
    val allowLocalDeletion: Boolean = false
)

/**
 * Result of refresh events operation
 */
sealed class RefreshEventsResult {
    data class Success(
        val events: List<Event>,
        val syncInfo: SyncInfo
    ) : RefreshEventsResult()
    
    data class Error(val message: String) : RefreshEventsResult()
    
    /**
     * Information about the synchronization operation
     */
    data class SyncInfo(
        val addedCount: Int,
        val updatedCount: Int,
        val deletedCount: Int,
        val totalEvents: Int
    )
}
