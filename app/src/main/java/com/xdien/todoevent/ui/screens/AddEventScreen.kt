package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.xdien.todoevent.ui.components.ChipItem
import com.xdien.todoevent.ui.components.ChipList
import com.xdien.todoevent.ui.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventScreen(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedEventType by remember { mutableStateOf<String?>(null) }
    
    // Event type options for chip list
    val eventTypeChips = remember {
        listOf(
            ChipItem(id = "meeting", title = "Meeting", color = androidx.compose.ui.graphics.Color(0xFF2196F3)),
            ChipItem(id = "workshop", title = "Workshop", color = androidx.compose.ui.graphics.Color(0xFF4CAF50)),
            ChipItem(id = "conference", title = "Conference", color = androidx.compose.ui.graphics.Color(0xFF9C27B0)),
            ChipItem(id = "party", title = "Party", color = androidx.compose.ui.graphics.Color(0xFFFF9800)),
            ChipItem(id = "seminar", title = "Seminar", color = androidx.compose.ui.graphics.Color(0xFF607D8B)),
            ChipItem(id = "hackathon", title = "Hackathon", color = androidx.compose.ui.graphics.Color(0xFFE91E63))
        )
    }
    
    // State for chip selection
    var selectedChipItems by remember { mutableStateOf(eventTypeChips.map { it.copy(isSelected = false) }) }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Add New Event") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                viewModel.addTodo(
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    eventType = selectedEventType
                                )
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Event Type Selection with Chip List
            Text(
                text = "Loại sự kiện",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Custom chip list for event types
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(selectedChipItems.size) { index ->
                    val chip = selectedChipItems[index]
                    FilterChip(
                        onClick = {
                            // Update selection - single selection
                            selectedChipItems = selectedChipItems.map { 
                                it.copy(isSelected = it.id == chip.id)
                            }
                            selectedEventType = if (chip.isSelected) null else chip.title
                        },
                        label = { Text(chip.title) },
                        selected = chip.isSelected,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chip.color ?: MaterialTheme.colorScheme.primary,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTodo(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            eventType = selectedEventType
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) {
                Text("Create Event")
            }
        }
    }
} 