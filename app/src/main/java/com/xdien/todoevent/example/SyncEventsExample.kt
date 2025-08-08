package com.xdien.todoevent.example

import android.util.Log
import com.xdien.todoevent.domain.usecase.SyncEventsUseCase
import com.xdien.todoevent.domain.usecase.SyncEventsInput
import com.xdien.todoevent.domain.usecase.SyncEventsResult
import com.xdien.todoevent.domain.usecase.RefreshEventsUseCase
import com.xdien.todoevent.domain.usecase.RefreshEventsInput
import com.xdien.todoevent.domain.usecase.RefreshEventsResult
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

/**
 * Example class demonstrating how to use SyncEventsUseCase and RefreshEventsUseCase
 * 
 * This is a reference implementation showing different ways to use the sync functionality
 */
class SyncEventsExample @Inject constructor(
    private val syncEventsUseCase: SyncEventsUseCase,
    private val refreshEventsUseCase: RefreshEventsUseCase
) {
    
    /**
     * Example 1: Basic synchronization without local deletion
     */
    suspend fun exampleBasicSync() {
        Log.d("SyncEventsExample", "Starting basic sync example")
        
        val input = SyncEventsInput(
            keyword = null,
            typeId = null,
            allowLocalDeletion = false
        )
        
        val result = syncEventsUseCase.execute(input)
        
        when (result) {
            is SyncEventsResult.Success -> {
                Log.d("SyncEventsExample", "Basic sync completed successfully")
                Log.d("SyncEventsExample", "Added: ${result.addedCount} events")
                Log.d("SyncEventsExample", "Updated: ${result.updatedCount} events")
                Log.d("SyncEventsExample", "Deleted: ${result.deletedCount} events")
                Log.d("SyncEventsExample", "Total: ${result.totalEvents} events")
            }
            is SyncEventsResult.Error -> {
                Log.e("SyncEventsExample", "Basic sync failed: ${result.message}")
            }
        }
    }
    
    /**
     * Example 2: Full synchronization with local deletion
     */
    suspend fun exampleFullSync() {
        Log.d("SyncEventsExample", "Starting full sync example")
        
        val input = SyncEventsInput(
            keyword = null,
            typeId = null,
            allowLocalDeletion = true // This will delete local events not found in API
        )
        
        val result = syncEventsUseCase.execute(input)
        
        when (result) {
            is SyncEventsResult.Success -> {
                Log.d("SyncEventsExample", "Full sync completed successfully")
                Log.d("SyncEventsExample", "Added: ${result.addedCount} events")
                Log.d("SyncEventsExample", "Updated: ${result.updatedCount} events")
                Log.d("SyncEventsExample", "Deleted: ${result.deletedCount} events")
                Log.d("SyncEventsExample", "Total: ${result.totalEvents} events")
            }
            is SyncEventsResult.Error -> {
                Log.e("SyncEventsExample", "Full sync failed: ${result.message}")
            }
        }
    }
    
    /**
     * Example 3: Filtered synchronization by keyword
     */
    suspend fun exampleFilteredSync() {
        Log.d("SyncEventsExample", "Starting filtered sync example")
        
        val input = SyncEventsInput(
            keyword = "meeting", // Only sync events containing "meeting"
            typeId = null,
            allowLocalDeletion = false
        )
        
        val result = syncEventsUseCase.execute(input)
        
        when (result) {
            is SyncEventsResult.Success -> {
                Log.d("SyncEventsExample", "Filtered sync completed successfully")
                Log.d("SyncEventsExample", "Added: ${result.addedCount} meeting events")
                Log.d("SyncEventsExample", "Updated: ${result.updatedCount} meeting events")
                Log.d("SyncEventsExample", "Deleted: ${result.deletedCount} meeting events")
                Log.d("SyncEventsExample", "Total: ${result.totalEvents} meeting events")
            }
            is SyncEventsResult.Error -> {
                Log.e("SyncEventsExample", "Filtered sync failed: ${result.message}")
            }
        }
    }
    
    /**
     * Example 4: Filtered synchronization by event type
     */
    suspend fun exampleTypeFilteredSync() {
        Log.d("SyncEventsExample", "Starting type-filtered sync example")
        
        val input = SyncEventsInput(
            keyword = null,
            typeId = 1, // Only sync events of type ID 1
            allowLocalDeletion = false
        )
        
        val result = syncEventsUseCase.execute(input)
        
        when (result) {
            is SyncEventsResult.Success -> {
                Log.d("SyncEventsExample", "Type-filtered sync completed successfully")
                Log.d("SyncEventsExample", "Added: ${result.addedCount} events of type 1")
                Log.d("SyncEventsExample", "Updated: ${result.updatedCount} events of type 1")
                Log.d("SyncEventsExample", "Deleted: ${result.deletedCount} events of type 1")
                Log.d("SyncEventsExample", "Total: ${result.totalEvents} events of type 1")
            }
            is SyncEventsResult.Error -> {
                Log.e("SyncEventsExample", "Type-filtered sync failed: ${result.message}")
            }
        }
    }
    
    /**
     * Example 5: Using RefreshEventsUseCase for UI refresh
     */
    suspend fun exampleRefreshEvents() {
        Log.d("SyncEventsExample", "Starting refresh events example")
        
        val input = RefreshEventsInput(
            keyword = null,
            typeId = null,
            allowLocalDeletion = false
        )
        
        refreshEventsUseCase.execute(input).collect { result ->
            when (result) {
                is RefreshEventsResult.Success -> {
                    Log.d("SyncEventsExample", "Refresh completed successfully")
                    Log.d("SyncEventsExample", "Events count: ${result.events.size}")
                    Log.d("SyncEventsExample", "Sync info - Added: ${result.syncInfo.addedCount}")
                    Log.d("SyncEventsExample", "Sync info - Updated: ${result.syncInfo.updatedCount}")
                    Log.d("SyncEventsExample", "Sync info - Deleted: ${result.syncInfo.deletedCount}")
                    Log.d("SyncEventsExample", "Sync info - Total: ${result.syncInfo.totalEvents}")
                    
                    // Here you would typically update your UI with result.events
                    result.events.forEach { event ->
                        Log.d("SyncEventsExample", "Event: ${event.title} (ID: ${event.id})")
                    }
                }
                is RefreshEventsResult.Error -> {
                    Log.e("SyncEventsExample", "Refresh failed: ${result.message}")
                }
            }
        }
    }
    
    /**
     * Example 6: Error handling and retry logic
     */
    suspend fun exampleWithRetry(maxRetries: Int = 3) {
        Log.d("SyncEventsExample", "Starting sync with retry logic")
        
        var retryCount = 0
        var success = false
        
        while (retryCount < maxRetries && !success) {
            try {
                Log.d("SyncEventsExample", "Attempt ${retryCount + 1} of $maxRetries")
                
                val input = SyncEventsInput(
                    keyword = null,
                    typeId = null,
                    allowLocalDeletion = false
                )
                
                val result = syncEventsUseCase.execute(input)
                
                when (result) {
                    is SyncEventsResult.Success -> {
                        Log.d("SyncEventsExample", "Sync succeeded on attempt ${retryCount + 1}")
                        Log.d("SyncEventsExample", "Added: ${result.addedCount}, Updated: ${result.updatedCount}")
                        success = true
                    }
                    is SyncEventsResult.Error -> {
                        Log.e("SyncEventsExample", "Sync failed on attempt ${retryCount + 1}: ${result.message}")
                        retryCount++
                        
                        if (retryCount < maxRetries) {
                            Log.d("SyncEventsExample", "Retrying in 2 seconds...")
                            kotlinx.coroutines.delay(2000) // Wait 2 seconds before retry
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SyncEventsExample", "Exception on attempt ${retryCount + 1}", e)
                retryCount++
                
                if (retryCount < maxRetries) {
                    Log.d("SyncEventsExample", "Retrying in 2 seconds...")
                    kotlinx.coroutines.delay(2000)
                }
            }
        }
        
        if (!success) {
            Log.e("SyncEventsExample", "All $maxRetries attempts failed")
        }
    }
    
    /**
     * Example 7: Batch synchronization for different event types
     */
    suspend fun exampleBatchSync() {
        Log.d("SyncEventsExample", "Starting batch sync example")
        
        val eventTypes = listOf(1, 2, 3) // Sync different event types
        var totalAdded = 0
        var totalUpdated = 0
        var totalDeleted = 0
        
        eventTypes.forEach { typeId ->
            Log.d("SyncEventsExample", "Syncing events of type $typeId")
            
            val input = SyncEventsInput(
                keyword = null,
                typeId = typeId,
                allowLocalDeletion = false
            )
            
            val result = syncEventsUseCase.execute(input)
            
            when (result) {
                is SyncEventsResult.Success -> {
                    totalAdded += result.addedCount
                    totalUpdated += result.updatedCount
                    totalDeleted += result.deletedCount
                    
                    Log.d("SyncEventsExample", "Type $typeId sync completed: " +
                        "Added: ${result.addedCount}, Updated: ${result.updatedCount}, " +
                        "Deleted: ${result.deletedCount}")
                }
                is SyncEventsResult.Error -> {
                    Log.e("SyncEventsExample", "Type $typeId sync failed: ${result.message}")
                }
            }
        }
        
        Log.d("SyncEventsExample", "Batch sync completed")
        Log.d("SyncEventsExample", "Total added: $totalAdded")
        Log.d("SyncEventsExample", "Total updated: $totalUpdated")
        Log.d("SyncEventsExample", "Total deleted: $totalDeleted")
    }
}
