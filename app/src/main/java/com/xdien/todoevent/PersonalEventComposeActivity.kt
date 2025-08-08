package com.xdien.todoevent

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.hilt.navigation.compose.hiltViewModel
import com.xdien.todoevent.common.NetworkManager
import com.xdien.todoevent.common.SharedPreferencesHelper

import com.xdien.todoevent.ui.components.ApiSettingsDialog
import com.xdien.todoevent.ui.components.ChipItem
import com.xdien.todoevent.ui.screens.PersonalEventListCompose
import com.xdien.todoevent.ui.theme.TodoEventTheme
import com.xdien.todoevent.ui.viewmodel.TodoViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PersonalEventComposeActivity : ComponentActivity() {
    
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    
    @Inject
    lateinit var networkManager: NetworkManager
    
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoEventTheme {
                val viewModel: TodoViewModel = hiltViewModel()
                val events by viewModel.events.collectAsState()
                val eventTypes by viewModel.eventTypes.collectAsState()
                
                // Search state
                var searchQuery by rememberSaveable { mutableStateOf("") }
                var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
                
                // Filter state
                var selectedEventTypes by rememberSaveable { mutableStateOf(emptySet<String>()) }
                
                // API Settings state
                var showApiSettingsDialog by rememberSaveable { mutableStateOf(false) }
                var currentApiUrl by rememberSaveable { mutableStateOf(sharedPreferencesHelper.getApiUrl()) }
                
                // Snackbar state
                val snackbarHostState = remember { SnackbarHostState() }
                

                
                // Create chip items for event types
                val eventTypeChips = remember(events, eventTypes) {
                    val eventTypeIds = events
                        .map { it.eventTypeId }
                        .distinct()
                    eventTypeIds.map { eventTypeId ->
                        val eventType = eventTypes.find { it.id == eventTypeId }
                        ChipItem(
                            id = eventTypeId.toString(),
                            title = eventType?.name ?: "Type $eventTypeId",
                            isSelected = selectedEventTypes.contains(eventTypeId.toString()),
                            color = when (eventTypeId) {
                                1 -> Color(0xFF2196F3) // Blue - Meeting
                                2 -> Color(0xFF4CAF50) // Green - Work
                                3 -> Color(0xFFE91E63) // Pink - Personal
                                4 -> Color(0xFF9C27B0) // Purple - Party
                                5 -> Color(0xFFFF9800) // Orange - Conference
                                else -> null
                            }
                        )
                    }
                }
                
                // Filter events based on search query and selected event types
                val filteredEvents = remember(events, searchQuery, selectedEventTypes) {
                    events.filter { event ->
                        val matchesSearch = searchQuery.isEmpty() || 
                            event.title.contains(searchQuery, ignoreCase = true) ||
                            event.description.contains(searchQuery, ignoreCase = true) ||
                            event.location.contains(searchQuery, ignoreCase = true)
                        
                        val matchesFilter = selectedEventTypes.isEmpty() || 
                            selectedEventTypes.contains(event.eventTypeId.toString())
                        
                        matchesSearch && matchesFilter
                    }
                }
                
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(paddingValues)
                    ) {

                        
                        // SearchBar with proper Material 3 implementation
                        if (isSearchExpanded) {
                            SearchBar(
                                modifier = Modifier
                                    .align(Alignment.TopCenter),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = searchQuery,
                                        onQueryChange = { searchQuery = it },
                                        onSearch = { searchQuery = it },
                                        expanded = isSearchExpanded,
                                        onExpandedChange = { isSearchExpanded = it },
                                        placeholder = { Text("Tìm kiếm sự kiện...") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search"
                                            )
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(
                                                    onClick = { searchQuery = "" }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Clear search"
                                                    )
                                                }
                                            }
                                        }
                                    )
                                },
                                expanded = isSearchExpanded,
                                onExpandedChange = { isSearchExpanded = it }
                            ) {
                                // Search suggestions could be added here
                            }
                        }
                        
                        // Top right buttons (Search and Settings)
                        if (!isSearchExpanded) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 16.dp, top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Settings button
                                IconButton(
                                    onClick = { showApiSettingsDialog = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "API Settings"
                                    )
                                }
                                
                                // Search button
                                IconButton(
                                    onClick = { isSearchExpanded = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search"
                                    )
                                }
                            }
                        }
                        
                        // Filter chips
                        if (eventTypeChips.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = if (isSearchExpanded) 80.dp else 60.dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(eventTypeChips) { chip ->
                                    FilterChip(
                                        onClick = {
                                            selectedEventTypes = if (selectedEventTypes.contains(chip.id)) {
                                                selectedEventTypes - chip.id
                                            } else {
                                                selectedEventTypes + chip.id
                                            }
                                        },
                                        label = { Text(chip.title) },
                                        selected = selectedEventTypes.contains(chip.id),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = chip.color ?: MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                        
                        // Event List
                        PersonalEventListCompose(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = if (eventTypeChips.isNotEmpty()) 120.dp else 80.dp),
                            viewModel = viewModel,
                            filteredEvents = filteredEvents,
                            onEventClick = { event ->
                                // Navigate to event detail screen
                                startActivity(EventDetailActivity.createIntent(this@PersonalEventComposeActivity, event.id.toLong()))
                            },
                            onAddEventClick = {
                                // Navigate to add event screen
                                val intent = EventFormActivity.createIntent(this@PersonalEventComposeActivity)
                                startActivity(intent)
                            }
                        )
                        
                        // API Settings Dialog
                        if (showApiSettingsDialog) {
                            ApiSettingsDialog(
                                currentApiUrl = currentApiUrl,
                                onDismiss = { showApiSettingsDialog = false },
                                onSave = { newApiUrl ->
                                    networkManager.updateApiUrl(newApiUrl)
                                    currentApiUrl = newApiUrl
                                    // Refresh data with new API URL
                                    viewModel.refreshEvents()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
} 