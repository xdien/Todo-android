package com.xdien.todoevent.domain.usecase

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.xdien.todoevent.common.UseCase
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.repository.EventRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for synchronizing events between API and local database
 * 
 * This use case implements a conflict resolution strategy based on the most recent update time.
 * It uses DiffUtil to efficiently compare and merge changes between API and local data.
 */
class SyncEventsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) : UseCase<SyncEventsInput, SyncEventsResult>() {

    override suspend fun execute(input: SyncEventsInput): SyncEventsResult {
        return try {
            Log.d("SyncEventsUseCase", "Starting event synchronization")
            
            // Get events from API
            val apiEvents = getEventsFromApi(input.keyword, input.typeId)
            Log.d("SyncEventsUseCase", "Retrieved ${apiEvents.size} events from API")
            
            // Get events from local database
            val localEvents = getEventsFromLocal(input.keyword, input.typeId)
            Log.d("SyncEventsUseCase", "Retrieved ${localEvents.size} events from local database")
            
            // Perform DiffUtil comparison
            val diffResult = performDiffComparison(localEvents, apiEvents)
            
            // Apply changes based on conflict resolution strategy
            val syncResult = applyChangesWithConflictResolution(diffResult, localEvents, apiEvents, input.allowLocalDeletion)
            
            Log.d("SyncEventsUseCase", "Synchronization completed successfully")
            SyncEventsResult.Success(
                addedCount = syncResult.addedCount,
                updatedCount = syncResult.updatedCount,
                deletedCount = syncResult.deletedCount,
                totalEvents = syncResult.totalEvents
            )
            
        } catch (e: Exception) {
            Log.e("SyncEventsUseCase", "Error during synchronization", e)
            SyncEventsResult.Error(e.message ?: "Unknown error occurred")
        }
    }
    
    /**
     * Get events from API
     */
    private suspend fun getEventsFromApi(keyword: String?, typeId: Int?): List<Event> {
        return try {
            eventRepository.getEvents(keyword, typeId).first()
        } catch (e: Exception) {
            Log.e("SyncEventsUseCase", "Error getting events from API", e)
            emptyList()
        }
    }
    
    /**
     * Get events from local database
     */
    private suspend fun getEventsFromLocal(keyword: String?, typeId: Int?): List<Event> {
        return try {
            eventRepository.getEvents(keyword, typeId).first()
        } catch (e: Exception) {
            Log.e("SyncEventsUseCase", "Error getting events from local database", e)
            emptyList()
        }
    }
    
    /**
     * Perform DiffUtil comparison between local and API events
     */
    private fun performDiffComparison(localEvents: List<Event>, apiEvents: List<Event>): DiffUtil.DiffResult {
        val diffCallback = EventDiffCallback(localEvents, apiEvents)
        return DiffUtil.calculateDiff(diffCallback)
    }
    
