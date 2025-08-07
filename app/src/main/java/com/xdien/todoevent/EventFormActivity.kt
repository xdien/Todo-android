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
import com.xdien.todoevent.ui.screens.EventFormScreen
import com.xdien.todoevent.ui.theme.TodoEventTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventFormActivity : ComponentActivity() {
    
    companion object {
        private const val EXTRA_EVENT_ID = "event_id"
        
        fun createIntent(context: Context, eventId: Long? = null): Intent {
            return Intent(context, EventFormActivity::class.java).apply {
                eventId?.let { putExtra(EXTRA_EVENT_ID, it) }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        val isEditMode = eventId != -1L
        
        setContent {
            TodoEventTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EventFormScreen(
                        eventId = if (isEditMode) eventId else null,
                        onNavigateBack = { finish() },
                        onEventSaved = { finish() }
                    )
                }
            }
        }
    }
} 