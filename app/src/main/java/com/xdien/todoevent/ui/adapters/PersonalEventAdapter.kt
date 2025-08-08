package com.xdien.todoevent.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xdien.todoevent.domain.model.Event
import java.text.SimpleDateFormat
import java.util.*

class PersonalEventAdapter(
    private val onEventClick: (Event) -> Unit
) : ListAdapter<Event, PersonalEventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val composeView = ComposeView(parent.context)
        return EventViewHolder(composeView, onEventClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = getItem(position)
        android.util.Log.d("PersonalEventAdapter", "Binding event at position $position: ${event.title} (ID: ${event.id})")
        holder.bind(event)
    }

    class EventViewHolder(
        private val composeView: ComposeView,
        private val onEventClick: (Event) -> Unit
    ) : RecyclerView.ViewHolder(composeView) {
        
        fun bind(event: Event) {
            composeView.setContent {
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
    
    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    eventTypeName: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp)
        ) {
            // Thumbnail Image
            val imageUrl = event.images.firstOrNull()?.url ?: "https://via.placeholder.com/120x120"
            android.util.Log.d("EventCard", "Event: ${event.title}, Image URL: $imageUrl, Images count: ${event.images.size}")
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Event thumbnail",
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentScale = ContentScale.Crop
            )
            
            // Event Details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2
                )
                
                // Event Type Badge
                Card(
                    modifier = Modifier.wrapContentWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = eventTypeName ?: "Type ${event.eventTypeId}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                // Time and location
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    event.startDate?.let { startDate ->
                        Text(
                            text = formatEventTime(startDate),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    event.location?.let { location ->
                        Text(
                            text = location,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun formatEventTime(dateTimeString: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
        val zonedDateTime = java.time.ZonedDateTime.parse(dateTimeString, formatter)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        dateFormat.format(Date.from(zonedDateTime.toInstant()))
    } catch (e: Exception) {
        dateTimeString
    }
} 