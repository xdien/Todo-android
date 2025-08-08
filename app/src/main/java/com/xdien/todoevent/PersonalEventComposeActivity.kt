package com.xdien.todoevent

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xdien.todoevent.common.NetworkManager
import com.xdien.todoevent.common.SharedPreferencesHelper

import com.xdien.todoevent.ui.components.ApiSettingsDialog
import com.xdien.todoevent.ui.components.ChipItem
import com.xdien.todoevent.ui.screens.PersonalEventListCompose
import com.xdien.todoevent.ui.theme.TodoEventTheme
import com.xdien.todoevent.ui.viewmodel.TodoViewModel
import com.xdien.todoevent.ui.adapters.EventCard
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
                val events by viewModel.events.collectAsStateWithLifecycle()
                val eventTypes by viewModel.eventTypes.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
                val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
                
                // Search state
                var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
                
                // Filter state (chỉ áp dụng cho events gốc, không áp dụng cho search results)
                var selectedEventTypes by rememberSaveable { mutableStateOf(emptySet<String>()) }
                
                // API Settings state
                var showApiSettingsDialog by rememberSaveable { mutableStateOf(false) }
                var currentApiUrl by rememberSaveable { mutableStateOf(sharedPreferencesHelper.getApiUrl()) }
                
                // Snackbar state
                val snackbarHostState = remember { SnackbarHostState() }
                
                // Create chip items for event types (chỉ dùng cho events gốc)
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
                
                // Quyết định hiển thị events nào - hiển thị search results hoặc danh sách gốc với filter
                val displayEvents = remember(events, selectedEventTypes, searchQuery, searchResults, isSearchExpanded) {
                    if (searchQuery.isNotEmpty() && isSearchExpanded) {
                        // Hiển thị kết quả search chỉ khi SearchBar đang expanded
                        searchResults
                    } else {
                        // Hiển thị danh sách gốc với filter
                        if (selectedEventTypes.isEmpty()) {
                            events
                        } else {
                            events.filter { event ->
                                selectedEventTypes.contains(event.eventTypeId.toString())
                            }
                        }
                    }
                }
                
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                if (isSearchExpanded) {
                                    SearchBar(
                                        modifier = Modifier.fillMaxWidth(),
                                        expanded = isSearchExpanded,
                                        onExpandedChange = { expanded ->
                                            isSearchExpanded = expanded
                                            if (!expanded) {
                                                // Clear search when collapsing
                                                viewModel.clearSearch()
                                            }
                                        },
                                        colors = SearchBarDefaults.colors(
                                            containerColor = Color.Transparent,
                                            dividerColor = Color.Transparent
                                        ),
                                        inputField = {
                                            SearchBarDefaults.InputField(
                                                query = searchQuery,
                                                onQueryChange = { query ->
                                                    viewModel.searchEvents(query)
                                                },
                                                onSearch = { query ->
                                                    viewModel.searchEvents(query)
                                                },
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
                                                            onClick = { 
                                                                viewModel.clearSearch()
                                                            }
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Clear,
                                                                contentDescription = "Clear search"
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    ) {
                                        // Empty content - không cần overlay nữa
                                    }
                                } else {
                                    Text("Sự kiện của tôi")
                                }
                            },
                            actions = {
                                if (!isSearchExpanded) {
                                    // Search button
                                    IconButton(
                                        onClick = { isSearchExpanded = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    // Settings button
                                    IconButton(
                                        onClick = { showApiSettingsDialog = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        
                        // Filter chips (chỉ hiển thị khi không đang search)
                        if (eventTypeChips.isNotEmpty() && searchQuery.isEmpty() && !isSearchExpanded) {
                            LazyRow(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                        
                        // Event List - hiển thị search results hoặc danh sách gốc với filter
                        PersonalEventListCompose(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = if (eventTypeChips.isNotEmpty() && searchQuery.isEmpty() && !isSearchExpanded) 60.dp else 16.dp),
                            viewModel = viewModel,
                            filteredEvents = displayEvents,
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