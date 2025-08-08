package com.xdien.todoevent.common

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EventBus for managing event changes across the application
 * This singleton class provides a centralized way to notify about event changes
 * like create, update, delete operations
 */
@Singleton
class EventBus @Inject constructor() {
    
    /**
     * Event types that can be broadcasted
     */
    sealed class EventChange {
        object EventCreated : EventChange()
        object EventUpdated : EventChange()
        object EventDeleted : EventChange()
        object EventsRefreshed : EventChange()
    }
    
    private val _eventChanges = MutableSharedFlow<EventChange>(replay = 0)
    val eventChanges: SharedFlow<EventChange> = _eventChanges.asSharedFlow()
    
    /**
     * Broadcast an event change
     */
    suspend fun broadcastEventChange(eventChange: EventChange) {
        _eventChanges.emit(eventChange)
    }
    
    /**
     * Broadcast event created
     */
    suspend fun broadcastEventCreated() {
        broadcastEventChange(EventChange.EventCreated)
    }
    
    /**
     * Broadcast event updated
     */
    suspend fun broadcastEventUpdated() {
        broadcastEventChange(EventChange.EventUpdated)
    }
    
    /**
     * Broadcast event deleted
     */
    suspend fun broadcastEventDeleted() {
        broadcastEventChange(EventChange.EventDeleted)
    }
    
    /**
     * Broadcast events refreshed
     */
    suspend fun broadcastEventsRefreshed() {
        broadcastEventChange(EventChange.EventsRefreshed)
    }
}
