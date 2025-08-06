package com.xdien.todoevent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.xdien.todoevent.ui.screens.EventDetailScreen
import com.xdien.todoevent.ui.screens.EventEditScreen
import com.xdien.todoevent.ui.theme.TodoEventTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventDetailActivity : ComponentActivity() {
    
    companion object {
        private const val EXTRA_EVENT_ID = "event_id"
        
        fun createIntent(context: Context, eventId: Long): Intent {
            return Intent(context, EventDetailActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, eventId)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        if (eventId == -1L) {
            finish()
            return
        }
        
        setContent {
            TodoEventTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventDetailScreen(
                        eventId = eventId,
                        onNavigateBack = { finish() },
                        onNavigateToEdit = { editEventId ->
                            // Navigate to edit screen
                            startActivity(EventEditActivity.createIntent(this, editEventId))
                        }
                    )
                }
            }
        }
    }
} 