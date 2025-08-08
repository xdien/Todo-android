package com.xdien.todoevent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xdien.todoevent.common.EventBus
import com.xdien.todoevent.domain.model.Event
import com.xdien.todoevent.domain.usecase.GetEventByIdUseCase
import com.xdien.todoevent.ui.viewmodel.TodoViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val event by viewModel.selectedEvent.collectAsStateWithLifecycle()
    val eventTypes by viewModel.eventTypes.collectAsStateWithLifecycle()
    val isEventLoading by viewModel.isEventLoading.collectAsStateWithLifecycle()
    val eventError by viewModel.eventError.collectAsStateWithLifecycle()
    
    // Load event initially
    LaunchedEffect(eventId) {
        viewModel.loadEventById(eventId.toInt())
    }
    
    // Listen to event changes from EventBus and refresh if the current event is updated
    LaunchedEffect(eventId) {
        // Get EventBus from the same Hilt container
        val eventBus = viewModel.getEventBus()
        eventBus.eventChanges.collect { eventChange ->
            when (eventChange) {
                is EventBus.EventChange.EventUpdated -> {
                    // Refresh the current event if it matches the eventId
                    android.util.Log.d("EventDetailScreen", "Event updated, refreshing event details for ID: $eventId")
                    viewModel.loadEventById(eventId.toInt())
                }
                is EventBus.EventChange.EventDeleted -> {
                    // If the current event is deleted, navigate back
                    android.util.Log.d("EventDetailScreen", "Event deleted, navigating back")
                    onNavigateBack()
                }
                else -> {
                    // For other changes, we don't need to do anything specific
                }
            }
        }
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết sự kiện") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    // Share button
                    IconButton(
                        onClick = {
                            event?.let { shareEvent(context, it, eventTypes) }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Chia sẻ")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit FAB
                FloatingActionButton(
                    onClick = { event?.let { onNavigateToEdit(it.id.toLong()) } },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                }
                
                // Delete FAB
                FloatingActionButton(
                    onClick = { showDeleteDialog = true },
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Xóa")
                }
            }
        }
    ) { paddingValues ->
        event?.let { event ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Carousel
                ImageCarousel(
                    images = event.images.map { it.url },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )
                
                // Event Details
                EventDetails(
                    event = event,
                    eventTypes = eventTypes,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } ?: run {
            // Show appropriate state based on loading and error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isEventLoading -> {
                        CircularProgressIndicator()
                    }
                    eventError != null -> {
                        // Error state - check if it's a 404 error
                        val isNotFoundError = eventError?.contains("404") == true || 
                                             eventError?.contains("not found", ignoreCase = true) == true ||
                                             eventError?.contains("không tìm thấy", ignoreCase = true) == true
                        
                        if (isNotFoundError) {
                            // Not found state
                            NotFoundState(
                                onNavigateBack = onNavigateBack,
                                onRetry = {
                                    viewModel.clearEventError()
                                    // Reload the event
                                    viewModel.loadEventById(eventId.toInt())
                                }
                            )
                        } else {
                            // Other error state
                            ErrorState(
                                errorMessage = eventError ?: "Unknown error occurred",
                                onRetry = {
                                    viewModel.clearEventError()
                                    // Reload the event
                                    viewModel.loadEventById(eventId.toInt())
                                }
                            )
                        }
                    }
                    else -> {
                        // Initial loading state
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sự kiện này không?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        event?.let { 
                            viewModel.deleteEvent(it.id)
                            // Navigate back immediately after deletion
                            onNavigateBack()
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) {
        // Placeholder when no images
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else if (images.size == 1) {
        // Single image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(images.first())
                .crossfade(true)
                .build(),
            contentDescription = "Event image",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Multiple images with ViewPager2
        AndroidView(
            modifier = modifier,
            factory = { context ->
                androidx.viewpager2.widget.ViewPager2(context).apply {
                    orientation = androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
                    adapter = ImagePagerAdapter(images)
                }
            }
        )
    }
}

class ImagePagerAdapter(private val images: List<String>) : 
    androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder>() {
    
    class ImageViewHolder(val imageView: android.widget.ImageView) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(imageView)
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = android.widget.ImageView(parent.context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
        return ImageViewHolder(imageView)
    }
    
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        coil.Coil.imageLoader(holder.imageView.context).enqueue(
            coil.request.ImageRequest.Builder(holder.imageView.context)
                .data(images[position])
                .target(holder.imageView)
                .build()
        )
    }
    
    override fun getItemCount(): Int = images.size
}

@Composable
fun EventDetails(
    event: Event,
    eventTypes: List<com.xdien.todoevent.domain.model.EventType>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        // Event Type Badge
        Card(
            modifier = Modifier.wrapContentWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            val eventTypeName = eventTypes.find { it.id == event.eventTypeId }?.name ?: "Type ${event.eventTypeId}"
            Text(
                text = eventTypeName,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Time Information
        TimeInfo(
            startTime = event.startDate,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Location
        event.location?.let { location ->
            LocationInfo(
                location = location,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Description
        DescriptionInfo(
            description = event.description,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Additional Info
        AdditionalInfo(
            event = event,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TimeInfo(
    startTime: String,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Thời gian",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = startTime,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LocationInfo(
    location: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Địa điểm",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DescriptionInfo(
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Mô tả",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AdditionalInfo(
    event: Event,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Thông tin khác",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            

            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ngày tạo:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = event.createdAt,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun shareEvent(context: android.content.Context, event: Event, eventTypes: List<com.xdien.todoevent.domain.model.EventType>) {
    val eventTypeName = eventTypes.find { it.id == event.eventTypeId }?.name ?: "Type ${event.eventTypeId}"
    val shareText = buildString {
        appendLine("Sự kiện: ${event.title}")
        appendLine("Mô tả: ${event.description}")
        appendLine("Thời gian: ${event.startDate}")
        appendLine("Địa điểm: ${event.location}")
        appendLine("Loại sự kiện: $eventTypeName")
    }
    
    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    
    val shareIntent = android.content.Intent.createChooser(sendIntent, "Chia sẻ sự kiện")
    context.startActivity(shareIntent)
}

@Composable
fun NotFoundState(
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                    Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Không tìm thấy sự kiện",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sự kiện bạn đang tìm kiếm có thể đã bị xóa hoặc không tồn tại.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quay lại")
            }
            
            Button(
                onClick = onRetry
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thử lại")
            }
        }
    }
}

@Composable
fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
                    Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Đã xảy ra lỗi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thử lại")
        }
    }
} 