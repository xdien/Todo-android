package com.xdien.todoevent.ui.components

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xdien.todoevent.domain.usecase.RequestCameraPermissionUseCase


/**
 * Helper function to get image information using ContentResolver
 */
private fun getImageInfo(contentResolver: ContentResolver, uri: Uri): String? {
    return try {
        android.util.Log.d("ImagePicker", "Getting image info for URI: $uri")
        val mimeType = contentResolver.getType(uri)
        android.util.Log.d("ImagePicker", "MIME type: $mimeType")
        if (mimeType?.startsWith("image/") == true) {
            android.util.Log.d("ImagePicker", "Valid image MIME type: $mimeType")
            mimeType
        } else {
            android.util.Log.w("ImagePicker", "Invalid MIME type: $mimeType")
            null
        }
    } catch (e: Exception) {
        android.util.Log.e("ImagePicker", "Error getting image info for URI: $uri", e)
        null
    }
}

/**
 * Helper function to get file size using ContentResolver
 */
private fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long? {
    return try {
        android.util.Log.d("ImagePicker", "Getting file size for URI: $uri")
        val size = contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.available().toLong()
        }
        android.util.Log.d("ImagePicker", "File size: $size bytes")
        size
    } catch (e: Exception) {
        android.util.Log.e("ImagePicker", "Error getting file size for URI: $uri", e)
        null
    }
}

/**
 * Format file size to human readable format
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

/**
 * Take persistent URI permission for long-running operations
 */
private fun takePersistableUriPermission(context: android.content.Context, uri: Uri) {
    try {
        val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, flag)
    } catch (e: Exception) {
        // Handle permission error silently
        // This is expected for some URI types that don't support persistent permissions
    }
}



