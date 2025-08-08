package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.xdien.todoevent.ui.viewmodel.EventFormViewModel
import com.xdien.todoevent.ui.components.ImagePicker
import android.net.Uri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io.File
import android.content.Context
import android.content.ContentResolver
import android.provider.MediaStore
import java.io.FileOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventFormScreen(
    eventId: Long? = null,
    onNavigateBack: () -> Unit = {},
    onEventSaved: () -> Unit = {},
    viewModel: EventFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventTypes by viewModel.eventTypes.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val currentEvent by viewModel.currentEvent.collectAsState()
    
    // Error dialog state
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isNotFoundError by remember { mutableStateOf(false) }
    
    // Load event for editing if eventId is provided
    LaunchedEffect(eventId) {
        eventId?.let { viewModel.loadEventForEdit(it) }
    }
    
    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onEventSaved()
        }
    }
    
    // Handle error
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            errorMessage = uiState.error!!
            // Check if it's a 404 error
            isNotFoundError = uiState.error!!.contains("404") || 
                             uiState.error!!.contains("not found", ignoreCase = true) ||
                             uiState.error!!.contains("không tìm thấy", ignoreCase = true) ||
                             uiState.error!!.contains("không tồn tại", ignoreCase = true)
            showErrorDialog = true
        }
    }
    
    // Handle back navigation
    val onBackPressed = {
        onNavigateBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show success snackbar
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar(
                message = if (uiState.isEditMode) "Cập nhật sự kiện thành công!" else "Tạo sự kiện thành công!",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (uiState.isEditMode) "Chỉnh sửa sự kiện" else "Tạo sự kiện mới"
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveEvent() },
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = if (uiState.isEditMode) "Cập nhật sự kiện" else "Tạo sự kiện"
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Field
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Tiêu đề sự kiện *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.title.isBlank() || uiState.title.length > 100,
                singleLine = true,
                maxLines = 1,
                supportingText = {
                    if (uiState.title.isNotBlank()) {
                        Text("${uiState.title.length}/100")
                    }
                }
            )
            
            // Title error message
            if (uiState.title.isBlank() && uiState.error != null) {
                Text(
                    text = "Tiêu đề sự kiện là bắt buộc",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            } else if (uiState.title.length > 100) {
                Text(
                    text = "Tiêu đề không được vượt quá 100 ký tự",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Description Field
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Mô tả *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = uiState.description.isBlank() || uiState.description.length > 500,
                supportingText = {
                    if (uiState.description.isNotBlank()) {
                        Text("${uiState.description.length}/500")
                    }
                }
            )
            
            // Description error message
            if (uiState.description.isBlank() && uiState.error != null) {
                Text(
                    text = "Mô tả là bắt buộc",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            } else if (uiState.description.length > 500) {
                Text(
                    text = "Mô tả không được vượt quá 500 ký tự",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Event Type Dropdown
            EventTypeDropdown(
                eventTypes = eventTypes,
                selectedTypeId = uiState.typeId,
                onTypeSelected = { viewModel.updateTypeId(it) },
                isError = uiState.typeId == 0
            )
            
            // Event type error message
            if (uiState.typeId == 0 && uiState.error != null) {
                Text(
                    text = "Loại sự kiện là bắt buộc",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Date and Time Picker
            DateTimePicker(
                value = uiState.startDate,
                onValueChange = { viewModel.updateStartDate(it) },
                isError = uiState.startDate.isBlank() || !isValidDateTime(uiState.startDate)
            )
            
            // Date error message
            if (uiState.startDate.isBlank() && uiState.error != null) {
                Text(
                    text = "Thời gian sự kiện là bắt buộc",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            } else if (uiState.startDate.isNotBlank() && !isValidDateTime(uiState.startDate)) {
                Text(
                    text = "Thời gian sự kiện không được trong quá khứ",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Location Field
            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.updateLocation(it) },
                label = { Text("Địa điểm *") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.location.isBlank() || uiState.location.length > 100,
                singleLine = true,
                supportingText = {
                    if (uiState.location.isNotBlank()) {
                        Text("${uiState.location.length}/100")
                    }
                }
            )
            
            // Location error message
            if (uiState.location.isBlank() && uiState.error != null) {
                Text(
                    text = "Địa điểm là bắt buộc",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            } else if (uiState.location.length > 100) {
                Text(
                    text = "Địa điểm không được vượt quá 100 ký tự",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Image Selection Section
            val context = LocalContext.current
            if (!uiState.isEditMode) {
                // For new events - show image picker
                ImagePicker(
                    selectedImages = selectedImages.map { it.toUri() },
                    onImageSelected = { uri ->
                        // Convert URI to File and add to ViewModel
                        val file = convertUriToFile(context, uri)
                        if (file != null) {
                            viewModel.addImages(listOf(file))
                        }
                    },
                    onImageRemoved = { uri ->
                        // Find the corresponding file and remove it
                        val file = selectedImages.find { it.toUri() == uri }
                        if (file != null) {
                            viewModel.removeImage(file)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxItems = 5
                )
            } else {
                // For edit mode - show existing images
                currentEvent?.let { event ->
                    if (event.images.isNotEmpty()) {
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
                                    text = "Ảnh sự kiện",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(event.images) { image ->
                                        Card(
                                            modifier = Modifier.size(80.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            AsyncImage(
                                                model = image.url,
                                                contentDescription = "Event image",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Error Message - Removed from here, will show as dialog instead

            // Success Message - Now handled by Snackbar
            
            // Duplicate Image Message
            if (uiState.duplicateImageMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.duplicateImageMessage!!,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearDuplicateMessage() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
            
            // Upload Progress
            uploadProgress?.let { progress ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Đang tải ảnh lên...",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${progress.uploadedCount}/${progress.totalCount}",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.uploadedCount.toFloat() / progress.totalCount.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
        
        // Error Dialog
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = {
                    showErrorDialog = false
                    viewModel.clearError()
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isNotFoundError) Icons.Default.Info else Icons.Default.Warning,
                            contentDescription = if (isNotFoundError) "Not Found" else "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = when {
                                isNotFoundError -> "Sự kiện không tồn tại"
                                uiState.isEditMode -> "Lỗi cập nhật"
                                else -> "Lỗi tạo sự kiện"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                text = {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    if (isNotFoundError) {
                        // For 404 errors, show "Quay lại" button
                        TextButton(
                            onClick = {
                                showErrorDialog = false
                                viewModel.clearError()
                                onNavigateBack()
                            }
                        ) {
                            Text("Quay lại")
                        }
                    } else {
                        // For other errors, show "Đóng" button
                        TextButton(
                            onClick = {
                                showErrorDialog = false
                                viewModel.clearError()
                            }
                        ) {
                            Text("Đóng")
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Event Type Dropdown Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTypeDropdown(
    eventTypes: List<com.xdien.todoevent.domain.model.EventType>,
    selectedTypeId: Int,
    onTypeSelected: (Int) -> Unit,
    isError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedEventType = eventTypes.find { it.id == selectedTypeId }
    val selectedEventTypeName = selectedEventType?.name ?: ""
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedEventTypeName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Loại sự kiện *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            isError = isError
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            eventTypes.forEach { eventType ->
                DropdownMenuItem(
                    text = { Text(eventType.name) },
                    onClick = {
                        onTypeSelected(eventType.id)
                        expanded = false
                    }
                )
            }
        }
        
    }
}

/**
 * Date and Time Picker Component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val currentDate = remember { LocalDateTime.now() }
    var selectedDate by remember { mutableStateOf(currentDate.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(currentDate.toLocalTime()) }
    
    // Update the value when date or time changes
    LaunchedEffect(selectedDate, selectedTime) {
        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        onValueChange(dateTime.format(formatter))
    }
    
    // Initialize with current value if provided
    LaunchedEffect(value) {
        if (value.isNotBlank()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateTime = LocalDateTime.parse(value, formatter)
                selectedDate = dateTime.toLocalDate()
                selectedTime = dateTime.toLocalTime()
            } catch (e: Exception) {
                // Keep current values if parsing fails
            }
        }
    }
    
    // Format display value
    val displayValue = remember(value) {
        if (value.isNotBlank()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val dateTime = LocalDateTime.parse(value, formatter)
                val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                dateTime.format(displayFormatter)
            } catch (e: Exception) {
                value // Fallback to original value if parsing fails
            }
        } else {
            ""
        }
    }
    
    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        readOnly = true,
        label = { Text("Thời gian sự kiện *") },
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Select date")
            }
        }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Chọn thời gian") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



/**
 * Validate if date time is not in the past
 */
private fun isValidDateTime(dateTimeString: String): Boolean {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val eventDateTime = LocalDateTime.parse(dateTimeString, formatter)
        val currentDateTime = LocalDateTime.now()
        eventDateTime.isAfter(currentDateTime)
    } catch (e: Exception) {
        false
    }
}

/**
 * Extension function to convert File to Uri
 */
private fun File.toUri(): Uri {
    return Uri.fromFile(this)
}

/**
 * Extension function to convert Uri to File
 */
private fun Uri.toFile(): File {
    return File(this.path ?: "")
}

/**
 * Convert URI to File
 */
private fun convertUriToFile(context: Context, uri: Uri): File? {
    return try {
        when (uri.scheme) {
            "file" -> {
                // Direct file URI
                File(uri.path ?: "")
            }
            "content" -> {
                // Content URI - need to copy to cache
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val fileName = getFileName(context, uri)
                    val file = File(context.cacheDir, fileName)
                    val outputStream = FileOutputStream(file)
                    
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    
                    file
                } else {
                    null
                }
            }
            else -> null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Get file name from URI
 */
private fun getFileName(context: Context, uri: Uri): String {
    val contentResolver: ContentResolver = context.contentResolver
    val cursor = contentResolver.query(uri, null, null, null, null)
    
    return try {
        if (cursor != null && cursor.moveToFirst()) {
            val displayNameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                cursor.getString(displayNameIndex)
            } else {
                "image_${System.currentTimeMillis()}.jpg"
            }
        } else {
            "image_${System.currentTimeMillis()}.jpg"
        }
    } finally {
        cursor?.close()
    }
} 