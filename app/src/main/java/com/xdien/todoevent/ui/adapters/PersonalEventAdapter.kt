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
import com.xdien.todoevent.data.entity.TodoEntity
import java.text.SimpleDateFormat
import java.util.*

class PersonalEventAdapter(
    private val onEventClick: (TodoEntity) -> Unit
) : ListAdapter<TodoEntity, PersonalEventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val composeView = ComposeView(parent.context)
        return EventViewHolder(composeView, onEventClick)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val composeView: ComposeView,
        private val onEventClick: (TodoEntity) -> Unit
    ) : RecyclerView.ViewHolder(composeView) {
        
        fun bind(event: TodoEntity) {
            composeView.setContent {
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) }
                )
            }
        }
    }
    
    private class EventDiffCallback : DiffUtil.ItemCallback<TodoEntity>() {
        override fun areItemsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: TodoEntity, newItem: TodoEntity): Boolean {
            return oldItem == newItem
        }
    }
}

@Composable
fun EventCard(
    event: TodoEntity,
    onClick: () -> Unit
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
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(event.thumbnailUrl ?: "https://via.placeholder.com/120x120")
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
                
                // Time and location
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    event.eventTime?.let { time ->
                        Text(
                            text = formatEventTime(time),
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

private fun formatEventTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
} 