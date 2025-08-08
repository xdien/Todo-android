package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.ui.adapters.EventCard
import com.xdien.todoevent.ui.viewmodel.TodoViewModel

@Composable
fun PersonalEventListCompose(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = hiltViewModel(),
    filteredEvents: List<Event> = emptyList(),
    onEventClick: (Event) -> Unit = {},
    onAddEventClick: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val eventTypes by viewModel.eventTypes.collectAsState()

    // Debug log
    android.util.Log.d("PersonalEventListCompose", "Rendering with ${filteredEvents.size} filtered events")
    filteredEvents.forEach { event ->
        android.util.Log.d("PersonalEventListCompose", "Filtered event: ${event.title} (ID: ${event.id})")
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filteredEvents) { event ->
                android.util.Log.d("PersonalEventListCompose", "Rendering event item: ${event.title}")
                val eventTypeName = eventTypes.find { it.id == event.eventTypeId }?.name
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    eventTypeName = eventTypeName
                )
            }
        }
        
        // Refresh button
        FloatingActionButton(
            onClick = { viewModel.refreshEvents() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh events"
            )
        }
        
        // Floating Action Button
        FloatingActionButton(
            onClick = onAddEventClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new event"
            )
        }
    }
} 