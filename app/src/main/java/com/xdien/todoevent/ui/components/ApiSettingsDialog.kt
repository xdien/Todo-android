package com.xdien.todoevent.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsDialog(
    currentApiUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiUrl by remember { mutableStateOf(currentApiUrl) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Cài đặt API Server",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { 
                        apiUrl = it
                        isError = false
                    },
                    label = { Text("Địa chỉ API Server") },
                    placeholder = { Text("http://192.168.31.194:5000") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Uri
                    ),
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Vui lòng nhập địa chỉ API hợp lệ") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Hủy")
                    }
                    
                    Button(
                        onClick = {
                            if (apiUrl.isNotBlank()) {
                                onSave(apiUrl)
                                onDismiss()
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text("Lưu")
                    }
                }
            }
        }
    }
} 