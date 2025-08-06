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
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    SwipeRefreshLayout(context).apply {
                        setOnRefreshListener {
                            // TODO: Implement refresh functionality
                            viewModel.loadTodos()
                        }
                        
                        val recyclerView = RecyclerView(context).apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = PersonalEventAdapter { event ->
                                onEventClick(event)
                            }
                        }
                        
                        addView(recyclerView)
                    }
                },
                update = { swipeRefreshLayout ->
                    swipeRefreshLayout.isRefreshing = isLoading
                    
                    val recyclerView = swipeRefreshLayout.getChildAt(0) as RecyclerView
                    val adapter = recyclerView.adapter as PersonalEventAdapter
                    adapter.submitList(filteredEvents)
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