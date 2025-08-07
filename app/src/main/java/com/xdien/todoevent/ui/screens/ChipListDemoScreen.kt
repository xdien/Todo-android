package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xdien.todoevent.ui.components.ChipList
import com.xdien.todoevent.ui.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipListDemoScreen(
    viewModel: TodoViewModel,
    modifier: Modifier = Modifier
) {
    val selectedEvents by viewModel.selectedChipIds.collectAsState()
    val selectedEventsList = viewModel.getSelectedEvents()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Single Selection Chip List
        Text(
            text = "Chọn một sự kiện (Single Selection)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        ChipList(
            chips = viewModel.chipItems,
            onChipClick = { chip ->
                viewModel.selectChip(chip.id, singleSelection = true)
            },
            singleSelection = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Multiple Selection Chip List
        Text(
            text = "Chọn nhiều sự kiện (Multiple Selection)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        ChipList(
            chips = viewModel.chipItems,
            onChipClick = { chip ->
                viewModel.selectChip(chip.id, singleSelection = false)
            },
            singleSelection = false,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Selected Items Display
        if (selectedEvents.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sự kiện đã chọn:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    selectedEventsList.forEach { event ->
                        Text(
                            text = "• ${event.title}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.clearChipSelection() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Xóa lựa chọn")
            }
            
            Button(
                onClick = { viewModel.loadEvents() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Tải lại")
            }
        }
        
        // Event Type Filter
        Text(
            text = "Lọc theo loại sự kiện",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        val eventTypes = listOf("Hội thảo", "Workshop", "Meetup", "Hackathon")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(eventTypes) { eventType ->
                FilterChip(
                    onClick = {
                        // Filter logic can be implemented here
                    },
                    label = { Text(eventType) },
                    selected = false
                )
            }
        }
    }
} 