package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xdien.todoevent.data.entity.TodoEntity
import com.xdien.todoevent.ui.adapters.PersonalEventAdapter
import com.xdien.todoevent.ui.viewmodel.TodoViewModel

@Composable
fun PersonalEventListCompose(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = hiltViewModel(),
    filteredEvents: List<TodoEntity> = emptyList(),
    onEventClick: (TodoEntity) -> Unit = {},
    onAddEventClick: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncResult by viewModel.syncResult.collectAsState()
    
    // Show sync result message
    LaunchedEffect(syncResult) {
        syncResult?.let { result ->
            when (result) {
                is com.xdien.todoevent.data.repository.SyncResult.Success -> {
                    // Success - data synced successfully
                    // You can show a snackbar or toast here if needed
                }
                is com.xdien.todoevent.data.repository.SyncResult.Error -> {
                    // Error - show error message
                    // You can show a snackbar or toast here if needed
                }
            }
            // Clear sync result after showing
            viewModel.clearSyncResult()
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                SwipeRefreshLayout(context).apply {
                    setOnRefreshListener {
                        // Trigger sync with server
                        viewModel.syncWithServer()
                    }
                    
                    val recyclerView = RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = PersonalEventAdapter { event ->
                            onEventClick(event)
                        }
                        // Set an ID to easily find it later
                        id = android.R.id.list
                    }
                    
                    addView(recyclerView)
                }
            },
            update = { swipeRefreshLayout ->
                // Update refresh state
                swipeRefreshLayout.isRefreshing = isSyncing
                
                // Find RecyclerView safely
                val recyclerView = swipeRefreshLayout.findViewById<RecyclerView>(android.R.id.list)
                recyclerView?.let { rv ->
                    val adapter = rv.adapter as? PersonalEventAdapter
                    adapter?.submitList(filteredEvents)
                }
            }
        )
        
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