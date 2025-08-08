package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.ui.adapters.PersonalEventAdapter
import com.xdien.todoevent.ui.viewmodel.TodoViewModel

@Composable
fun PersonalEventListCompose(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = hiltViewModel(),
    filteredEvents: List<Event> = emptyList(),
    onEventClick: (Event) -> Unit = {},
    onAddEventClick: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val eventTypes by viewModel.eventTypes.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Debug log
    android.util.Log.d("PersonalEventListCompose", "Rendering with ${filteredEvents.size} filtered events")
    filteredEvents.forEach { event ->
        android.util.Log.d("PersonalEventListCompose", "Filtered event: ${event.title} (ID: ${event.id})")
    }
    
    // Create adapter with remember to maintain state
    val adapter = remember {
        PersonalEventAdapter(onEventClick = onEventClick)
    }
    
    // Update adapter when events change
    LaunchedEffect(filteredEvents) {
        android.util.Log.d("PersonalEventListCompose", "Updating adapter with ${filteredEvents.size} events")
        adapter.submitList(filteredEvents.toList()) // Create a new list to ensure DiffUtil works
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // SwipeRefreshLayout with RecyclerView
        AndroidView(
            factory = { context ->
                android.util.Log.d("PersonalEventListCompose", "Creating SwipeRefreshLayout and RecyclerView")
                SwipeRefreshLayout(context).apply {
                    // Create RecyclerView immediately
                    val recyclerView = RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        this.adapter = adapter
                        setHasFixedSize(true)
                    }
                    addView(recyclerView)
                    
                    setOnRefreshListener {
                        android.util.Log.d("PersonalEventListCompose", "Refresh triggered")
                        viewModel.refreshEvents()
                        // Stop refreshing after a delay
                        postDelayed({ isRefreshing = false }, 1000)
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { swipeRefreshLayout ->
                // Update refreshing state
                swipeRefreshLayout.isRefreshing = isLoading
            }
        )
        
        // Floating Action Button for adding new event
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