@Composable
fun ImagePicker(
    selectedImages: List<Uri>,
    onImageSelected: (Uri) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    maxItems: Int = 5,
    permissionUseCase: RequestCameraPermissionUseCase? = null
) {
    val context = LocalContext.current
    val contentResolver: ContentResolver = context.contentResolver
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImages by remember { mutableStateOf<Set<Uri>>(emptySet()) }
    var hasPermission by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Track original camera URIs to prevent conversion
    var originalCameraUris by remember { mutableStateOf<Map<Uri, Uri>>(emptyMap()) }
    
    // Calculate remaining slots
    val remainingSlots = maxItems - selectedImages.size
    
    LaunchedEffect(Unit) {
        if (permissionUseCase != null) {
            hasPermission = permissionUseCase.allPermissionsGranted(context)
        } else {
            hasPermission = true
        }
    }
    
    LaunchedEffect(permissionUseCase) {
        if (permissionUseCase != null) {
            hasPermission = permissionUseCase.allPermissionsGranted(context)
        }
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        hasPermission = allGranted
        if (allGranted) {
            showImageSourceDialog = true
        }
    }
    
    // Photo picker launcher for selecting single image (when only 1 slot remains)
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        android.util.Log.d("ImagePicker", "Single photo picker result - selected: $uri, remaining slots: $remainingSlots")
        uri?.let { selectedUri ->
            // Validate URI using ContentResolver
            val imageInfo = getImageInfo(contentResolver, selectedUri)
            if (imageInfo != null) {
                // Take persistent URI permission for long-running operations
                takePersistableUriPermission(context, selectedUri)
                onImageSelected(selectedUri)
            }
        }
    }
    
    // Photo picker launcher for selecting multiple images (when 2 or more slots remain)
    // Use remainingSlots to limit the selection based on current selected images
    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(
            maxItems = if (remainingSlots >= 2) remainingSlots else 2  // Ensure maxItems >= 2
        )
    ) { uris ->
        android.util.Log.d("ImagePicker", "Multiple photo picker result - selected: ${uris.size}, remaining slots: $remainingSlots")
        // Only process up to remainingSlots images
        val limitedUris = uris.take(remainingSlots)
        limitedUris.forEach { uri ->
            // Validate URI using ContentResolver
            val imageInfo = getImageInfo(contentResolver, uri)
            if (imageInfo != null) {
                // Take persistent URI permission for long-running operations
                takePersistableUriPermission(context, uri)
                onImageSelected(uri)
            }
        }
    }
    
    // Camera launcher for taking photos
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        android.util.Log.d("ImagePicker", "Camera launcher result - success: $success, uri: $cameraImageUri, remaining slots: $remainingSlots")
        if (success && cameraImageUri != null) {
            try {
                android.util.Log.d("ImagePicker", "Processing camera image: $cameraImageUri")
                
                // Check if file exists using ContentResolver
                val inputStream = contentResolver.openInputStream(cameraImageUri!!)
                val fileExists = inputStream != null
                inputStream?.close()
                android.util.Log.d("ImagePicker", "File exists via ContentResolver: $fileExists")
                
                // Validate the captured image
                val imageInfo = getImageInfo(contentResolver, cameraImageUri!!)
                android.util.Log.d("ImagePicker", "Image info: $imageInfo")
                
                if (imageInfo != null) {
                    // Take persistent URI permission for long-running operations
                    takePersistableUriPermission(context, cameraImageUri!!)
                    
                    // Convert content URI to file URI for better compatibility
                    val filePath = cameraImageUri!!.path
                    val fileName = filePath?.substringAfterLast("/") ?: "camera_image.jpg"
                    val fileUri = Uri.fromFile(File(context.cacheDir, fileName))
                    
                    
                    // Mark this as a camera image using file URI
                    cameraImages = cameraImages + fileUri
                    // Store mapping from file URI to original content URI
                    originalCameraUris = originalCameraUris + (fileUri to cameraImageUri!!)
                    onImageSelected(fileUri)
                    android.util.Log.d("ImagePicker", "Camera image selected successfully")
                } else {
                    // Log error if image validation fails
                    android.util.Log.e("ImagePicker", "Failed to validate camera image: $cameraImageUri")
                }
            } catch (e: Exception) {
                android.util.Log.e("ImagePicker", "Error processing camera image", e)
            }
        } else {
            android.util.Log.w("ImagePicker", "Camera capture failed or URI is null")
        }
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hình ảnh sự kiện",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Info icon with tooltip
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Thông tin",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Add image button
            if (selectedImages.size < maxItems) {
                item {
                    Card(
                        modifier = Modifier.size(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Show image source dialog when clicked
                                IconButton(
                                    onClick = { 
                                        if (hasPermission) {
                                            showImageSourceDialog = true
                                        } else {
                                            permissionUseCase?.let { useCase ->
                                                if (useCase.needsPermissionRequest(context)) {
                                                    permissionLauncher.launch(useCase.getRequiredPermissions())
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Thêm ảnh",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Thêm ảnh",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${selectedImages.size}/$maxItems",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Selected images
            items(selectedImages) { imageUri ->
                android.util.Log.d("ImagePicker", "Rendering image item: $imageUri, isCameraImage: ${cameraImages.contains(imageUri)}")
                
                // Check if this is a camera image by URI scheme and path
                val isCameraImageByUri = imageUri.scheme == "content" && 
                    imageUri.host == "${context.packageName}.fileprovider" &&
                    imageUri.path?.contains("camera_") == true
                
                // Check if this is a camera image file URI
                val isCameraImageFileUri = imageUri.scheme == "file" && 
                    imageUri.path?.contains("camera_") == true
                
                val shouldUseCameraImage = cameraImages.contains(imageUri) || isCameraImageByUri || isCameraImageFileUri
                val uriToUse = imageUri // Use the URI as is
                
                android.util.Log.d("ImagePicker", "isCameraImageByUri: $isCameraImageByUri, isCameraImageFileUri: $isCameraImageFileUri, shouldUseCameraImage: $shouldUseCameraImage, uriToUse: $uriToUse")
                
                Card(
                    modifier = Modifier.size(120.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box {
                        if (shouldUseCameraImage) {
                            android.util.Log.d("ImagePicker", "Using AsyncImage for camera: $uriToUse")
                            // Use AsyncImage for camera photos (content URI)
                            AsyncImage(
                                model = uriToUse,
                                contentDescription = "Ảnh đã chọn",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = { state ->
                                    android.util.Log.e("ImagePicker", "Error loading camera image: ${state.result}")
                                },
                                onSuccess = { state ->
                                    android.util.Log.d("ImagePicker", "Camera image loaded successfully: $uriToUse")
                                }
                            )
                        } else {
                            android.util.Log.d("ImagePicker", "Using AsyncImage for gallery: $imageUri")
                            // Use AsyncImage for gallery photos
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Ảnh đã chọn",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = { state ->
                                    android.util.Log.e("ImagePicker", "Error loading image: ${state.result}")
                                },
                                onSuccess = { state ->
                                    android.util.Log.d("ImagePicker", "Image loaded successfully: $imageUri")
                                }
                            )
                        }
                        
                        // Remove button
                        IconButton(
                            onClick = { 
                                onImageRemoved(imageUri)
                                // Also remove from camera images set and original URIs
                                cameraImages = cameraImages - imageUri
                                originalCameraUris = originalCameraUris.filter { (key, _) -> key != imageUri }
                                android.util.Log.d("ImagePicker", "Image removed: $imageUri")
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Xóa ảnh",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Image type indicator
                        val imageInfo = getImageInfo(contentResolver, imageUri)
                        imageInfo?.let { info ->
                            Card(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                )
                            ) {
                                Text(
                                    text = info.substringAfter("/").uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        
                        // File size warning for large files
                        val fileSize = getFileSize(contentResolver, imageUri)
                        fileSize?.let { size ->
                            if (size > 5 * 1024 * 1024) { // 5MB
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Cảnh báo",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = formatFileSize(size),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Info text
        if (selectedImages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = "Chọn tối đa $maxItems hình ảnh cho sự kiện của bạn",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Đã chọn ${selectedImages.size}/$maxItems hình ảnh (còn lại $remainingSlots)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    // Show total file size
                    val totalSize = selectedImages.sumOf { uri ->
                        getFileSize(contentResolver, uri) ?: 0L
                    }
                    if (totalSize > 0) {
                        Text(
                            text = "Tổng dung lượng: ${formatFileSize(totalSize)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
    
    // Image source selection dialog
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = {
                Text("Chọn nguồn ảnh")
            },
            text = {
                Column {
                    Text(
                        text = "Chọn cách bạn muốn thêm ảnh vào sự kiện",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Camera button
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            // Check if there are remaining slots and permission
                            if (remainingSlots > 0 && hasPermission) {
                                // Create a temporary file for camera capture
                                val tempFile = File.createTempFile("camera_", ".jpg", context.cacheDir)
                                cameraImageUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    tempFile
                                )
                                android.util.Log.d("ImagePicker", "Camera URI created: $cameraImageUri")
                                android.util.Log.d("ImagePicker", "Temp file path: ${tempFile.absolutePath}")
                                android.util.Log.d("ImagePicker", "Temp file exists: ${tempFile.exists()}")
                                cameraLauncher.launch(cameraImageUri!!)
                            } else if (!hasPermission) {
                                // Hiển thị thông báo cần quyền
                                android.util.Log.w("ImagePicker", "Camera permission not granted")
                            }
                        },
                        enabled = remainingSlots > 0 && hasPermission
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chụp ảnh")
                    }
                    
                    // Gallery picker (single or multiple based on remaining slots)
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            if (remainingSlots > 0 && hasPermission) {
                                if (remainingSlots == 1) {
                                    // Use single picker when only 1 slot remains
                                    singlePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest.Builder()
                                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            .build()
                                    )
                                } else {
                                    // Use multiple picker when 2 or more slots remain
                                    multiplePhotoPickerLauncher.launch(
                                        PickVisualMediaRequest.Builder()
                                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            .build()
                                    )
                                }
                            } else if (!hasPermission) {
                                android.util.Log.w("ImagePicker", "Photo permission not granted")
                            }
                        },
                        enabled = remainingSlots > 0 && hasPermission
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (remainingSlots == 1) "Chọn ảnh" else "Chọn nhiều ảnh ($remainingSlots)")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImageSourceDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text("Quyền truy cập cần thiết")
            },
            text = {
                Text(
                    text = permissionUseCase?.getPermissionExplanation() 
                        ?: "Ứng dụng cần quyền truy cập camera và ảnh để chụp và chọn ảnh."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        permissionUseCase?.let { useCase ->
                            permissionLauncher.launch(useCase.getRequiredPermissions())
                        }
                    }
                ) {
                    Text("Cấp quyền")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
} 