    /**
     * Apply changes with conflict resolution strategy
     */
    private suspend fun applyChangesWithConflictResolution(
        diffResult: DiffUtil.DiffResult,
        localEvents: List<Event>,
        apiEvents: List<Event>,
        allowLocalDeletion: Boolean
    ): SyncResult {
        var addedCount = 0
        var updatedCount = 0
        var deletedCount = 0
        
        // Create maps for efficient lookup
        val localEventMap = localEvents.associateBy { it.id }
        val apiEventMap = apiEvents.associateBy { it.id }
        
        // Process additions (events in API but not in local)
        apiEvents.forEach { apiEvent ->
            if (!localEventMap.containsKey(apiEvent.id)) {
                try {
                    eventRepository.createEvent(apiEvent)
                    addedCount++
                    Log.d("SyncEventsUseCase", "Added event: ${apiEvent.title} (ID: ${apiEvent.id})")
                } catch (e: Exception) {
                    Log.e("SyncEventsUseCase", "Error adding event ${apiEvent.id}", e)
                }
            }
        }
        
        // Process updates and conflicts
        localEvents.forEach { localEvent ->
            val apiEvent = apiEventMap[localEvent.id]
            if (apiEvent != null) {
                // Event exists in both local and API - check for conflicts
                val shouldUpdate = resolveConflict(localEvent, apiEvent)
                if (shouldUpdate) {
                    try {
                        val updateResult = eventRepository.updateEvent(
                            id = apiEvent.id,
                            title = apiEvent.title,
                            description = apiEvent.description,
                            typeId = apiEvent.eventTypeId,
                            startDate = apiEvent.startDate,
                            location = apiEvent.location
                        )
                        if (updateResult.isSuccess) {
                            updatedCount++
                            Log.d("SyncEventsUseCase", "Updated event: ${apiEvent.title} (ID: ${apiEvent.id})")
                        } else {
                            Log.e("SyncEventsUseCase", "Failed to update event ${apiEvent.id}: ${updateResult.exceptionOrNull()?.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("SyncEventsUseCase", "Error updating event ${apiEvent.id}", e)
                    }
                }
            } else {
                // Event exists in local but not in API - check if it should be deleted
                if (allowLocalDeletion) {
                    try {
                        eventRepository.deleteEvent(localEvent.id)
                        deletedCount++
                        Log.d("SyncEventsUseCase", "Deleted event: ${localEvent.title} (ID: ${localEvent.id})")
                    } catch (e: Exception) {
                        Log.e("SyncEventsUseCase", "Error deleting event ${localEvent.id}", e)
                    }
                }
            }
        }
        
        return SyncResult(addedCount, updatedCount, deletedCount, apiEvents.size)
    }
    
    /**
     * Resolve conflict between local and API event based on update time
     * Returns true if API version should be used, false if local version should be kept
     */
    private fun resolveConflict(localEvent: Event, apiEvent: Event): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        try {
            val localUpdatedAt = localEvent.updatedAt?.let { dateFormat.parse(it) }
            val apiUpdatedAt = apiEvent.updatedAt?.let { dateFormat.parse(it) }
            
            // If API event has no update time, keep local version
            if (apiUpdatedAt == null) {
                Log.d("SyncEventsUseCase", "API event ${apiEvent.id} has no update time, keeping local version")
                return false
            }
            
            // If local event has no update time, use API version
            if (localUpdatedAt == null) {
                Log.d("SyncEventsUseCase", "Local event ${localEvent.id} has no update time, using API version")
                return true
            }
            
            // Compare update times - use the most recent one
            val shouldUseApi = apiUpdatedAt.after(localUpdatedAt)
            Log.d("SyncEventsUseCase", "Conflict resolved for event ${apiEvent.id}: " +
                "Local updated at $localUpdatedAt, API updated at $apiUpdatedAt, " +
                "Using ${if (shouldUseApi) "API" else "local"} version")
            
            return shouldUseApi
            
        } catch (e: Exception) {
            Log.e("SyncEventsUseCase", "Error parsing dates for event ${apiEvent.id}", e)
            // In case of parsing error, prefer API version
            return true
        }
    }
    
    /**
     * DiffUtil callback for comparing events
     */
    private class EventDiffCallback(
        private val oldList: List<Event>,
        private val newList: List<Event>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldEvent = oldList[oldItemPosition]
            val newEvent = newList[newItemPosition]
            return oldEvent.id == newEvent.id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldEvent = oldList[oldItemPosition]
            val newEvent = newList[newItemPosition]
            return oldEvent == newEvent
        }
    }
    
    /**
     * Result of synchronization operation
     */
    private data class SyncResult(
        val addedCount: Int,
        val updatedCount: Int,
        val deletedCount: Int,
        val totalEvents: Int
    )
}

/**
 * Input data for SyncEventsUseCase
 */
data class SyncEventsInput(
    val keyword: String? = null,
    val typeId: Int? = null,
    val allowLocalDeletion: Boolean = false // Whether to delete local events not found in API
)

/**
 * Result of synchronization operation
 */
sealed class SyncEventsResult {
    data class Success(
        val addedCount: Int,
        val updatedCount: Int,
        val deletedCount: Int,
        val totalEvents: Int
    ) : SyncEventsResult()
    
    data class Error(val message: String) : SyncEventsResult()
}
