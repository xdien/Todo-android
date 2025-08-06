package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xdien.todoevent.ui.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val event by viewModel.getTodoById(eventId).collectAsStateWithLifecycle(initialValue = null)
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var galleryImagesText by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    
    // Event types for dropdown
    val eventTypes = listOf("Hội thảo", "Workshop", "Meetup", "Hackathon", "Conference", "Seminar", "Training", "Khác")
    
    LaunchedEffect(event) {
        event?.let { todo ->
            title = todo.title
            description = todo.description ?: ""
            location = todo.location ?: ""
            eventType = todo.eventType ?: ""
            thumbnailUrl = todo.thumbnailUrl ?: ""
            galleryImagesText = todo.galleryImages?.joinToString("\n") ?: ""
            startTime = todo.eventTime?.let { 
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
            } ?: ""
            endTime = todo.eventEndTime?.let { 
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
            } ?: ""
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa sự kiện") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Save changes
                            event?.let { todo ->
                                val galleryImages = if (galleryImagesText.isNotBlank()) {
                                    galleryImagesText.split("\n").filter { it.isNotBlank() }
                                } else null
                                
                                val startTimeMillis = parseDateTime(startTime)
                                val endTimeMillis = parseDateTime(endTime)
                                
                                viewModel.updateTodoWithNewData(
                                    id = todo.id,
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    thumbnailUrl = thumbnailUrl.takeIf { it.isNotBlank() },
                                    galleryImages = galleryImages,
                                    eventTime = startTimeMillis,
                                    eventEndTime = endTimeMillis,
                                    location = location.takeIf { it.isNotBlank() },
                                    eventType = eventType.takeIf { it.isNotBlank() }
                                )
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Lưu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề sự kiện *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            // Event Type
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = eventType,
                    onValueChange = { eventType = it },
                    label = { Text("Loại sự kiện") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    eventTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { 
                                eventType = type
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Địa điểm") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                }
            )
            
            // Start Time
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Thời gian bắt đầu (dd/MM/yyyy HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                placeholder = { Text("Ví dụ: 25/12/2024 09:00") }
            )
            
            // End Time
            OutlinedTextField(
                value = endTime,
                onValueChange = { endTime = it },
                label = { Text("Thời gian kết thúc (dd/MM/yyyy HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                placeholder = { Text("Ví dụ: 25/12/2024 17:00") }
            )
            
            // Thumbnail URL
            OutlinedTextField(
                value = thumbnailUrl,
                onValueChange = { thumbnailUrl = it },
                label = { Text("URL hình ảnh chính") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                }
            )
            
            // Gallery Images
            OutlinedTextField(
                value = galleryImagesText,
                onValueChange = { galleryImagesText = it },
                label = { Text("URL hình ảnh gallery (mỗi URL một dòng)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                placeholder = { Text("https://example.com/image1.jpg\nhttps://example.com/image2.jpg") }
            )
            
            // Help text
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Hướng dẫn:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Tiêu đề là bắt buộc\n" +
                                "• Thời gian theo định dạng dd/MM/yyyy HH:mm\n" +
                                "• Mỗi URL hình ảnh gallery trên một dòng riêng\n" +
                                "• Để trống các trường không cần thiết",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun parseDateTime(dateTimeString: String): Long? {
    return try {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        dateFormat.parse(dateTimeString)?.time
    } catch (e: Exception) {
        null
    }
} 