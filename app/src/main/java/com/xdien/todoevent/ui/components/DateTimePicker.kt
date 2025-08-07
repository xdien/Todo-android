package com.xdien.todoevent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePicker(
    selectedDateTime: Long?,
    onDateTimeSelected: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    val selectedDate = selectedDateTime?.let { Date(it) }
    val dateText = selectedDate?.let { dateFormatter.format(it) } ?: ""
    val timeText = selectedDate?.let { timeFormatter.format(it) } ?: ""
    
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date picker
            OutlinedTextField(
                value = dateText,
                onValueChange = { },
                label = { Text("Ngày") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Date")
                }
            )
            
            // Time picker
            OutlinedTextField(
                value = timeText,
                onValueChange = { },
                label = { Text("Giờ") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = "Time")
                }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Chọn ngày")
            }
            
            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Chọn giờ")
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateTime ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { dateMillis ->
                            val currentTime = selectedDateTime ?: System.currentTimeMillis()
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = currentTime
                            }
                            val selectedCalendar = Calendar.getInstance().apply {
                                timeInMillis = dateMillis
                            }
                            
                            // Keep the same time, change only date
                            calendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR))
                            calendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH))
                            calendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH))
                            
                            onDateTimeSelected(calendar.timeInMillis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedDate?.let { Calendar.getInstance().apply { time = it }.get(Calendar.HOUR_OF_DAY) } ?: 0,
            initialMinute = selectedDate?.let { Calendar.getInstance().apply { time = it }.get(Calendar.MINUTE) } ?: 0
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Chọn thời gian") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentDateTime = selectedDateTime ?: System.currentTimeMillis()
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = currentDateTime
                        }
                        
                        // Update time
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        
                        onDateTimeSelected(calendar.timeInMillis)
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Hủy")
                }
            }
        )
    }
} 