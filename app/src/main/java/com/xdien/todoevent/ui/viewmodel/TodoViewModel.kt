package com.xdien.todoevent.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xdien.todoevent.common.EventBus
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.model.EventType
import com.xdien.todoevent.domain.repository.EventRepository
import com.xdien.todoevent.ui.components.toChipItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val eventBus: EventBus
) : ViewModel() {
    
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // LiveData for Fragment approach
    private val _eventsLiveData = MutableLiveData<List<Event>>()
    val eventsLiveData: LiveData<List<Event>> = _eventsLiveData
    
    private val _isLoadingLiveData = MutableLiveData<Boolean>()
    val isLoadingLiveData: LiveData<Boolean> = _isLoadingLiveData
    
    // For event detail screen
    private val _selectedEvent = MutableStateFlow<Event?>(null)
    val selectedEvent: StateFlow<Event?> = _selectedEvent.asStateFlow()
    
    // For chip selection
    private val _selectedChipIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedChipIds: StateFlow<Set<Long>> = _selectedChipIds.asStateFlow()
    
    private val _chipItems = MutableStateFlow<List<com.xdien.todoevent.ui.components.ChipItem>>(emptyList())
    val chipItems: StateFlow<List<com.xdien.todoevent.ui.components.ChipItem>> = _chipItems.asStateFlow()
    
    // For event types
    private val _eventTypes = MutableStateFlow<List<EventType>>(emptyList())
    val eventTypes: StateFlow<List<EventType>> = _eventTypes.asStateFlow()
    
    init {
        loadEvents()
        loadEventTypes()
        // Initialize chip items
        updateChipItems()
        
        // Listen to event changes from EventBus
        listenToEventChanges()
    }
    
    /**
     * Listen to event changes from EventBus and refresh events accordingly
     */
    private fun listenToEventChanges() {
        viewModelScope.launch {
            eventBus.eventChanges.collect { eventChange ->
                when (eventChange) {
                    is EventBus.EventChange.EventCreated -> {
                        android.util.Log.d("TodoViewModel", "Event created, refreshing events")
                        loadEvents()
                    }
                    is EventBus.EventChange.EventUpdated -> {
                        android.util.Log.d("TodoViewModel", "Event updated, refreshing events")
                        loadEvents()
                    }
                    is EventBus.EventChange.EventDeleted -> {
                        android.util.Log.d("TodoViewModel", "Event deleted, refreshing events")
                        loadEvents()
                    }
                    is EventBus.EventChange.EventsRefreshed -> {
                        android.util.Log.d("TodoViewModel", "Events refreshed, refreshing events")
                        loadEvents()
                    }
                }
            }
        }
    }
    
    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoadingLiveData.value = true
            try {
                // Fetch events from API
                eventRepository.getEvents(null, null).collect { eventList ->
                    android.util.Log.d("TodoViewModel", "Received ${eventList.size} events from repository")
                    eventList.forEach { event ->
                        android.util.Log.d("TodoViewModel", "Event: ${event.title} (ID: ${event.id})")
                    }
                    _events.value = eventList
                    _eventsLiveData.value = eventList
                }
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "Error loading events", e)
                _events.value = emptyList()
                _eventsLiveData.value = emptyList()
            } finally {
                _isLoading.value = false
                _isLoadingLiveData.value = false
            }
        }
    }
    
    fun loadEventTypes() {
        viewModelScope.launch {
            try {
                val result = eventRepository.getEventTypes()
                result.onSuccess { types ->
                    _eventTypes.value = types
                }.onFailure { error ->
                    android.util.Log.e("TodoViewModel", "Failed to load event types: ${error.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "Exception loading event types", e)
            }
        }
    }
    
    fun createEvent(title: String, description: String, eventTypeId: Int, startDate: String, location: String) {
        viewModelScope.launch {
            try {
                val event = Event(
                    id = 0, // Will be set by server
                    title = title,
                    description = description,
                    eventTypeId = eventTypeId,
                    startDate = startDate,
                    location = location,
                    createdAt = "",
                    updatedAt = "",
                    images = emptyList()
                )
                eventRepository.createEvent(event)
                // Broadcast event created
                eventBus.broadcastEventCreated()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun updateEvent(id: Int, title: String, description: String, typeId: Int, startDate: String, location: String) {
        viewModelScope.launch {
            try {
                val result = eventRepository.updateEvent(id, title, description, typeId, startDate, location)
                if (result.isSuccess) {
                    // Broadcast event updated
                    eventBus.broadcastEventUpdated()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("TodoViewModel", "Deleting event with ID: $id")
                eventRepository.deleteEvent(id)
                
                // Update events list by removing the deleted event immediately
                val currentEvents = _events.value.toMutableList()
                val removedEvent = currentEvents.find { it.id == id }
                if (removedEvent != null) {
                    currentEvents.removeAll { it.id == id }
                    _events.value = currentEvents
                    _eventsLiveData.value = currentEvents
                    android.util.Log.d("TodoViewModel", "Event deleted successfully. Remaining events: ${currentEvents.size}")
                } else {
                    android.util.Log.w("TodoViewModel", "Event with ID $id not found in current list")
                }
                
                // Broadcast event deleted
                eventBus.broadcastEventDeleted()
            } catch (e: Exception) {
                android.util.Log.e("TodoViewModel", "Error deleting event", e)
            }
        }
    }
    
    // Get event by ID for detail screen
    suspend fun getEventById(id: Int): Flow<Event?> {
        return eventRepository.getEventById(id)
    }
    
    // Load event by ID and update selectedEvent
    fun loadEventById(id: Int) {
        viewModelScope.launch {
            eventRepository.getEventById(id).collect { event ->
                _selectedEvent.value = event
            }
        }
    }
    
    /**
     * Refresh events - this method is called for pull-to-refresh
     */
    fun refreshEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch fresh data from API
                eventRepository.getEvents(null, null).collect { eventList ->
                    _events.value = eventList
                    _eventsLiveData.value = eventList
                }
                // Broadcast events refreshed
                eventBus.broadcastEventsRefreshed()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Update chip items when events change
    private fun updateChipItems() {
        viewModelScope.launch {
            events.collect { eventList ->
                val chipItems = eventList.toChipItems(_selectedChipIds.value)
                _chipItems.value = chipItems
            }
        }
    }
    
    // Handle chip selection
    fun selectChip(chipId: String, singleSelection: Boolean = true) {
        val id = chipId.toLongOrNull() ?: return
        
        if (singleSelection) {
            _selectedChipIds.value = setOf(id)
        } else {
            val currentSelected = _selectedChipIds.value.toMutableSet()
            if (currentSelected.contains(id)) {
                currentSelected.remove(id)
            } else {
                currentSelected.add(id)
            }
            _selectedChipIds.value = currentSelected
        }
        
        // Update chip items with new selection
        updateChipItems()
    }
    
    // Get selected events
    fun getSelectedEvents(): List<Event> {
        return events.value.filter { it.id.toLong() in _selectedChipIds.value }
    }
    
    // Clear all selections
    fun clearChipSelection() {
        _selectedChipIds.value = emptySet()
        updateChipItems()
    }
    
    // Get event type name by ID
    fun getEventTypeName(eventTypeId: Int): String {
        return _eventTypes.value.find { it.id == eventTypeId }?.name ?: "Type $eventTypeId"
    }
} 