package com.xdien.todoevent.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val eventRepository: EventRepository
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
                // Reload events after creation
                loadEvents()
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
                    // Reload events after update
                    loadEvents()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            try {
                eventRepository.deleteEvent(id)
                // Reload events after deletion
                loadEvents()
            } catch (e: Exception) {
                // Handle error
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